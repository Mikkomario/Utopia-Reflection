package utopia.reflection.component

import javax.swing.{JComponent, JLabel, SwingUtilities}
import utopia.flow.collection.VolatileList
import utopia.genesis.color.Color
import utopia.genesis.event.{MouseButton, MouseButtonStateEvent, MouseMoveEvent}
import utopia.genesis.handling.{MouseButtonStateListener, MouseMoveListener}
import utopia.inception.handling.HandlerType
import utopia.reflection.component.Alignment.Center
import utopia.reflection.localization.LocalizedString
import utopia.reflection.shape.StackSize
import utopia.reflection.text.Font

/**
  * Buttons are used for user interaction
  * @author Mikko Hilpinen
  * @since 25.4.2019, v1+
  */
class Button(override val text: LocalizedString, override val font: Font, color: Color, override val margins: StackSize,
			 val action: () => Unit) extends TextComponent with JWrapper
{
	// ATTRIBUTES	------------------
	
	private val label = new JLabel()
	private val colorHistory = VolatileList(color)
	
	
	// INITIAL CODE	------------------
	
	label.setFont(font.toAwt)
	setHandCursor()
	textColor = Color.textBlack
	label.setText(text.string)
	background = color
	label.setHorizontalAlignment(Alignment.Center.toSwingAlignment)
	
	{
		// Adds mouse handling
		val listener = new ButtonMouseListener()
		addMouseMoveListener(listener)
		addMouseButtonListener(listener)
	}
	
	
	// COMPUTED	----------------------
	
	/**
	  * @return Whether this button is currently enabled
	  */
	def isEnabled = label.isEnabled
	/**
	  * @param newEnabled Whether this button should be enabled
	  */
	def isEnabled_=(newEnabled: Boolean) =
	{
		if (newEnabled != isEnabled)
		{
			// Adds transparency when disabled
			if (isEnabled)
				pushColor(background.withAlpha(0.55))
			else
				returnColor()
		}
		label.setEnabled(newEnabled)
	}
	
	
	// IMPLEMENTED	------------------
	
	override def alignment = Center
	
	override def hasMinWidth = true
	
	override def component: JComponent = label
	
	override def updateLayout() = Unit
	
	override def toString = s"Button($text)"
	
	
	// OTHER	----------------------
	
	/**
	  * Triggers this button's action. Same as if the user clicked this button (only works for enabled buttons)
	  */
	def trigger() = if (isEnabled) action()
	
	private def pushColor(newColor: Color) =
	{
		colorHistory +:= background
		SwingUtilities.invokeLater { () => background = newColor }
	}
	
	private def returnColor() =
	{
		SwingUtilities.invokeLater { () => colorHistory.pop().foreach { background = _ } }
	}
	
	
	// NESTED CLASSES	--------------
	
	private class ButtonMouseListener extends MouseButtonStateListener with MouseMoveListener
	{
		// ATTRIBUTES	--------------
		
		var isDown = false
		
		
		// IMPLEMENTED	--------------
		
		// Only listens to left mouse button presses & releases
		override val mouseButtonStateEventFilter = MouseButtonStateEvent.buttonFilter(MouseButton.Left)
		
		// Listens to mouse enters & exits
		override def mouseMoveEventFilter = MouseMoveEvent.enterAreaFilter(bounds) ||
			MouseMoveEvent.exitedAreaFilter(bounds)
		
		// On left mouse within bounds, brightens color and remembers, on release, returns
		override def onMouseButtonState(event: MouseButtonStateEvent) =
		{
			if (isDown)
			{
				if (event.wasReleased)
				{
					isDown = false
					trigger()
					returnColor()
					true
				}
				else
					false
			}
			else if (event.isOverArea(bounds))
			{
				isDown = true
				pushColor(background.lightened(1.5))
				true
			}
			else
				false
		}
		
		// When mouse enters, brightens, when mouse leaves, returns
		override def onMouseMove(event: MouseMoveEvent) =
		{
			if (event.isOverArea(bounds))
				pushColor(background.lightened(1.5))
			else
				returnColor()
		}
		
		override def parent = None
		
		override def allowsHandlingFrom(handlerType: HandlerType) = isEnabled
	}
}
