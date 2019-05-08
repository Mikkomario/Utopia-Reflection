package utopia.reflection.component.swing.label

import java.awt.event.{FocusEvent, FocusListener, KeyEvent}

import javax.swing.SwingUtilities
import utopia.flow.collection.VolatileList
import utopia.genesis.color.Color
import utopia.genesis.event.{KeyStateEvent, MouseButton, MouseButtonStateEvent, MouseMoveEvent}
import utopia.genesis.handling.{KeyStateListener, MouseButtonStateListener, MouseMoveListener}
import utopia.inception.handling.HandlerType
import utopia.reflection.component.Alignment
import utopia.reflection.component.Alignment.Center
import utopia.reflection.component.swing.AwtTextComponentWrapper
import utopia.reflection.localization.LocalizedString
import utopia.reflection.shape.{Border, StackSize}
import utopia.reflection.text.Font

/**
  * Buttons are used for user interaction
  * @author Mikko Hilpinen
  * @since 25.4.2019, v1+
  */
class Button(override val text: LocalizedString, override val font: Font, color: Color, override val margins: StackSize,
			 val borderWidth: Double, val action: () => Unit) extends Label with AwtTextComponentWrapper
{
	// ATTRIBUTES	------------------
	
	private val colorHistory = VolatileList(color)
	private var _isInFocus = false
	
	
	// INITIAL CODE	------------------
	
	label.setFont(font.toAwt)
	label.setFocusable(true)
	setHandCursor()
	label.setText(text.string)
	background = color
	label.setHorizontalAlignment(Alignment.Center.toSwingAlignment)
	
	{
		// Adds mouse handling
		val listener = new ButtonMouseListener()
		addMouseMoveListener(listener)
		addMouseButtonListener(listener)
		
		// Adds key listening
		addKeyStateListener(new ButtonKeyListener())
		
		// Adds focus listening
		label.addFocusListener(new ButtonFocusListener())
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
			// TODO: Also change text color
			// Adds transparency when disabled
			if (isEnabled)
				pushColor(background.withAlpha(0.55))
			else
				returnColor()
		}
		label.setEnabled(newEnabled)
	}
	
	/**
	  * @return Whether this button is currently in focus
	  */
	def isInFocus = _isInFocus
	
	
	// IMPLEMENTED	------------------
	
	override def background_=(color: Color) =
	{
		// When background updates, also updates the border
		super.background_=(color)
		updateBorder(color)
	}
	
	override def alignment = Center
	
	override def hasMinWidth = true
	
	override def updateLayout() = Unit
	
	override def toString = s"Button($text)"
	
	
	// OTHER	----------------------
	
	/**
	  * Triggers this button's action. Same as if the user clicked this button (only works for enabled buttons)
	  */
	def trigger() = if (isEnabled) action()
	
	/**
	  * Makes this button request focus within the current window
	  * @return Whether this button is likely to gain focus
	  */
	def requestFocus() = label.requestFocusInWindow()
	
	private def updateBorder(baseColor: Color) = setBorder(Border.raised(borderWidth, baseColor, 0.5))
	
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
	
	private class ButtonFocusListener extends FocusListener
	{
		override def focusGained(e: FocusEvent) =
		{
			if (!_isInFocus)
			{
				_isInFocus = true
				pushColor(background.lightened(1.5))
			}
		}
		
		override def focusLost(e: FocusEvent) =
		{
			if (_isInFocus)
			{
				_isInFocus = false
				returnColor()
			}
		}
	}
	
	private class ButtonKeyListener extends KeyStateListener
	{
		// Only accepts enter & space presses
		override val keyStateEventFilter = KeyStateEvent.wasPressedFilter &&
			(KeyStateEvent.keyFilter(KeyEvent.VK_SPACE) || KeyStateEvent.keyFilter(KeyEvent.VK_ENTER))
		
		override def onKeyState(event: KeyStateEvent) = trigger()
		
		override def parent = None
		
		// Only allows handling while in focus
		override def allowsHandlingFrom(handlerType: HandlerType) = _isInFocus
	}
	
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
