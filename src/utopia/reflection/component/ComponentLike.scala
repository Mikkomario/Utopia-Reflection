package utopia.reflection.component

import utopia.genesis.shape.shape2D.Point
import java.awt.FontMetrics

import utopia.reflection.event.ResizeListener
import utopia.genesis.color.Color
import utopia.genesis.event.{KeyStateEvent, KeyTypedEvent, MouseButtonStateEvent, MouseEvent, MouseMoveEvent, MouseWheelEvent}
import utopia.genesis.handling.{KeyStateListener, KeyTypedListener, MouseButtonStateListener, MouseMoveListener, MouseWheelListener}
import utopia.genesis.handling.mutable.{KeyStateHandler, KeyTypedHandler, MouseButtonStateHandler, MouseMoveHandler, MouseWheelHandler}
import utopia.inception.handling.Handleable
import utopia.reflection.container.Container

/**
* This trait describes basic component features without any implementation
* @author Mikko Hilpinen
* @since 25.2.2019
**/
trait ComponentLike extends Area
{
    // ABSTRACT    ------------------------
    
    def resizeListeners: Vector[ResizeListener]
    def resizeListeners_=(listeners: Vector[ResizeListener]): Unit
    
    /**
      * @return The parent component of this component
      */
    def parent: Option[ComponentLike]
    
    /**
      * @return Whether this component is currently visible
      */
    def isVisible: Boolean
    /**
      * Updates this component's visibility
      * @param isVisible Whether this component is currently visible
      */
    def isVisible_=(isVisible: Boolean): Unit
    
    /**
      * @return The background color of this component
      */
    def background: Color
    def background_=(color: Color): Unit
    
    /**
      * @return Whether this component is transparent (not drawing full area)
      */
    def isTransparent: Boolean
    
    /**
      * @return The font metrics object for this component. None if font hasn't been specified.
      */
    def fontMetrics: Option[FontMetrics]
    
    def mouseButtonHandler: MouseButtonStateHandler
    def mouseMoveHandler: MouseMoveHandler
    def mouseWheelHandler: MouseWheelHandler
    
    def keyStateHandler: KeyStateHandler
    def keyTypedHandler: KeyTypedHandler
    
    
    // OTHER    ---------------------------
    
    /**
      * @return An iterator of this components parents
      */
    def parents: Iterator[ComponentLike] = new ParentsIterator()
    
    /**
      * @return The background color of this component's first non-transparent parent. None if this component doesn't
      *         have a non-transparent parent
      */
    def parentBackground = parents.find { !_.isTransparent }.map { _.background }
    
    /**
      * Calculates text width within this component
      * @param text Text to be presented
      * @return The with of the would be text
      */
    def textWidth(text: String) = 
    {
        if (text.isEmpty)
            Some(0)
        else
            fontMetrics.map { _.stringWidth(text) }
    }
    /**
      * @return The text height for the current font used in this component
      */
    def textHeight = fontMetrics.map { _.getHeight }
    
    
    // OTHER    -------------------------
    
    /**
      * Distributes a mouse button event to this wrapper and children
      * @param event A mouse event. Should be within this component's parent's context
      *              (origin should be at the parent component's position). Events outside parent context shouldn't be
      *              distributed.
      */
    def distributeMouseButtonEvent(event: MouseButtonStateEvent): Unit =
    {
        // Informs own listeners first
        mouseButtonHandler.onMouseButtonState(event)
        
        distributeDefaultMouseEvent[MouseButtonStateEvent](event, (e, p) => e.copy(mousePosition = p),
            _.distributeMouseButtonEvent(_))
    }
    
    /**
      * Distributes a mouse move event to this wrapper and children
      * @param event A mouse move event. Should be within this component's parent's context
      *              (origin should be at the parent component's position). Events outside parent context shouldn't be
      *              distributed.
      */
    def distributeMouseMoveEvent(event: MouseMoveEvent): Unit =
    {
        // Informs own listeners first
        mouseMoveHandler.onMouseMove(event)
        
        distributeEvent[MouseMoveEvent](event, e => Vector(e.mousePosition, e.previousMousePosition),
            (e, t) => e.copy(mousePosition = e.mousePosition - t, previousMousePosition = e.previousMousePosition - t),
            _.distributeMouseMoveEvent(_))
    }
    
