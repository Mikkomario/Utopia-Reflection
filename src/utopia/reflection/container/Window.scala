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
        (content.stackSize + insets.total).limitedTo(maxSize)
    }
    
    
    // OTHER    --------------------
    
    /**
     * Centers this window on the screen
     */
    def centerOnScreen() = component.setLocationRelativeTo(null)
    
    /**
     * Centers this window on the screen or on the parent component
     */
    def center() = component.setLocationRelativeTo(component.getParent)
    
    /**
     * Makes this window fill the whole screen
     */
    def goFullScreen() = 
    {
        position = if (showsToolBar) insets.toPoint else Point.origin
        stackSize.max.foreach { size = _ }
    }
}