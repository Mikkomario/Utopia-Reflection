package utopia.reflection.container.swing.window

import java.awt.event.{WindowEvent, WindowFocusListener}

import utopia.reflection.localization.LocalString._
import utopia.genesis.handling.mutable.ActorHandler
import utopia.genesis.shape.shape2D.{Point, Size}
import utopia.reflection.component.ComponentLike
import utopia.reflection.component.stack.Stackable
import utopia.reflection.component.swing.AwtComponentRelated
import utopia.reflection.container.swing.AwtContainerRelated
import utopia.reflection.container.swing.window.WindowResizePolicy.Program
import utopia.reflection.util.Screen

/**
  * Used for converting components to pop-ups
  * @author Mikko Hilpinen
  * @since 2.8.2019, v1+
  */
object Popup
{
	/**
	  * Creates a new popup window
	  * @param context Component that wishes to display the popup
	  * @param content Popup contents (stackabe container)
	  * @param actorHandler An actorhandler that will supply the pop-up with action events. These are required in
	  *                     mouse- and keyboard event generating.
	  * @param getTopLeft A function for calculating the new top left corner of the pop-up within context component.
	  *                   Provided parameters are: a) Context size and b) pop-up window size
	  * @param hideWhenFocusLost Whether the pop-up should be hidden when it loses focus (default = true)
	  * @tparam C Type of displayed item
	  * @return Newly created and pop-up window
	  */
	def apply[C <: AwtContainerRelated with Stackable](context: ComponentLike with AwtComponentRelated, content: C,
													   actorHandler: ActorHandler, getTopLeft: (Size, Size) => Point,
													   hideWhenFocusLost: Boolean = true) =
	{
		// If context isn't in a window (which it should), has to use a Frame instead of a dialog
		val owner = context.parentWindow
		val windowTitle = "Popup".local("en").localizationSkipped
		val newWindow = owner.map { o => new Dialog(o, content, windowTitle, Program, borderless = true) }.getOrElse(
			Frame.windowed(content, windowTitle, Program, borderless = true))
		
		// Calculates the absolute target position
		val newPosition = context.absolutePosition + getTopLeft(context.size, newWindow.size)
		
		// Sets pop-up position, but makes sure it fits into screen
		val maxPosition = (Screen.size - newWindow.size).toPoint
		newWindow.position = newPosition topLeft maxPosition
		
		// Activates focus listening, if necessary
		if (hideWhenFocusLost)
			newWindow.component.addWindowFocusListener(new HideOnFocusLostListener(newWindow))
		
		newWindow.startEventGenerators(actorHandler)
		newWindow.setToCloseOnEsc()
		newWindow
	}
	
	
	// NESTED	----------------------
	
	private class HideOnFocusLostListener(popup: Window[_]) extends WindowFocusListener
	{
		override def windowGainedFocus(e: WindowEvent) = Unit
		
		override def windowLostFocus(e: WindowEvent) =
		{
			popup.component.removeWindowFocusListener(this)
			popup.close()
		}
	}
}
