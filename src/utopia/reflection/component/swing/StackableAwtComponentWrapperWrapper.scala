package utopia.reflection.component.swing

import utopia.reflection.component.StackableWrapper

/**
  * This wrapper wraps a stackable wrapper
  * @author Mikko Hilpinen
  * @since 28.4.2019, v1+
  */
trait StackableAwtComponentWrapperWrapper extends AwtComponentWrapperWrapper with StackableWrapper
{
	// ABSTRACT	---------------------
	
	override def wrapped: StackableAwtComponentWrapper
}
