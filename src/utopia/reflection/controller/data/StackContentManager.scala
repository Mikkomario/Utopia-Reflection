package utopia.reflection.controller.data

import utopia.reflection.component.Refreshable
import utopia.reflection.component.stack.Stackable
import utopia.reflection.container.stack.StackLike

/**
  * This content manager handles content changes for a Stack
  * @author Mikko Hilpinen
  * @since 5.6.2019, v1+
  * @tparam A The type of content displayed in the stack
  * @tparam C The type of display where a single item is displayed
  * @param stack The stack managed through this manager
  * @param makeItem A function for producing new displays
  */
class StackContentManager[A, C <: Stackable with Refreshable[A]](private val stack: StackLike[C],
																 private val makeItem: A => C) extends ContentManager[A, C]
{
	// IMPLEMENTED	-----------------------
	
	override def displays = stack.components
	
	override protected def addDisplaysFor(values: Vector[A]) = stack ++= values.map(makeItem)
	
	override protected def dropDisplays(dropped: Vector[C]) = stack --= dropped
	
	override protected def finalizeRefresh() = stack.revalidate()
}