package utopia.reflection.component

import utopia.flow.util.NullSafe._

import javax.swing.JComponent
import utopia.genesis.shape.shape2D.Point
import utopia.genesis.shape.shape2D.Size
import utopia.genesis.shape.shape2D.Bounds
import java.awt.Component
import java.awt.Font
import java.awt.Color
import utopia.reflection.shape.StackSize

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
    // ABSTRACT    ------------------------
    
    /**
     * The wrapped component
     */
    def component: Component
    
    
    // IMPLEMENTED    ---------------------
    
    def position = Point(component.getX, component.getY)
    def position_=(p: Point) = component.setLocation(p.toAwtPoint)
    
    def size = Size(component.getWidth, component.getHeight)
    def size_=(s: Size) = component.setSize(s.toDimension)
    
    
	// COMPUTED PROPERTIES    -------------
    
    def parent: Option[Wrapper] =  component.getParent.toOption.map(new SimpleWrapper(_))
    def parents: Iterator[Wrapper] = new ParentsIterator()
    
    def visible = component.isVisible()
    def visible_=(isVisible: Boolean) = component.setVisible(isVisible)
    
    def background = component.getBackground
    def background_=(color: Color) = component.setBackground(color)
    
    def transparent = !component.isOpaque()
    
    def font = component.getFont.toOption
    def font_=(f: Font) = component.setFont(f)
    
    def fontMetrics = font.map(component.getFontMetrics(_))
    def textWidth(text: String) = 
    {   
        if (text.isEmpty())
            Some(0)
        else
            fontMetrics.map(_.stringWidth(text))
    }
    
    // TODO: Add support for mouse events
    
    
    // OTHER    -------------------------
    
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