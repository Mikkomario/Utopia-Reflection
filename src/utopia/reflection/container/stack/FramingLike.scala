package utopia.reflection.container.stack

import utopia.genesis.shape.shape2D.{Bounds, Point, Size}
import utopia.genesis.shape.{Axis2D, X, Y}
import utopia.reflection.component.stack.{CachingStackable, Stackable}
import utopia.reflection.component.ComponentWrapper
import utopia.reflection.container.Container
import utopia.reflection.shape.{StackLength, StackSize}

/**
  * Framings are containers that present a component with scaling 'frames', like a painting
  * @author Mikko Hilpinen
  * @since 26.4.2019, v1+
  */
trait FramingLike[C <: Stackable] extends SingleStackContainer[C] with ComponentWrapper with CachingStackable
{
	// ABSTRACT	-----------------------
	
	/**
	  * @return The margins around the component in this container
	  */
	def margins: StackSize
	
	/**
	  * @return The underlying container of this framing
	  */
	protected def container: Container[C]
	
	
	// IMPLEMENTED	--------------------
	
	override protected def wrapped = container
	
	override def isVisible_=(isVisible: Boolean) = super[CachingStackable].isVisible_=(isVisible)
	
	override protected def updateVisibility(visible: Boolean) = super[ComponentWrapper].isVisible_=(visible)
	
	override def components = container.components
	
	override def updateLayout() =
	{
		val c = content
		if (c.isDefined)
		{
			// Repositions and resizes content
			val layout = Axis2D.values.map
			{
				axis =>
					
					// Calculates lengths
					val (contentLength, marginLength) = lengthsFor(c.get, axis)
					// Margin cannot go below 0
					if (marginLength < 0)
						axis -> (0, lengthAlong(axis).toInt)
					else
						axis -> (marginLength / 2, contentLength)
			}.toMap
			
			val position = Point(layout(X)._1, layout(Y)._1)
			val size = Size(layout(X)._2, layout(Y)._2)
			
			c.get.bounds = Bounds(position, size)
		}
	}
	
	override protected def calculatedStackSize = content.map { _.stackSize + margins * 2 } getOrElse StackSize.any.withLowPriority
	
	
	// OTHER	-----------------------
	
	// Returns content final length -> margin final length
	private def lengthsFor(content: C, axis: Axis2D) =
	{
		val myLength = lengthAlong(axis)
		val contentLength = content.stackSize.along(axis)
		val margin = margins.along(axis) * 2
		
		val totalAdjustment = myLength - (contentLength.optimal + margin.optimal)
		
		// Sometimes adjustment isn't necessary
		if (totalAdjustment == 0)
		{
			contentLength.optimal -> margin.optimal
		}
		else
		{
			// Either enlargens or shrinks the components
			if (totalAdjustment > 0)
			{
				val (contentAdjust, marginAdjust) = adjustmentsFor(contentLength, margin, totalAdjustment,
					l => l.max.map { _ - l.optimal })
				
				(contentLength.optimal + contentAdjust) -> (margin.optimal + marginAdjust)
			}
			else
			{
				val (contentAdjust, marginAdjust) = adjustmentsFor(contentLength, margin, totalAdjustment,
					l => Some(l.optimal - l.min))
				
				(contentLength.optimal - contentAdjust) -> (margin.optimal - marginAdjust)
			}
		}
	}
	
	// Returns content adjustment -> margin adjustment (with correct multiplier)
	private def adjustmentsFor(contentLength: StackLength, margin: StackLength, totalAdjustment: Double,
					   getMaxAdjust: StackLength => Option[Int]): (Int, Int) =
	{
		// Determines the default split based on length priorities
		val (defaultContentAdjust, defaultMarginAdjust) =
		{
			if (contentLength.isLowPriority == margin.isLowPriority)
				(totalAdjustment.abs / 2) -> (totalAdjustment.abs / 2)
			else if (contentLength.isLowPriority)
				totalAdjustment.abs -> 0.0
			else
				0.0 -> totalAdjustment
		}
		
		val contentMaxAdjust = getMaxAdjust(contentLength)
		
		// If content maximum is reached, puts the remaining adjustment to margin
		if (contentMaxAdjust.exists { defaultContentAdjust >= _ })
		{
			val remainsAfterMaxed = defaultContentAdjust - contentMaxAdjust.get
			contentMaxAdjust.get -> (defaultMarginAdjust + remainsAfterMaxed).toInt
		}
		else
		{
			val marginMaxAdjust = getMaxAdjust(margin)
			
			// If margin maximum is reached, puts the remaining adjustment to component
			// (until maxed, after which to margin anyway)
			if (marginMaxAdjust.exists { defaultMarginAdjust >= _ })
			{
				val remainsAfterMarginMaxed = defaultMarginAdjust - marginMaxAdjust.get
				val proposedContentAdjust = defaultContentAdjust + remainsAfterMarginMaxed
				
				if (contentMaxAdjust.exists { proposedContentAdjust > _ })
				{
					val remainsAfterComponentMaxed = proposedContentAdjust - contentMaxAdjust.get
					contentMaxAdjust.get -> (marginMaxAdjust.get + remainsAfterComponentMaxed).toInt
				}
				else
				{
					proposedContentAdjust.toInt -> marginMaxAdjust.get
				}
			}
			// If neither is reached, adjusts both equally
			else
			{
				defaultContentAdjust.toInt -> defaultMarginAdjust.toInt
			}
		}
	}
}
