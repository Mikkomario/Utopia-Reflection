package utopia.reflection.container

import utopia.flow.collection.VolatileList
import utopia.flow.datastructure.immutable.GraphEdge
import utopia.flow.datastructure.mutable.GraphNode
import utopia.flow.util.Counter
import utopia.reflection.component.Stackable

import scala.collection.mutable

object StackHierarchyManager
{
	// TYPES	-------------------------
	
	private type Node = GraphNode[Stackable, Int]
	private type Edge = GraphEdge[Stackable, Int, Node]
	
	
	// ATTRIBUTES	---------------------
	
	private val indexCounter = new Counter(1)
	// Stakable -> id, used for finding parents
	private val ids = mutable.HashMap[Stackable, StackId]()
	// Id -> Node -> Children, used for finding children
	private val graph = mutable.HashMap[Int, Node]()
	
	private val validationQueue = VolatileList[Stackable]()
	
	
	// OTHER	-------------------------
	
	/**
	  * Requests validation for the specified item
	  * @param item An item
	  */
	def requestValidationFor(item: Stackable) = validationQueue :+= item
	
	def revalidate(): Unit =
	{
		val items = validationQueue.getAndSet(Vector()).toSet
		val itemIds = items.flatMap(ids.get)
		
		// Validates the necessary master items
		val masterNodes = itemIds.map { _.masterId }.flatMap { index => graph.get(index) }
		// TODO: Perform validation once a function is available. masterNodes.foreach { _.content.revalidate() }
		
		// Validates the deeper levels
		val nextIds = itemIds.flatMap { _.tail }
		revalidate(nextIds, masterNodes)
	}
	
	private def revalidate(remainingIds: Set[StackId], nodes: Set[Node]): Unit =
	{
		// Finds the next set of nodes to validate
		val currentLevelIds = remainingIds.map { _.head }
		val nextNodes = nodes.flatMap { _.leavingEdges.filter { e => currentLevelIds.contains(e.content) }.map { _.end } }
		
		if (nextNodes.nonEmpty)
		{
			// Validates the items
			// TODO: Add validation function to stackable and then call it here
			
			// Traverses to the next level, if necessary
			val nextIds = remainingIds.flatMap { _.tail }
			if (nextIds.nonEmpty)
				revalidate(nextIds, nextNodes)
		}
	}
	
	/**
	  * Unregisters a component. This removes this component, as well as all its child components from this hierarchy
	  * @param item The stackable item to be removed from this hierarchy
	  */
	def unregister(item: Stackable) =
	{
		// Removes the provided item and each child from both ids and graph
		if (ids.contains(item))
		{
			// Finds correct id and node
			val itemId = ids(item)
			val node = nodeForId(itemId)
			
			// Removes the node from graph
			if (itemId.isMasterId)
				graph -= itemId.masterId
			else
				graphForId(itemId).disconnectAll(node)
			
			// Removes any child nodes
			node.foreach { ids -= _.content }
		}
	}
	
	/**
	  * Registers a parent-child combo to this hierarchy
	  * @param parent A parent element
	  * @param child A child element
	  */
	def registerConnection(parent: Stackable, child: Stackable) =
	{
		// If the child already had a parent, makes the child a master (top level component) first
		if (ids.contains(child))
		{
			val childId = ids(child)
			childId.parentId.foreach
			{
				parentId =>
					
					// Disconnects the child from the parent, also updates all id numbers
					val parentNode = nodeForId(parentId)
					val childIndex = childId.last
					val childNode = (parentNode / childIndex).head
					
					parentNode.disconnectDirect(childNode)
					childNode.foreach { c => ids(c.content) = ids(c.content).dropUntil(childIndex) }
					
					// Makes the child a master
					graph(childIndex) = childNode
			}
		}
		
		// Makes sure that the parent is already registered
		val newParentId = parentId(parent)
		val newParentNode = nodeForId(newParentId)
		
		// If the child (master) already exists, attaches it to the new parent
		if (ids.contains(child))
		{
			val oldChildId = ids(child)
			val childIndex = oldChildId.last
			val childNode = graph(childIndex)
			
			// Updates all child ids
			childNode.foreach { n => ids(n.content) = newParentId + ids(n.content) }
			
			// Removes the child from master nodes and attaches it to the new parent
			graph -= childIndex
			newParentNode.connect(childNode, childIndex)
		}
		// Otherwise adds the child as a new id + node
		else
		{
			val newChildId = newParentId + indexCounter.next()
			ids(child) = newChildId
			newParentNode.connect(new Node(child), newChildId.last)
		}
	}
	
	private def graphForId(id: StackId) = graph(id.masterId)
	
	private def nodeForId(id: StackId) =
	{
		if (id.isMasterId)
			graphForId(id)
		else
			(graphForId(id) / id.parts.drop(1)).head
	}
	
	private def parentId(item: Stackable) =
	{
		val existing = ids.get(item)
		if (existing.isDefined)
			existing.get
		else
		{
			// If there isn't an id, creates one
			val newId = StackId.root(indexCounter.next())
			// Adds the new id to id map as well as graph
			ids += (item -> newId)
			graph(newId.masterId) = new Node(item)
			newId
		}
	}
}

private object StackId
{
	def root(index: Int) = StackId(Vector(index))
}

private case class StackId(parts: Vector[Int])
{
	def head = parts.head
	
	def last = parts.last
	
	def tail = if (parts.size < 2) None else Some(StackId(parts.tail))
	
	def isMasterId = parts.size < 2
	
	def masterId = parts.head
	
	def parentId = if (parts.size == 1) None else Some(StackId(parts.dropRight(1)))
	
	def apply(index: Int) = parts(index)
	
	def +(index: Int) = StackId(parts :+ index)
	
	def +(other: StackId) = StackId(parts ++ other.parts)
	
	def isChildOf(other: StackId) =
	{
		if (parts.size <= other.parts.size)
			false
		else
			other.parts.indices.forall { i => apply(i) == other(i) }
	}
	
	def dropUntil(index: Int) = StackId(parts.dropWhile { _ != index })
}
