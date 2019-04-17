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
    
    // TODO: Won't fire resize events on first time because cached size matches component size
    updateContentBounds(size)
    
    // Registers to update bounds on each validation
    _component.validationListeners :+= (() => frameRevalidated())
    
    // Registers a listener to update content bounds on frame size changes
    resizeListeners :+= ResizeListener(e => updateContentBounds(e.newSize))
    
    
	// IMPLEMENTED    ------------------
    
    def component: JFrame = _component
    
    // Each time (content) layout is updated, may resize this frame
    override def updateLayout() = updateFrameBounds()
    
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
    
    private def frameRevalidated() =
    {
        println("Revalidating")
        // Each time frame is validated, updates cached size, which then fires content bounds update too
        size = Size(_component.getWidth, _component.getHeight)
        println("Validation completed")
    }
    
    private def updateContentBounds(newSize: Size) = 
    {
        val newContentSize = newSize - insets.total
        println(s"Setting frame content size to: $newContentSize")
        content.size = newContentSize
        
        // Fires resize events for container (since they won't be fired from the container itself)
        /*
        val event = ResizeEvent(lastContentSize, newContentSize)
        lastContentSize = newContentSize
        content.resizeListeners.foreach { _.onResizeEvent(event) }*/
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