package utopia.reflection.component

import utopia.flow.util.NullSafe._

import utopia.genesis.shape.shape2D.Point
import utopia.genesis.shape.shape2D.Size
import java.awt.Component
import java.awt.Font
import java.awt.Color
import utopia.reflection.shape.StackSize
import utopia.reflection.event.ResizeListener
import utopia.reflection.event.ResizeEvent
import utopia.flow.datastructure.mutable.Lazy

object Wrapper
{
    /**
     * Wraps a component
     */
    def apply(component: Component): Wrapper = new SimpleWrapper(component)    
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
     * The wrapped component
     */
    def component: Component
    
    
    // IMPLEMENTED    ---------------------
    
    def position = cachedPosition.get
    def position_=(p: Point) = cachedPosition.set(p)// component.setLocation(p.toAwtPoint)
    
    def size = cachedSize.get
    def size_=(s: Size) = 
    {
        println(s"Setting size to $s. Will inform ${resizeListeners.size} listeners")
        
        // Informs resize listeners, if there are any
        if (resizeListeners.isEmpty)
            cachedSize.set(s)// component.setSize(s.toDimension)
        else
        {
            val oldSize = size
            cachedSize.set(s)// component.setSize(s.toDimension)
            
            println(s"$oldSize -> $s")
            
            if (oldSize !~== s)
            {
                val newEvent = ResizeEvent(oldSize, s)
                resizeListeners.foreach { _.onResizeEvent(newEvent) }
            }
        }
    }
    
    
	// COMPUTED PROPERTIES    -------------
    
    def parent: Option[Wrapper] =  component.getParent.toOption.map(new SimpleWrapper(_))
    def parents: Iterator[Wrapper] = new ParentsIterator()
    
    def visible = component.isVisible
    def visible_=(isVisible: Boolean) = component.setVisible(isVisible)
    
    def background = component.getBackground
    def background_=(color: Color) = component.setBackground(color)
    
    def transparent = !component.isOpaque
    
    def font = component.getFont.toOption
    def font_=(f: Font) = component.setFont(f)
    
    def fontMetrics = font.map(component.getFontMetrics(_))
    def textWidth(text: String) = 
    {   
        if (text.isEmpty)
            Some(0)
        else
            fontMetrics.map(_.stringWidth(text))
    }
    
    // TODO: Add support for mouse events
    
    
    // OTHER    -------------------------
    
    def updateBounds() = 
    {
        cachedPosition.current.foreach { p => component.setLocation(p.toAwtPoint) }
        cachedSize.current.foreach { s => component.setSize(s.toDimension) }
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
    def withStackSize(size: StackSize) = Stackable(component, size)
    
    
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

private class SimpleWrapper(val component: Component) extends Wrapper