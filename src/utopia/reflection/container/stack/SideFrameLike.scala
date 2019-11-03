package utopia.reflection.container.stack

import utopia.genesis.shape.shape2D.{Direction2D, Point, Size}
import utopia.reflection.component.ComponentWrapper
import utopia.reflection.component.stack.{CachingStackable, Stackable}
import utopia.reflection.container.Container
import utopia.reflection.shape.StackSize

/**
 * Holds a single component, but places it at a single side of this component
 * @author Mikko Hilpinen
 * @since 3.11.2019, v1+
 */
trait SideFrameLike[C <: Stackable] extends SingleStackContainer[C] with ComponentWrapper with CachingStackable
{
	// ABSTRACT	-----------------------
	
	/**
	 * @return The side to which the content is placed
	 */
	def contentSide: Direction2D
	
	protected def container: Container[C]
	
	override protected def wrapped = container
	
	override protected def updateVisibility(visible: Boolean) = super[CachingStackable].isVisible_=(visible)
	
	override def components = container.components
	
	// Places component at one side of the area and also fits it
	override def updateLayout() =
	{
		content.foreach { c =>
			// Handles more strict axis first
			val freeAxis = contentSide.axis
			val strictAxis = freeAxis.perpendicular
			
			val strictLength = lengthAlong(strictAxis)
			
			// Then handles the more free axis
			val myLength = lengthAlong(freeAxis)
			val optimalLength = c.stackSize.along(freeAxis).optimal
			
			val freeLength = if (myLength < optimalLength) myLength else optimalLength
			
			c.size = Size(freeLength, strictLength, freeAxis)
			
			// Positions the component as well
			val freePosition = if (contentSide.isPositiveDirection) myLength - freeLength else 0
			c.position = Point(freePosition, 0, freeAxis)
		}
	}
	
	// Uses stack sizes of underlying content, except extends targeted axis
	override protected def calculatedStackSize = content.map { _.stackSize.withLowPriorityFor(contentSide.axis)}.getOrElse(StackSize.any)
}
