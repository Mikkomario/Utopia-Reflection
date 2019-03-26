package utopia.reflection.container

import utopia.reflection.component.Stackable
import utopia.reflection.component.JWrapper
import javax.swing.JFrame
import utopia.reflection.util.Screen
import utopia.genesis.shape.shape2D.Point
import utopia.reflection.event.ResizeListener
import utopia.genesis.shape.shape2D.Size

/**
* A frame operates as the / a main window in an app
* @author Mikko Hilpinen
* @since 26.3.2019
**/
// TODO: Maybe add title to the window trait
class Frame[C <: Stackable with Container[_]](val content: C, val title: String, 
        val borderless: Boolean, startFullScreen: Boolean, startWithToolBar: Boolean) extends Window[C]
{
    // ATTRIBUTES    -------------------
    
    private val _component = new MyFrame(title, borderless, content.component, 
            () => updateContentBounds(size));
    
    private var _fullScreen = startFullScreen
    private var _showsToolBar = startWithToolBar
    
    
    // INITIAL CODE    -----------------
    
    // Sets position and size
    updateBounds()
    
    if (!fullScreen)
        position = ((Screen.size - size) / 2).toVector.toPoint;
    
    updateContentBounds(size)
    
    // Registers a listener to update content bounds on frame size changes
    resizeListeners :+= ResizeListener(e => updateContentBounds(e.newSize))
    
    
	// IMPLEMENTED    ------------------
    
    def component: JFrame = _component
     
    def fullScreen: Boolean = _fullScreen
    def fullScreen_=(newStatus: Boolean) = _fullScreen = newStatus // TODO: Resize and reposition
     
    def showsToolBar: Boolean = _showsToolBar
    def showsToolBar_=(newStatus: Boolean) = _showsToolBar = newStatus // TODO: Resize if necessary
    
    
    // OTHER    ------------------------
    
    private def updateBounds() = 
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
    
    private def updateContentBounds(newSize: Size) = content.size = newSize - insets.total
}

private class MyFrame(title: String, borderless: Boolean, contentPanel: java.awt.Container, 
        val updateContentSize: () => Unit) extends JFrame(title)
{
    // INITIAL CODE    -----------------
    
    setContentPane(contentPanel)
    setUndecorated(borderless)
    pack()
    
    
    // IMPLEMENTED    ------------------
    
    // Updates content bounds whenever this frame is revalidated
    override def validate() = 
    {
        updateContentSize()
        super.validate()
    }
}