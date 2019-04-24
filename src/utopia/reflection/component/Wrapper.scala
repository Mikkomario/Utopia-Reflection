package utopia.reflection.component

import utopia.flow.util.NullSafe._
import utopia.genesis.shape.shape2D.Point
import utopia.genesis.shape.shape2D.Size
import java.awt.Component
import java.awt.Color

import javax.swing.SwingUtilities
import utopia.flow.async.VolatileFlag
import utopia.reflection.shape.StackSize
import utopia.reflection.event.ResizeListener
import utopia.reflection.event.ResizeEvent
import utopia.flow.datastructure.mutable.Lazy
import utopia.genesis.event.{KeyStateEvent, KeyTypedEvent, MouseButtonStateEvent, MouseEvent, MouseMoveEvent, MouseWheelEvent}
import utopia.genesis.handling.{KeyStateListener, KeyTypedListener, MouseButtonStateListener, MouseMoveListener, MouseWheelListener}
import utopia.genesis.handling.mutable.{KeyStateHandler, KeyTypedHandler, MouseButtonStateHandler, MouseMoveHandler, MouseWheelHandler}
import utopia.inception.handling.Handleable
import utopia.reflection.container.Container

object Wrapper
{
    /**
     * Wraps a component
     */
    def apply(component: Component, children: Set[Wrapper] = Set()): Wrapper = new SimpleWrapper(component, children)
}

/**
* This class wraps a JComponent for a standardized interface
* @author Mikko Hilpinen
* @since 25.2.2019
**/
trait Wrapper extends Area
{
    // ATTRIBUTES    ----------------------
    
    // Temporarily cached position and size
    private val cachedPosition = new Lazy(() => Point(component.getX, component.getY))
    private val cachedSize = new Lazy(() => Size(component.getWidth, component.getHeight))
    
    private val updateDisabled = new VolatileFlag()
    
    // Handlers for distributing events
    private val mouseButtonHandler = MouseButtonStateHandler()
    private val mouseMoveHandler = MouseMoveHandler()
    private val mouseWheelHandler = MouseWheelHandler()
    
    private val keyStateHandler = KeyStateHandler()
    private val keyTypedHandler = KeyTypedHandler()
    
    /**
     * The currently active resize listeners for this wrapper. Please note that the listeners 
     * will by default only be informed on size changes made through this wrapper. Size changes 
     * that happen directly in the component are ignored by default
     */
    var resizeListeners = Vector[ResizeListener]()
    /**
     * Removes a resize listener from the informed listeners
     */
    def resizeListeners_-=(listener: Any) = resizeListeners = resizeListeners.filterNot { _ == listener }
    
    
    // ABSTRACT    ------------------------
    
    /**
     * @return The wrapped component
     */
    def component: Component
    
    
    // IMPLEMENTED    ---------------------
    
    /**
      * @return This component's current position
      */
    def position = cachedPosition.get
    def position_=(p: Point) =
    {
        cachedPosition.set(p)
        updateBounds()
    }
    
    /**
      * @return This component's current size
      */
    def size = cachedSize.get
    def size_=(s: Size) = 
    {
        // Informs resize listeners, if there are any
        if (resizeListeners.isEmpty)
            cachedSize.set(s)
        else
        {
            val oldSize = size
            cachedSize.set(s)
            
            if (oldSize !~== s)
            {
                val newEvent = ResizeEvent(oldSize, s)
                resizeListeners.foreach { _.onResizeEvent(newEvent) }
            }
        }
        
        updateBounds()
    }
    
    
	// COMPUTED PROPERTIES    -------------
    
    /**
      * @return The parent component of this component (wrapped)
      */
    def parent: Option[Wrapper] =  component.getParent.toOption.map { new SimpleWrapper(_, Set(this)) }
    /**
      * @return An iterator of this components parents (wrapped)
      */
    def parents: Iterator[Wrapper] = new ParentsIterator()
    
    /**
      * @return Whether this component is currently visible
      */
    def visible = component.isVisible
    def visible_=(isVisible: Boolean) = component.setVisible(isVisible)
    
    /**
      * @return The background color of this component
      */
    def background = component.getBackground
    def background_=(color: Color) = component.setBackground(color)
    
    /**
      * @return Whether this component is transparent (not drawing full area)
      */
    def transparent = !component.isOpaque
    
    /**
      * @return The font metrics object for this component. None if font hasn't been specified.
      */
    def fontMetrics = component.getFont.toOption.map { component.getFontMetrics(_) } orElse
        component.getGraphics.toOption.map { _.getFontMetrics }
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
      * Performs a (longer) operation on the GUI thread and updates the component size & position only after the update
      * has been done
      * @param operation The operation that will be run
      * @tparam U Arbitary result type
      */
    def doThenUpdate[U](operation: => U) =
    {
        SwingUtilities.invokeLater(() =>
        {
            // Disables update during operation
            updateDisabled.set()
            operation
            
            // Enables updates and performs them
            updateDisabled.reset()
            updateBounds()
        })
    }
    
    /**
     * Removes all resize listeners from this wrapper
     */
    def clearResizeListeners() = resizeListeners = Vector()
    
    /**
     * Transforms this wrapper into a Stackable
     */
    def withStackSize(getSize: () => StackSize) = Stackable(component, getSize)
    
    /**
     * Transforms this wrapper into a Stackable
     */
    // TODO: Don't just wrap the component, wrap this wrapper
    def withStackSize(size: StackSize) = Stackable(component, size)
    
    private def updateBounds(): Unit =
    {
        updateDisabled.doIfNotSet
        {
            // Updates own position and size
            cachedPosition.current.foreach
            { p => component.setLocation(p.toAwtPoint) }
            cachedSize.current.foreach
            { s => component.setSize(s.toDimension) }
        }
    }
    
    private def forMeAndChildren[U](operation: Wrapper => U): Unit =
    {
        operation(this)
        forChildren(operation)
    }
    
    private def forChildren[U](operation: Wrapper => U): Unit = this match
    {
        case c: Container[_] => c.components.foreach { operation(_) }
        case _ => Unit
    }
    
    private def distributeDefaultMouseEvent[E <: MouseEvent](event: E, withPosition: (E, Point) => E,
                                                             childAccept: (Wrapper, E) => Unit) =
        distributeEvent[E](event, e => Vector(e.mousePosition), (e, t) => withPosition(e, e.mousePosition - t), childAccept)
    
    private def distributeEvent[E](event: E, positionsFromEvent: E => Traversable[Point],
                                   translateEvent: (E, Point) => E, childAccept: (Wrapper, E) => Unit) =
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
    private class ParentsIterator extends Iterator[Wrapper]
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

private class SimpleWrapper(val component: Component, val children: Set[Wrapper]) extends Wrapper