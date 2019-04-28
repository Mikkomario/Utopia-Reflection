package utopia.reflection.container.window

import java.awt.event.{ComponentAdapter, ComponentEvent}

import utopia.flow.async.VolatileFlag
import utopia.genesis.handling.mutable.ActorHandler
import utopia.genesis.handling._
import utopia.genesis.shape.shape2D.{Point, Size}
import utopia.genesis.view.{ConvertingKeyListener, MouseEventGenerator}
import utopia.inception.handling.Handleable
import utopia.reflection.component.Stackable
import utopia.reflection.shape.Insets
import utopia.reflection.util.Screen

/**
* This is a common wrapper for all window implementations
* @author Mikko Hilpinen
* @since 25.3.2019
**/
trait Window[Content <: Stackable] extends Stackable
{
    // ATTRIBUTES   ----------------
    
    private val generatorActivated = new VolatileFlag()
    
    
	// ABSTRACT    -----------------
    
    override def component: java.awt.Window
    
    /**
      * @return The title of this window
      */
    def title: String
    
    /**
     * The content displayed in this window
     */
    def content: Content
    
    /**
     * Whether the OS toolbar should be shown
     */
    def showsToolBar: Boolean
    
    /**
     * Whether this window is currently set to full screen mode
     */
    def fullScreen: Boolean
    
    /**
      * @return The current resize policy for this window
      */
    def resizePolicy: WindowResizePolicy
    
    
    // COMPUTED    ----------------
    
    /**
     * The insets in around this screen
     */
    def insets = Insets of component.getInsets
    
    
    // IMPLEMENTED    --------------
    
    // Overrides size and position because user may resize and move this Window at will, breaking the cached size / position logic
    override def size = Size(component.getWidth, component.getHeight)
    override def size_=(newSize: Size) = component.setSize(newSize.toDimension)
    
    override def position = Point(component.getX, component.getY)
    override def position_=(newPosition: Point) = component.setLocation(newPosition.toAwtPoint)
    
    override protected def calculatedStackSize =
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
    
    // Each time (content) layout is updated, may resize this window
    override def updateLayout() = updateWindowBounds(resizePolicy.allowsProgramResize)
	
	// Overrides listener functions to send them to content instead
	override def addMouseButtonListener(listener: MouseButtonStateListener) = content.addMouseButtonListener(listener)
	override def addMouseMoveListener(listener: MouseMoveListener) = content.addMouseMoveListener(listener)
	override def addMouseWheelListener(listener: MouseWheelListener) = content.addMouseWheelListener(listener)
	override def addKeyStateListener(listener: KeyStateListener) = content.addKeyStateListener(listener)
	override def addKeyTypedListener(listener: KeyTypedListener) = content.addKeyTypedListener(listener)
	override def removeListener(listener: Handleable) = content.removeListener(listener)
    
    
    // OTHER    --------------------
	
	/**
      * Starts mouse event generation for this window
      * @param actorHandler An actorhandler that generates the necessary action events
      */
    def startEventGenerators(actorHandler: ActorHandler) =
    {
        generatorActivated.runAndSet
        {
			// Starts mouse listening
            val mouseButtonListener = MouseButtonStateListener { e => content.distributeMouseButtonEvent(e); false }
            val mouseMovelistener = MouseMoveListener { content.distributeMouseMoveEvent(_) }
            val mouseWheelListener = MouseWheelListener { content.distributeMouseWheelEvent(_) }
            
            actorHandler += new MouseEventGenerator(content.component, mouseMovelistener, mouseButtonListener,
                mouseWheelListener, () => 1.0)
			
			// Starts key listening
			val keyStateListener = KeyStateListener { content.distributeKeyStateEvent(_) }
			val keyTypedListener = KeyTypedListener { content.distributeKeyTypedEvent(_) }
			
			component.addKeyListener(new ConvertingKeyListener(keyStateListener, keyTypedListener))
        }
    }
    
    /**
      * Updates the bounds of this window's contents to match those of this window
      */
    protected def updateContentBounds() = content.size = size - insets.total
    
    /**
      * Starts following component resizes, updating content size on each resize
      */
    protected def activateResizeHandling() =
    {
        component.addComponentListener(new ComponentAdapter
        {
            // Resizes content each time this window is resized
            // TODO: This will not limit user's ability to resize window beyond minimum and maximum
            override def componentResized(e: ComponentEvent) = updateContentBounds()
        })
    }
    
    /**
      * Updates this window's bounds according to changes either outside or inside this window
      * @param dictateSize Whether this window should dictate the resulting size (Full screen windows always dictate their size)
      */
    protected def updateWindowBounds(dictateSize: Boolean) =
    {
        if (fullScreen)
        {
            // Full screen mode always dictates size & position
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
                checkWindowBounds()
        }
    }
    
    /**
      * Makes sure the window bounds are within stackSize limits
      */
    protected def checkWindowBounds() =
    {
        // Checks current bounds against allowed limits
        stackSize.maxWidth.filter { _ < width }.foreach { width = _ }
        stackSize.maxHeight.filter { _ < height }.foreach { height = _ }
        
        if (isUnderSized)
            size = size max stackSize.min
    }
    
    /**
     * Centers this window on the screen
     */
    def centerOnScreen() = centerOn(null)
    
    /**
     * Centers this window on the screen or on the parent component
     */
    // TODO: This method is redundant in Frame, which has no parent
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