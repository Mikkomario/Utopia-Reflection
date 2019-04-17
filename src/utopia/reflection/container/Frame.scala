package utopia.reflection.container

import java.awt.event.{ComponentAdapter, ComponentEvent}

import javax.swing.JFrame
import utopia.reflection.util.Screen
import utopia.genesis.shape.shape2D.Point
import utopia.genesis.shape.shape2D.Size
import javax.swing.WindowConstants

object Frame
{
    /**
      * Creates a new windowed frame
      * @param content The frame contents
      * @param title The frame title
      * @param resizePolicy The policy used about Frame resizing. By default, only the user may resize the Frame
      * @return A new windowed frame
      */
    def windowed(content: StackContainer[_], title: String,
                 resizePolicy: WindowResizePolicy = WindowResizePolicy.User) =
        new Frame(content, title, resizePolicy, false, false, false)
    
    /**
      * Creates a new full screen frame
      * @param content The frame contents
      * @param title The frame title
      * @param showToolBar Whether tool bar (bottom) should be displayed
      * @return A new full screen frame
      */
    def fullScreen(content: StackContainer[_], title: String, showToolBar: Boolean) = new Frame(content, title,
        WindowResizePolicy.Program, true, true, showToolBar)
}

/**
* A frame operates as the / a main window in an app
* @author Mikko Hilpinen
* @since 26.3.2019
**/
// TODO: Maybe add title to the window trait
class Frame(val content: StackContainer[_], val title: String, startResizePolicy: WindowResizePolicy,
        val borderless: Boolean, startFullScreen: Boolean, startWithToolBar: Boolean) extends Window[StackContainer[_]]
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
    
        // Sets position and size
        updateFrameBounds(true)
    
        if (!fullScreen)
            position = ((Screen.size - size) / 2).toVector.toPoint
    
        updateContentBounds(size)
    
        // Registers to update bounds on each size change
        _component.addComponentListener(new FrameListener())
        
        // Registers self (and content) into stack hierarchy management
        StackHierarchyManager.registerConnection(this, content)
    }
    
	// IMPLEMENTED    ------------------
    
    def component: JFrame = _component
    
    // Each time (content) layout is updated, may resize this frame
    override def updateLayout() = updateFrameBounds(resizePolicy.allowsProgramResize)
    
    def fullScreen: Boolean = _fullScreen
    def fullScreen_=(newStatus: Boolean) = _fullScreen = newStatus // TODO: Resize and reposition
     
    def showsToolBar: Boolean = _showsToolBar
    def showsToolBar_=(newStatus: Boolean) = _showsToolBar = newStatus // TODO: Resize if necessary
    
    def resizePolicy = _resizePolicy
    def resizePolicy_=(newPolicy: WindowResizePolicy) =
    {
        _resizePolicy = newPolicy
        _component.setResizable(newPolicy.allowsUserResize)
    }
    
    // Overrides size and position because user may resize and move this frame at will, breaking the cached size / position logic
    override def size = Size(component.getWidth, component.getHeight)
    override def size_=(newSize: Size) = component.setSize(newSize.toDimension)
    
    override def position = Point(component.getX, component.getY)
    override def position_=(newPosition: Point) = component.setLocation(newPosition.toAwtPoint)
    
    
    // OTHER    ------------------------
    
    /**
     * Sets it so that JVM will exit once this frame closes
     */
    def setToExitOnClose() = _component.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    
    private def updateFrameBounds(dictateSize: Boolean) =
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
            // In windowed mode, either dictates the new size or just makes sure its within limits
            if (dictateSize)
            {
                val oldSize = size
                size = stackSize.optimal
    
                val increase = size - oldSize
                position = (position - (increase / 2).toVector).positive
            }
            else
                checkFrameBounds()
        }
    }
    
    private def checkFrameBounds() =
    {
        // Checks current bounds against allowed limits
        stackSize.maxWidth.filter { _ < width }.foreach { width = _ }
        stackSize.maxHeight.filter { _ < height }.foreach { height = _ }
        
        if (isUnderSized)
            size = size max stackSize.min
    }
    
    private def updateContentBounds(newSize: Size) = 
    {
        content.size = newSize - insets.total
        
        // Fires resize events for container (since they won't be fired from the container itself)
        /*
        val event = ResizeEvent(lastContentSize, newContentSize)
        lastContentSize = newContentSize
        content.resizeListeners.foreach { _.onResizeEvent(event) }*/
    }
    
    
    // NESTED CLASSES   -------------------
    
    private class FrameListener extends ComponentAdapter
    {
        // When frame is resized, also updates content size
        override def componentResized(e: ComponentEvent) = updateContentBounds(size)
    }
}