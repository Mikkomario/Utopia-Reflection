package utopia.reflection.controller.data

import utopia.flow.util.CollectionExtensions._
import utopia.reflection.component.Refreshable

/**
  * ContentManagers update content on a component
  * @author Mikko Hilpinen
  * @since 22.5.2019, v1+
  */
trait ContentManager[A, C <: Refreshable[A]] extends Refreshable[Vector[A]]
{
	// ABSTRACT	----------------------
	
	/**
	  * @return The currently used displays
	  */
	def displays: Vector[C]
	
	/**
	  * Adds new displays for new values
	  * @param values New values that need to be displayed
	  */
	protected def addDisplaysFor(values: Vector[A]): Unit
	
	/**
	  * Removes unnecessary displays
	  * @param dropped The displays to be dropped
	  */
	protected def dropDisplays(dropped: Vector[C])
	
	/**
	  * This method will be called at the end of each refresh
	  */
	protected def finalizeRefresh(): Unit
	
	
	// IMPLEMENTED	------------------
	
	override def content = displays.map { _.content }
	
	override def content_=(newContent: Vector[A]) =
	{
		// Existing rows are updated
		displays.foreachWith(newContent) { _.content = _ }
		
		val size = displays.size
		
		// Unnecessary rows are removed and new rows may be added
		if (size > newContent.size)
			dropDisplays(displays.drop(newContent.size))
		else if (size < newContent.size)
			addDisplaysFor(newContent.drop(size))
		
		finalizeRefresh()
	}
	
	
	// OTHER	--------------------
	
	/**
	  * Finds a display currently showing provided element
	  * @param item A searched item
	  * @param equals A function for testing equality between contents
	  * @tparam B Type of tested item
	  * @return The display currently showing the provided item. None if no such display was found.
	  */
	def displayFor[B](item: B, equals: (A, B) => Boolean) = displays.find { d => equals(d.content, item) }
	
	/**
	  * Finds a display currently showing provided element (uses equals to find the element)
	  * @param item A searched item
	  * @return The display currently showing the provided item. None if no such display was found.
	  */
	def displayFor(item: Any): Option[C] = displayFor[Any](item, _ == _)
}
