package utopia.reflection.container

import utopia.flow.util.Counter
import utopia.reflection.component.Stackable

import scala.collection.immutable.HashMap

class StackHierarchyManager
{
	// ATTRIBUTES	---------------------
	
	private val indexCounter = new Counter(1)
	private var ids = HashMap[Stackable, StackId]()
	
	
	// OTHER	-------------------------
	
	def unregister(item: Stackable) =
	{
		// Removes the provided item and each child
		if (ids.contains(item))
		{
			val itemId = ids(item)
			ids -= item
			ids.filterNot { _._2.isChildOf(itemId) }
		}
	}
	
	def registerConnection(parent: Stackable, child: Stackable) =
	{
		// Makes sure that the parent is already registered
		val newParentId = parentId(parent)
		
		// TODO: Implement with trees instead
	}
	
	private def parentForId(id: StackId) = id.parentId.flatMap(pid => ids.find { _._2 == pid })
	
	private def parentId(item: Stackable) =
	{
		val existing = ids.get(item)
		if (existing.isDefined)
			existing.get
		else
		{
			// If there isn't an id, creates one
			val newId = StackId.root(indexCounter.next())
			ids += (item -> newId)
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
	def parentId = if (parts.size == 1) None else Some(StackId(parts.dropRight(1)))
	
	def apply(index: Int) = parts(index)
	
	def +(index: Int) = StackId(parts :+ index)
	
	def isChildOf(other: StackId) =
	{
		if (parts.size <= other.parts.size)
			false
		else
			other.parts.indices.forall { i => apply(i) == other(i) }
	}
}
