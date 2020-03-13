package utopia.reflection.component.swing

import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.genesis.color.Color
import utopia.genesis.shape.Axis.X
import utopia.genesis.shape.shape2D.Bounds
import utopia.genesis.util.Drawer
import utopia.reflection.component.drawing.{CustomDrawableWrapper, CustomDrawer}
import utopia.reflection.component.drawing.DrawLevel.Normal
import utopia.reflection.component.stack.StackLeaf
import utopia.reflection.component.swing.label.EmptyLabel
import utopia.reflection.shape.StackSize
import utopia.reflection.util.ComponentContext

object ProgressBar
{
	/**
	  * Creates a new progress bar using contextual information
	  * @param stackSize Progress bar size
	  * @param data Tracked data
	  * @param calculateProgress A function for calculating progress
	  * @param context Component creation context
	  * @tparam A Tracked item type
	  * @return A new progress bar
	  */
	def contextual[A](stackSize: StackSize, data: PointerWithEvents[A])(calculateProgress: A => Double)
					 (implicit context: ComponentContext) = new ProgressBar[A](stackSize, context.barBackground,
		context.highlightColor, data, calculateProgress)
}

/**
  * Used for displaying progress of some longer operation
  * @author Mikko Hilpinen
  * @since 1.8.2019, v1+
  */
class ProgressBar[A](override val stackSize: StackSize, val backgroundColor: Color, val barColor: Color,
					 val data: PointerWithEvents[A], val calculateProgress: A => Double)
	extends AwtComponentWrapperWrapper with CustomDrawableWrapper with StackLeaf
{
	// ATTRIBUTES	---------------------
	
	private val label = new EmptyLabel()
	private var _progress = calculateProgress(data.get) max 0 min 1
	
	
	// INITIAL CODE	---------------------
	
	label.addCustomDrawer(new ProgressDrawer)
	data.addListener { event =>
		_progress = calculateProgress(event.newValue) max 0 min 1
		repaint()
	}
	
	
	// COMPUTED	--------------------------
	
	/**
	  * @return Current progress of the tracked data
	  */
	def progress = _progress
	
	
	// IMPLEMENTED	----------------------
	
	override def updateLayout() = Unit
	
	override def resetCachedSize() = Unit
	
	override def stackId = hashCode()
	
	override protected def wrapped = label
	
	override def drawable = label
	
	
	// NESTED	--------------------------
	
	private class ProgressDrawer extends CustomDrawer
	{
		override def drawLevel = Normal
		
		override def draw(drawer: Drawer, bounds: Bounds) =
		{
			// Draws background first
			val barBounds = if (bounds.width > bounds.height) bounds else
			{
				val height = bounds.width
				val translation = (bounds.height - height) / 2
				Bounds(bounds.position.plusY(translation), bounds.size.withHeight(height))
			}
			
			val rounded = barBounds.toRoundedRectangle(1)
			
			if (_progress < 1)
				drawer.onlyFill(backgroundColor).draw(rounded)
			
			// Next draws the progress
			if (_progress > 0)
			{
				if (_progress == 1)
					drawer.onlyFill(barColor).draw(rounded)
				else
				{
					val partial = barBounds.mapSize { _ * (_progress, X) }
					drawer.clippedTo(rounded).onlyFill(barColor).draw(partial)
				}
			}
		}
	}
}
