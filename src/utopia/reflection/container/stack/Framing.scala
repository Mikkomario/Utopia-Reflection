package utopia.reflection.container.stack

import utopia.genesis.shape.shape2D.{Bounds, Point, Size}
import utopia.genesis.shape.{Axis2D, X, Y}
import utopia.reflection.component.{JWrapper, Stackable}
import utopia.reflection.container.Panel
import utopia.reflection.shape.{StackLength, StackSize}

/**
  * Framings are containers that present a component with scaling 'frames', like a painting
  * @author Mikko Hilpinen
  * @since 26.4.2019, v1+
  */
class Framing[C <: Stackable](initialComponent: C, val margins: StackSize) extends SingleStackContainer[C] with JWrapper
{
	// ATTRIBUTES	--------------------
	
	private val panel = new Panel[C]()
	private var content: Option[C] = Some(initialComponent)
	
	
	// INITIAL CODE	--------------------
	
	set(initialComponent)
	// Each time Framing size changes, changes content size too
	addResizeListener(updateLayout())
	
	
	// IMPLEMENTED	--------------------
	
	override def component = panel.component
	
	override def components = panel.components
	
	override protected def add(component: C) =
	{
		panel += component
		content = Some(component)
	}
	
	override protected def remove(component: C) =
	{
		panel -= component
		content = None
	}
	
	override def updateLayout() =
	{
		if (content.isDefined)
		{
			// Repositions and resizes content
			val layout = Axis2D.values.map
			{
				axis =>
					
					// Calculates lengths
					val (contentLength, marginLength) = lengthsFor(axis)
					// Margin cannot go below 0
					if (marginLength < 0)
						axis -> (0, lengthAlong(axis).toInt)
					else
						axis -> (marginLength / 2, contentLength)
			}.toMap
			
			val position = Point(layout(X)._1, layout(Y)._1)
			val size = Size(layout(X)._2, layout(Y)._2)
			
			content.get.bounds = Bounds(position, size)
		}
	}
	
	override protected def calculatedStackSize = content.map { _.stackSize + margins * 2 } getOrElse StackSize.any.withLowPriority
	
	
	// OTHER	-----------------------
	
	// Returns content final length -> margin final length
	private def lengthsFor(axis: Axis2D) =
	{
		val myLength = lengthAlong(axis)
		val contentLength = content.get.stackSize.along(axis)
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
