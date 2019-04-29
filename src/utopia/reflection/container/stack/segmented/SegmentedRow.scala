package utopia.reflection.container.stack.segmented

import utopia.genesis.color.Color
import utopia.genesis.shape.Axis2D
import utopia.genesis.shape.shape2D.Size
import utopia.reflection.component.{AwtComponentRelated, StackableWrapper, SwingComponentRelated}
import utopia.reflection.container.stack.Stack.AwtStackable
import utopia.reflection.container.stack.segmented.SegmentedRow.RowSegment
import utopia.reflection.container.stack.{Stack, StackLayout}
import utopia.reflection.shape.StackLength

object SegmentedRow
{
	type RowSegment = StackableWrapper with AwtComponentRelated
	
	/**
	  * Creates a new row that becomes a part of the specified group
	  * @param group A group
	  * @param layout Row layout
	  * @param margin Margin between row components
	  * @param cap Cap at each end of the row
	  * @return A new row, already registered to the specified group
	  */
	def partOfGroup[C <: AwtStackable](group: SegmentedGroup, layout: StackLayout, margin: StackLength, cap: StackLength) =
	{
		val row = new SegmentedRow[C](group.direction, layout, margin, cap, group)
		group.register(row)
		row
	}
	
	/**
	  * Creates a new row with items
	  * @param direction Row direction
	  * @param layout Row layout
	  * @param margin Margin between row components
	  * @param cap Cap at each end of the row
	  * @param items The items for the new row
	  * @return A new row
	  */
	def withItems[C <: AwtStackable](direction: Axis2D, layout: StackLayout, margin: StackLength, cap: StackLength,
									 master: Segmented, items: TraversableOnce[C]) =
	{
		val row = new SegmentedRow[C](direction, layout, margin, cap, master)
		row ++= items
		row
	}
	
	/**
	  * Creates a new row that becomes a part of the specified group
	  * @param group A group
	  * @param layout Row layout
	  * @param margin Margin between row components
	  * @param cap Cap at each end of the row
	  * @param items The items for the new row
	  * @return A new row, already registered to the specified group
	  */
	def partOfGroupWithItems[C <: AwtStackable](group: SegmentedGroup, layout: StackLayout, margin: StackLength,
												cap: StackLength, items: TraversableOnce[C]) =
	{
		val row = withItems(group.direction, layout, margin, cap, group, items)
		group.register(row)
		row
	}
}

/**
  * Segmented rows are basically stack panels where each item is a segment and the lengths of the segments can be
  * matched from another segmented source. If you wish to make this row part of that source, use SegmentedGroup and
  * register this row as part of it
  * @author Mikko Hilpinen
  * @since 28.4.2019, v1+
  */
class SegmentedRow[C <: AwtStackable](override val direction: Axis2D, layout: StackLayout, margin: StackLength,
									  cap: StackLength, val master: Segmented)
	extends SegmentedRowLike[C, RowSegment] with SwingComponentRelated
{
	// ATTRIBUTES	-----------------
	
	override protected val stack = new Stack[RowSegment](direction, layout, margin, cap)
	
	
	// INITIAL CODE	----------------
	
	// TODO: Why is the stack's resizelistener not being called but this one is?
	// TODO: + this doesn't set bounds correctly (segment setSize not getting called either)
	addResizeListener(updateLayout())
	
	
	// IMPLEMENTED	-----------------
	
	override protected def addSegmentToStack(segment: Segment) = stack += new AwtComponentSegment(segment)
	
	override protected def removeSegmentFromStack(segment: Segment) = stack.filterNot { _.component == segment.item.component }
	
	override def component = stack.component
	
	
	
	
	// NESTED CLASSES	-----------
	
	override def background_=(color: Color) = super[SegmentedRowLike].background_=(color)
	
	private class AwtComponentSegment(val segment: Segment) extends StackableWrapper with AwtComponentRelated
	{
		override protected def wrapped = segment
		
		override def component = segment.item.component
	}
}