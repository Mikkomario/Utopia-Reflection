package utopia.reflection.container.stack.segmented

import utopia.genesis.shape.Axis2D
import utopia.reflection.component.{JWrapper, Stackable}
import utopia.reflection.container.stack.{MultiStackContainer, Stack, StackLayout}
import utopia.reflection.shape.StackLength

/**
  * Segmented rows are basically stack panels where each item is a segment and the lengths of the segments can be
  * matched from another segmented source. If you wish to make this row part of that source, use SegmentedGroup and
  * register this row as part of it
  * @author Mikko Hilpinen
  * @since 28.4.2019, v1+
  */
class SegmentedRow(override val direction: Axis2D, layout: StackLayout, margin: StackLength, cap: StackLength,
				   val master: Segmented)
	extends MultiStackContainer[Stackable] with Segmented with JWrapper
{
	// ATTRIBUTES	-----------------
	
	private val stack = new Stack(direction, layout, margin, cap)
	
	
	// IMPLEMENTED	-----------------
	
	override def segmentCount = stack.count
	
	/**
	  * Finds the natrual length of a segment at the specified index
	  * @param index Segment index
	  * @return The natural length of the segment at the specified index. None if this segment doesn't have a segment
	  *         at the index or if the length couldn't be calculated
	  */
	override def naturalLengthForSegment(index: Int) = ???
	
	override def component = stack.component
	
	override def updateLayout() =
	{
		stack.updateLayout()
		informSegmentChanged(this)
		// TODO: Inform segment changed on stack size reset instead?
	}
	
	override protected def calculatedStackSize = stack.stackSize
	
	// TODO: Perhaps return the unwrapped versions
	override def components = stack.components
	
	/**
	  * Adds a new item to this container
	  */
	override protected def add(component: Stackable) = ???
	
	/**
	  * Removes an item from this container
	  */
	override protected def remove(component: Stackable) = ???
}
