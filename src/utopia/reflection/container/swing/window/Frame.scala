package utopia.reflection.container.swing.window

import javax.swing.{JFrame, WindowConstants}
import utopia.reflection.component.stack.Stackable
import utopia.reflection.container.swing.AwtContainerRelated

object Frame
{
    /**
      * Creates a new windowed frame
      * @param content The frame contents
      * @param title The frame title
      * @param resizePolicy The policy used about Frame resizing. By default, only the user may resize the Frame
      * @return A new windowed frame
      */
    def windowed[C <: Stackable with AwtContainerRelated](content: C, title: String,
                                                          resizePolicy: WindowResizePolicy = WindowResizePolicy.User,
                                                          borderless: Boolean = false) =
        new Frame(content, title, resizePolicy, borderless, false, false)
    
    /**
      * Creates a new full screen frame
      * @param content The frame contents
      * @param title The frame title
      * @param showToolBar Whether tool bar (bottom) should be displayed
      * @return A new full screen frame
      */
    def fullScreen[C <: Stackable with AwtContainerRelated](content: C, title: String, showToolBar: Boolean) =
        new Frame(content, title, WindowResizePolicy.Program, true, true, showToolBar)
}

/**
* A frame operates as the / a main window in an app
* @author Mikko Hilpinen
* @since 26.3.2019
**/
class Frame[C <: Stackable with AwtContainerRelated](override val content: C, override val title: String,
                                                     startResizePolicy: WindowResizePolicy, val borderless: Boolean,
                                                     startFullScreen: Boolean, startWithToolBar: Boolean) extends Window[C]
{
    // ATTRIBUTES    -------------------
    
    private val _component = new JFrame(title)
    
    private var _fullScreen = startFullScreen
    private var _showsToolBar = startWithToolBar
    private var _resizePolicy = startResizePolicy
    
    
    // INITIAL CODE    -----------------
    
    {
        // Makes sure content size has been cached (so that events will be fired correctly)
        content.size
        
        // Sets up the underlying frame
        _component.setContentPane(content.component)
        _component.setUndecorated(borderless)
        _component.setResizable(startResizePolicy.allowsUserResize)
        _component.pack()
    
        setup()
    }
    
	// IMPLEMENTED    ------------------
    
    def component: JFrame = _component
    
    def fullScreen: Boolean = _fullScreen
    def fullScreen_=(newStatus: Boolean) =
    {
        if (_fullScreen != newStatus)
        {
            _fullScreen = newStatus
            // Resizes and repositions the frame when status changes
            resetCachedSize()
            updateWindowBounds(true)
        }
    }
     
    def showsToolBar: Boolean = _showsToolBar
    def showsToolBar_=(newStatus: Boolean) =
    {
        if (_showsToolBar != newStatus)
        {
            _showsToolBar = newStatus
            // Resizes and repositions when status changes
            resetCachedSize()
            updateWindowBounds(true)
        }
    }
    
    def resizePolicy = _resizePolicy
    def resizePolicy_=(newPolicy: WindowResizePolicy) =
    {
        _resizePolicy = newPolicy
        _component.setResizable(newPolicy.allowsUserResize)
    }
    
    
    // OTHER    ------------------------
    
    /**
     * Sets it so that JVM will exit once this frame closes
     */
    def setToExitOnClose() = _component.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
}