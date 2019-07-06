package utopia.reflection.controller.data

import java.awt.event.KeyEvent

import utopia.genesis.event.{KeyStateEvent, MouseButtonStateEvent, MouseEvent}
import utopia.genesis.handling.{KeyStateListener, MouseButtonStateListener}
import utopia.genesis.shape.shape2D.Bounds
import utopia.genesis.util.Drawer
import utopia.inception.handling.immutable.Handleable
import utopia.reflection.component.Refreshable
import utopia.reflection.component.drawing.{CustomDrawable, CustomDrawer}
import utopia.reflection.component.stack.Stackable
import utopia.reflection.container.stack.StackLike

/**
  * This class handles content and selection in a stack
  * @author Mikko Hilpinen
  * @since 5.6.2019, v1+
  */
class StackSelectionManager[A, C <: Stackable with Refreshable[A]](stack: StackLike[C] with CustomDrawable, makeItem: A => C,
																   private val selectionAreaDrawer: CustomDrawer)
	extends StackContentManager[A, C](stack, makeItem) with SelectionManager[A, C]
{
	// INITIAL CODE	--------------------
	
	stack.addCustomDrawer(new SelectionDrawer)
	
	
	// IMPLEMENTED	--------------------
	
	override protected def updateSelectionDisplay(oldSelected: Option[C], newSelected: Option[C]) = stack.repaint()
	
	override protected def finalizeRefresh() =
	{
		super.finalizeRefresh()
		stack.repaint()
	}
	
	
	// OTHER	------------------------
	
	/**
	  * Enables mouse button state handling for the stack (selects the clicked item)
	  * @param consumeEvents Whether mouse events should be consumed (default = true)
	  */
	def enableMouseHandling(consumeEvents: Boolean = true) = stack.addMouseButtonListener(new MouseHandler(consumeEvents))
	/**
	  * Enables key state handling for the stack (allows selection change with up & down arrows)
	  */
	def enableKeyHandling() = stack.addKeyStateListener(new KeyHandler)
	
	
	// NESTED CLASSES	----------------
	
	private class SelectionDrawer extends CustomDrawer
	{
		override def drawLevel = selectionAreaDrawer.drawLevel
		
		override def draw(drawer: Drawer, bounds: Bounds) =
		{
			// Draws the selected area using another custom drawer
			selectedDisplay.flatMap(stack.areaOf).foreach { area => selectionAreaDrawer.draw(drawer,
				area.translated(bounds.position)) }
		}
	}
	
	private class MouseHandler(val consumeEvents: Boolean) extends MouseButtonStateListener with Handleable
	{
		// Only considers left mouse button presses inside stack bounds
		override def mouseButtonStateEventFilter = MouseButtonStateEvent.leftPressedFilter &&
			MouseEvent.isOverAreaFilter(stack.bounds)
		
		override def onMouseButtonState(event: MouseButtonStateEvent) =
		{
			val nearest = stack.itemNearestTo(event.mousePosition - stack.position)
			nearest.foreach(handleMouseClick)
			consumeEvents && nearest.isDefined
		}
	}
	
	private class KeyHandler extends KeyStateListener with Handleable
	{
		// Only checks for up & down key presses
		override def keyStateEventFilter = KeyStateEvent.wasPressedFilter &&
			KeyStateEvent.keysFilter(KeyEvent.VK_UP, KeyEvent.VK_DOWN)
		
		override def onKeyState(event: KeyStateEvent) = handleKeyPress(event.index)
	}
}
