package utopia.reflection.component.swing

import java.awt.{Component, Cursor}

import javax.swing.SwingUtilities
import utopia.flow.async.VolatileFlag
import utopia.flow.datastructure.mutable.Lazy
import utopia.flow.util.NullSafe._
import utopia.genesis.color.Color
import utopia.genesis.handling.mutable._
import utopia.genesis.shape.shape2D.{Point, Size}
import utopia.reflection.component.stack.{CachingStackable, Stackable}
import utopia.reflection.component.ComponentLike
import utopia.reflection.event.{ResizeEvent, ResizeListener}
import utopia.reflection.shape.StackSize

object AwtComponentWrapper
{
    /**
     * Wraps a component
     */
    // TODO: Consider wrapping the whole hierarchy
    def apply(component: Component): AwtComponentWrapper = new SimpleAwtComponentWrapper(component, Vector())
}

/**
* This class wraps a JComponent for a standardized interface
* @author Mikko Hilpinen
* @since 25.2.2019
**/
trait AwtComponentWrapper extends ComponentLike with AwtComponentRelated
{
    // ATTRIBUTES    ----------------------
    
    // Temporarily cached position and size
    private val cachedPosition = new Lazy(() => Point(component.getX, component.getY))
    private val cachedSize = new Lazy(() => Size(component.getWidth, component.getHeight))
    
    private val updateDisabled = new VolatileFlag()
    
    // Handlers for distributing events
    override val mouseButtonHandler = MouseButtonStateHandler()
    override val mouseMoveHandler = MouseMoveHandler()
    override val mouseWheelHandler = MouseWheelHandler()
    
    override val keyStateHandler = KeyStateHandler()
    override val keyTypedHandler = KeyTypedHandler()
    
    /**
     * The currently active resize listeners for this wrapper. Please note that the listeners
     * will by default only be informed on size changes made through this wrapper. Size changes
     * that happen directly in the component are ignored by default
     */
    override var resizeListeners = Vector[ResizeListener]()
    /**
     * Removes a resize listener from the informed listeners
     */
    def resizeListeners_-=(listener: Any) = resizeListeners = resizeListeners.filterNot { _ == listener }
    
    
    // IMPLEMENTED    ---------------------
    
    /**
      * @return This component's current position
      */
    override def position = cachedPosition.get
    override def position_=(p: Point) =
    {
        cachedPosition.set(p)
        updateBounds()
    }
    
    /**
      * @return This component's current size
      */
    override def size = cachedSize.get
    override def size_=(s: Size) =
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
    
    /**
      * @return The parent component of this component (wrapped)
      */
    override def parent: Option[AwtComponentWrapper] =  component.getParent.toOption.map { new SimpleAwtComponentWrapper(_, Vector(this)) }
    
    /**
      * @return Whether this component is currently visible
      */
    override def isVisible = component.isVisible
    override def isVisible_=(isVisible: Boolean) = component.setVisible(isVisible)
    
    /**
      * @return The background color of this component
      */
    override def background: Color = component.getBackground
    override def background_=(color: Color) =
    {
        // Since swing components don't handle transparency very well, mixes a transparent color with background instead
        if (color.isTransparent)
        {
            val myRGB = color.rgb
            val parentRGB = parentBackground.map { _.rgb }
            
            if (parentRGB.isDefined)
            {
                // Picks average, weighting by alpha
                val finalRGB = myRGB * color.alpha + parentRGB.get * (1 - color.alpha)
                component.setBackground(finalRGB.toAwt)
            }
            else
                component.setBackground(myRGB.toAwt)
        }
        else
            component.setBackground(color.toAwt)
    }
    
    /**
      * @return Whether this component is transparent (not drawing full area)
      */
    override def isTransparent = !component.isOpaque
    
    /**
      * @return The font metrics object for this component. None if font hasn't been specified.
      */
    override def fontMetrics = component.getFont.toOption.map { component.getFontMetrics(_) } orElse
        component.getGraphics.toOption.map { _.getFontMetrics }
    
    
    // OTHER    -------------------------
    
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
     * Transforms this wrapper into a Stackable
     */
    def withStackSize(getSize: () => StackSize, update: () => Unit = () => Unit): AwtComponentWrapperWrapper with Stackable =
        new AwtComponentWrapperWrapperWithStackable(this, getSize, update)
    
    /**
     * Transforms this wrapper into a Stackable
     */
    def withStackSize(size: StackSize): AwtComponentWrapperWrapper with Stackable = withStackSize(() => size)
    
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
}

private class SimpleAwtComponentWrapper(val component: Component, override val children: Seq[ComponentLike]) extends AwtComponentWrapper

private class AwtComponentWrapperWrapperWithStackable(override val wrapped: AwtComponentWrapper, getSize: () => StackSize, update: () => Unit)
    extends AwtComponentWrapperWrapper with CachingStackable
{
    // IMPLEMENTED  ---------------------
    
    override protected def calculatedStackSize = getSize()
    
    override protected def updateVisibility(visible: Boolean) = super[AwtComponentWrapperWrapper].isVisible_=(visible)
    
    override def isVisible_=(isVisible: Boolean) = super[CachingStackable].isVisible_=(isVisible)
    
    override def updateLayout() = update()
}