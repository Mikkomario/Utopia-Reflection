package utopia.reflection.component.swing

import java.awt.event.{FocusEvent, FocusListener, KeyEvent}

import utopia.genesis.event.{KeyStateEvent, MouseButton, MouseButtonStateEvent, MouseMoveEvent}
import utopia.genesis.handling.{KeyStateListener, MouseButtonStateListener, MouseMoveListener}
import utopia.inception.handling.HandlerType
import utopia.reflection.component.ComponentLike

/**
  * Used as a common trait for all different button implementations
  * @author Mikko Hilpinen
  * @since 25.4.2019, v1+
  */
trait ButtonLike extends ComponentLike with AwtComponentRelated
{
	// ABSTRACT	----------------------
	
	/**
	  * Performs the action associated with this button
	  */
	protected def performAction(): Unit
	/**
	  * Updates this button's style to match the new state
	  * @param newState New button state
	  */
	protected def updateStyleForState(newState: ButtonState): Unit
	
	
	// ATTRIBUTES	------------------
	
	private var _state: ButtonState = ButtonState(isEnabled = true, isInFocus = false, isMouseOver = false, isPressed = false)
	
	
	// INITIAL CODE	------------------
	
	{
		// Adds mouse handling
		val listener = new ButtonMouseListener()
		addMouseMoveListener(listener)
		addMouseButtonListener(listener)
		
		// Adds key listening
		addKeyStateListener(new ButtonKeyListener())
		
		// Adds focus listening
		component.addFocusListener(new ButtonFocusListener())
	}
	
	
	// COMPUTED	----------------------
	
	/**
	  * @return Whether this button is currently enabled
	  */
	def isEnabled = component.isEnabled
	/**
	  * @param newEnabled Whether this button should be enabled
	  */
	def isEnabled_=(newEnabled: Boolean) =
	{
		state = _state.copy(isEnabled = newEnabled)
		component.setEnabled(newEnabled)
	}
	
	/**
	  * @return This button's current state
	  */
	def state = _state
	private def state_=(newState: ButtonState) =
	{
		_state = newState
		updateStyleForState(newState)
	}
	
	/**
	  * @return Whether this button is currently in focus
	  */
	def isInFocus = state.isInFocus
	private def isInFocus_=(newFocusState: Boolean) = state = state.copy(isInFocus = newFocusState)
	
	
	// OTHER	----------------------
	
	/**
	  * Triggers this button's action. Same as if the user clicked this button (only works for enabled buttons)
	  */
	def trigger() = if (isEnabled) performAction()
	
	/**
	  * Makes this button request focus within the current window
	  * @return Whether this button is likely to gain focus
	  */
	def requestFocus() = component.requestFocusInWindow()
	
	
	// NESTED CLASSES	--------------
	
	private class ButtonFocusListener extends FocusListener
	{
		override def focusGained(e: FocusEvent) =
		{
			if (!isInFocus)
				isInFocus = true
		}
		
		override def focusLost(e: FocusEvent) =
		{
			if (isInFocus)
				isInFocus = false
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
		override def allowsHandlingFrom(handlerType: HandlerType) = isInFocus
	}
	
	private class ButtonMouseListener extends MouseButtonStateListener with MouseMoveListener
	{
		// ATTRIBUTES	--------------
		
		def isDown = state.isPressed
		def isDown_=(newState: Boolean) = state = state.copy(isPressed = newState)
		
		
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
					trigger()
					isDown = false
					true
				}
				else
					false
			}
			else if (event.isOverArea(bounds))
			{
				isDown = true
				true
			}
			else
				false
		}
		
		// When mouse enters, brightens, when mouse leaves, returns
		override def onMouseMove(event: MouseMoveEvent) =
		{
			if (event.isOverArea(bounds))
				state = state.copy(isMouseOver = true)
			else
				state = state.copy(isMouseOver = false)
		}
		
		override def parent = None
		
		override def allowsHandlingFrom(handlerType: HandlerType) = isEnabled
	}
}
