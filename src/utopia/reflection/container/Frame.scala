package utopia.reflection.container

import javax.swing.JFrame
import utopia.reflection.util.Screen
import utopia.genesis.shape.shape2D.Point
import utopia.reflection.event.ResizeListener
import utopia.genesis.shape.shape2D.Size
import javax.swing.WindowConstants

object Frame
{
    /**
      * Creates a new windowed frame
      * @param content The frame contents
      * @param title The frame title
      * @return A new windowed frame
      */
    def windowed(content: StackContainer[_], title: String) = new Frame(content, title, false,
        false, false)
    
    /**
      * Creates a new full screen frame
      * @param content The frame contents
      * @param title The frame title
      * @param showToolBar Whether tool bar (bottom) should be displayed
      * @return A new full screen frame
      */
    def fullScreen(content: StackContainer[_], title: String, showToolBar: Boolean) = new Frame(content, title,
        true, true, showToolBar)
}

/**
* A frame operates as the / a main window in an app
* @author Mikko Hilpinen
* @since 26.3.2019
**/
// TODO: Maybe add title to the window trait
class Frame(val content: StackContainer[_], val title: String,
        val borderless: Boolean, startFullScreen: Boolean, startWithToolBar: Boolean) extends Window[StackContainer[_]]
{
    // ATTRIBUTES    -------------------
    
    private val _component = new MyFrame(title, borderless, content.component)
    
    private var _fullScreen = startFullScreen
    private var _showsToolBar = startWithToolBar
    
    
    // INITIAL CODE    -----------------
    
    // Sets position and size
    updateFrameBounds()
    
    if (!fullScreen)
        position = ((Screen.size - size) / 2).toVector.toPoint
    
    updateContentBounds(size)
    
    // Registers to update bounds on each validation
    // _component.validationListeners :+= (() => updateContentBounds(size))
    
    // Registers a listener to update content bounds on frame size changes
    resizeListeners :+= ResizeListener(e => updateContentBounds(e.newSize))
    
    
	// IMPLEMENTED    ------------------
    
    def component: JFrame = _component
     
    def fullScreen: Boolean = _fullScreen
    def fullScreen_=(newStatus: Boolean) = _fullScreen = newStatus // TODO: Resize and reposition
     
    def showsToolBar: Boolean = _showsToolBar
    def showsToolBar_=(newStatus: Boolean) = _showsToolBar = newStatus // TODO: Resize if necessary
    
    
    // OTHER    ------------------------
    
    /**
     * Sets it so that JVM will exit once this frame closes
     */
    def setToExitOnClose() = _component.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    
    private def updateFrameBounds() = 
    {
        if (fullScreen)
        {
            if (showsToolBar)
                position = Screen.insetsAt(component.getGraphicsConfiguration).toPoint
            else
                position = Point.origin
                
            size = stackSize.optimal
        }
        else
        {
            val oldSize = size
            size = stackSize.optimal
            
            val increase = size - oldSize
            position = (position - (increase / 2).toVector).positive
        }
    }
    
    private def updateContentBounds(newSize: Size) = 
    {
        println(s"Setting frame content size to: $newSize")
        content.size = newSize - insets.total
    }
}

private class MyFrame(title: String, borderless: Boolean, contentPanel: java.awt.Container) extends JFrame(title)
{
    // ATTRIBUTES    -------------------
    
    var validationListeners = Vector[() => Unit]()
    
    
    // INITIAL CODE    -----------------
    
    setContentPane(contentPanel)
    setUndecorated(borderless)
    pack()
    
    
    // IMPLEMENTED    ------------------
    
    // Updates content bounds whenever this frame is revalidated
    override def validate() = 
    {
        validationListeners.foreach { _() }
        super.validate()
    }
}