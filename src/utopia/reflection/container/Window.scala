package utopia.reflection.container

import utopia.reflection.component.Wrapper
import utopia.reflection.component.Stackable
import utopia.genesis.shape.shape2D.Size
import utopia.reflection.shape.Insets
import utopia.reflection.util.Screen
import utopia.genesis.shape.shape2D.Point

/**
* This is a common wrapper for all window implementations
* @author Mikko Hilpinen
* @since 25.3.2019
**/
trait Window[C <: Stackable] extends Stackable
{
	// ABSTRACT    -----------------
    
    override def component: java.awt.Window
    
    /**
     * The content displayed in this window
     */
    def content: C
    
    /**
     * Whether the OS toolbar should be shown
     */
    def showsToolBar: Boolean
    
    /**
     * Whether this window is currently set to full screen mode
     */
    def fullScreen: Boolean
    
    
    // COMPUTED    ----------------
    
    /**
     * The insets in around this screen
     */
    def insets = Insets of component.getInsets
    
    
    // IMPLEMENTED    ------------
    
    def stackSize = 
    {
        val maxSize = 
        {
            if (showsToolBar)
                Screen.size - Screen.insetsAt(component.getGraphicsConfiguration).total
            else 
                Screen.size
        }
        val normal = (content.stackSize + insets.total).limitedTo(maxSize)
        
        // If on full screen mode, tries to maximize screen size
        if (fullScreen)
            normal.withOptimal(normal.max getOrElse normal.optimal)
        else
            normal
    }
    
    
    // OTHER    --------------------
    
    /**
     * Centers this window on the screen
     */
    def centerOnScreen() = centerOn(null)
    
    /**
     * Centers this window on the screen or on the parent component
     */
    def center() = centerOn(component.getParent)
    
    private def centerOn(component: java.awt.Component) = 
    {
        if (fullScreen)
        {
            if (showsToolBar)
                position = Screen.insetsAt(component.getGraphicsConfiguration).toPoint
            else
                position = Point.origin
        }
        else
            this.component.setLocationRelativeTo(component)
    }
}