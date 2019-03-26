package utopia.reflection.container

import utopia.reflection.component.Stackable
import utopia.reflection.component.JWrapper
import javax.swing.JFrame

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
    
    private val _component = new MyFrame(title, borderless, content.component)
    
    private var _fullScreen = startFullScreen
    private var _showsToolBar = startWithToolBar
    
    
    // INITIAL CODE    -----------------
    
    // TODO: Set size & position
    
    
	// IMPLEMENTED    ------------------
    
    def component: JFrame = _component
     
    def fullScreen: Boolean = _fullScreen
    def fullScreen_=(newStatus: Boolean) = _fullScreen = newStatus // TODO: Resize and reposition
     
    def showsToolBar: Boolean = _showsToolBar
    def showsToolBar_=(newStatus: Boolean) = _showsToolBar = newStatus // TODO: Resize if necessary
}

private class MyFrame(title: String, borderless: Boolean, contentPanel: java.awt.Container) extends JFrame(title)
{
    // INITIAL CODE    -----------------
    
    setContentPane(contentPanel)
    setUndecorated(borderless)
    pack()
    
    // TODO: Handle content validation
}