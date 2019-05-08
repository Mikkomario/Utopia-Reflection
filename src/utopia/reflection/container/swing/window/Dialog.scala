package utopia.reflection.container.swing.window

import javax.swing.JDialog
import utopia.reflection.component.stack.Stackable
import utopia.reflection.container.stack.StackHierarchyManager
import utopia.reflection.container.swing.AwtContainerRelated
import utopia.reflection.container.swing.window.WindowResizePolicy.User
import utopia.reflection.util.Screen


/**
* A frame operates as the / a main window in an app
* @author Mikko Hilpinen
* @since 26.3.2019
**/
class Dialog[C <: Stackable with AwtContainerRelated](owner: java.awt.Window, override val content: C, override val title: String,
                                                     startResizePolicy: WindowResizePolicy = User) extends Window[C]
{
    // ATTRIBUTES    -------------------
    
    private val _component = new JDialog(owner, title)
    private var _resizePolicy = startResizePolicy
    
    
    // INITIAL CODE    -----------------
    
    {
        // Makes sure content size has been cached (so that events will be fired correctly)
        content.size
        
        // Sets up the underlying frame
        _component.setContentPane(content.component)
        _component.setResizable(startResizePolicy.allowsUserResize)
        _component.pack()
    
        // Sets position and size
        updateWindowBounds(true)
    
        if (!fullScreen)
            position = ((Screen.size - size) / 2).toVector.toPoint
        
        updateContentBounds()
    
        // Registers to update bounds on each size change
        activateResizeHandling()
        
        // Registers self (and content) into stack hierarchy management
        StackHierarchyManager.registerConnection(this, content)
    }
    
	// IMPLEMENTED    ------------------
    
    def component = _component
    
    def fullScreen = false
    def showsToolBar: Boolean = true
    
    def resizePolicy = _resizePolicy
    def resizePolicy_=(newPolicy: WindowResizePolicy) =
    {
        _resizePolicy = newPolicy
        _component.setResizable(newPolicy.allowsUserResize)
    }
}