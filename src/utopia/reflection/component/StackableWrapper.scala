package utopia.reflection.component

/**
  * This wrapper wraps a stackable, providing full stackable interface itself
  * @author Mikko Hilpinen
  * @since 28.4.2019, v1+
  */
trait StackableWrapper extends ComponentWrapper with Stackable
{
	// ABSTRACT	---------------------
	
	override protected def wrapped: Stackable
	
	
	// IMPLEMENTED	-----------------
	
	override def updateLayout() = wrapped.updateLayout()
	
	override def stackSize = wrapped.stackSize
	
	override def resetCachedSize() = wrapped.resetCachedSize()
}