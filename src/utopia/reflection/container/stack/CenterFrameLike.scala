package utopia.reflection.container.stack

import utopia.genesis.shape.shape2D.Bounds
import utopia.reflection.component.ComponentWrapper
import utopia.reflection.component.stack.{CachingStackable, Stackable}
import utopia.reflection.container.Container
import utopia.reflection.shape.StackSize

/**
 * This container "frames" a component by placing it at the center of this container / frame. This can be used when
 * the component would otherwise be scaled above desired measurements, like in certain stacks or segments
 * @author Mikko Hilpinen
 * @since 7.11.2019, v1+
 */
@deprecated("Replaced with AlignFrameLike", "v1")
trait CenterFrameLike[C <: Stackable] extends SingleStackContainer[C] with ComponentWrapper with CachingStackable
{
	// ABSTRACT	-----------------------
	
	protected def container: Container[C]
	
	
	// IMPLEMENTED	------------------
	
	override protected def wrapped = container
	
	override protected def updateVisibility(visible: Boolean) = super[CachingStackable].isVisible_=(visible)
	
	override def components = container.components
	
	// Places component at one side of the area and also fits it
	override def updateLayout() =
	{
		content.foreach { c =>
			// Content size is set to optimal, if it fits
			val mySize = size
			val newContentSize = c.stackSize.optimal.fittedInto(mySize)
			
			// Content is placed at the center of this container
			val newContentPosition = (mySize - newContentSize).toPoint / 2
			
			// Applies changes
			c.bounds = Bounds(newContentPosition, newContentSize)
		}
	}
	
	// Uses stack sizes of underlying content, except extends targeted axis
	override protected def calculatedStackSize = content.map { _.stackSize.withNoMax }.getOrElse(StackSize.any)
}