    /**
      * Distributes a mouse wheel event to this wrapper and children
      * @param event A mouse wheel event. Should be within this component's parent's context
      *              (origin should be at the parent component's position). Events outside parent context shouldn't be
      *              distributed.
      */
    def distributeMouseWheelEvent(event: MouseWheelEvent): Unit =
    {
        // Informs own listeners
        mouseWheelHandler.onMouseWheelRotated(event)
        // Then continues with child components
        distributeDefaultMouseEvent[MouseWheelEvent](event, (e, p) => e.copy(mousePosition = p), _.distributeMouseWheelEvent(_))
    }
    
    /**
      * Distributes a keyboard state event through this component's hierarchy. Should only be called for components
      * in the topmost window
      * @param event A keyboard state event
      */
    def distributeKeyStateEvent(event: KeyStateEvent): Unit =
    {
        keyStateHandler.onKeyState(event)
        forChildren { _.distributeKeyStateEvent(event) }
    }
    
    /**
      * Distributes a key typed event through this component's hierarchy. Should only be called for components
      * in the topmost window
      * @param event A key typed event
      */
    def distributeKeyTypedEvent(event: KeyTypedEvent): Unit =
    {
        keyTypedHandler.onKeyTyped(event)
        forChildren { _.distributeKeyTypedEvent(event) }
    }
    
    /**
      * Adds a new mouse button listener to this wrapper
      * @param listener A new listener
      */
    def addMouseButtonListener(listener: MouseButtonStateListener) = mouseButtonHandler += listener
    
    /**
      * Adds a new mouse move listener to this wrapper
      * @param listener A new listener
      */
    def addMouseMoveListener(listener: MouseMoveListener) = mouseMoveHandler += listener
    
    /**
      * Adds a new mouse wheel listener to this wrapper
      * @param listener A new listener
      */
    def addMouseWheelListener(listener: MouseWheelListener) = mouseWheelHandler += listener
    
    /**
      * Adds a new key state listener to this wrapper
      * @param listener A listener
      */
    def addKeyStateListener(listener: KeyStateListener) = keyStateHandler += listener
    
    /**
      * Adds a new key typed listener to this wrapper
      * @param listener A listener
      */
    def addKeyTypedListener(listener: KeyTypedListener) = keyTypedHandler += listener
    
    /**
      * Removes a listener from this wrapper
      * @param listener A listener to be removed
      */
    def removeListener(listener: Handleable) = forMeAndChildren
    {
        c =>
            c.mouseButtonHandler -= listener
            c.mouseMoveHandler -= listener
            c.mouseWheelHandler -= listener
            c.keyStateHandler -= listener
            c.keyTypedHandler -= listener
    }
    
    /**
      * Adds a resize listener to listen to this component
      * @param listener A resize listener
      */
    def addResizeListener(listener: ResizeListener) = resizeListeners :+= listener
    
    /**
      * Removes a resize listener from this component
      * @param listener A resize listener to be removed
      */
    def removeResizeListener(listener: Any) = resizeListeners = resizeListeners.filterNot { _ == listener }
    
    /**
     * Removes all resize listeners from this wrapper
     */
    def clearResizeListeners() = resizeListeners = Vector()
    
    private def forMeAndChildren[U](operation: ComponentLike => U): Unit =
    {
        operation(this)
        forChildren(operation)
    }
    
    private def forChildren[U](operation: ComponentLike => U): Unit = this match
    {
            // TODO: Container needs a non-swing super type here
        case c: Container[_] => c.components.foreach { operation(_) }
        case _ => Unit
    }
    
    private def distributeDefaultMouseEvent[E <: MouseEvent](event: E, withPosition: (E, Point) => E,
                                                             childAccept: (ComponentLike, E) => Unit) =
        distributeEvent[E](event, e => Vector(e.mousePosition), (e, t) => withPosition(e, e.mousePosition - t), childAccept)
    
    private def distributeEvent[E](event: E, positionsFromEvent: E => Traversable[Point],
                                   translateEvent: (E, Point) => E, childAccept: (ComponentLike, E) => Unit) =
    {
        // If has chilren, informs them. Event position is modified and only events within this component's area
        // are relayed forward
        val myBounds = bounds
        if (positionsFromEvent(event).exists(myBounds.contains))
        {
            val translated = translateEvent(event, myBounds.position)
            forChildren { childAccept(_, translated) }
        }
    }
    
    
    // NESTED CLASSES    ----------------
    
    // This iterator is used for iterating through parent components (bottom to top)
    private class ParentsIterator extends Iterator[ComponentLike]
    {
        var nextParent = parent
        
        def hasNext = nextParent.isDefined
        
        def next() = 
        {
            val result = nextParent.get
            nextParent = result.parent
            result
        }
    }
}