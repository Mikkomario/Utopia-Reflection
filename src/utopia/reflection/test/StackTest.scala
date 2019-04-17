package utopia.reflection.test

import javax.swing.JLabel
import utopia.reflection.component.Wrapper
import utopia.reflection.shape.StackSize
import utopia.genesis.shape.shape2D.Size
import utopia.reflection.component.JWrapper
import java.awt.Color
import utopia.reflection.container.Stack
import utopia.genesis.shape.X
import utopia.reflection.container.StackLayout.Fit
import utopia.reflection.shape.StackLength
import utopia.reflection.container.Frame
import utopia.reflection.event.ResizeListener
import utopia.flow.generic.DataType

/**
 * This test creates a simple stack and sees whether the components are positioned properly
 * @author Mikko Hilpinen
 * @since 26.3.2019
 */
object StackTest extends App
{
    DataType.setup()
    
    // Creates the basic components & wrap as Stackable
    def makeItem() = 
    {
        val item = JWrapper(new JLabel()).withStackSize(StackSize.any(Size(64, 64)))
        item.background = Color.CYAN
        item
    }
    
    // Creates the stack
    val stack = new Stack(X, Fit, StackLength.fixed(16), StackLength.fixed(16))
    stack ++= Vector.fill(3)(makeItem())
    
    stack.resizeListeners :+= ResizeListener(e => println(e.newSize))
    stack.background = Color.ORANGE
    
    // Creates the frame
    val frame = Frame.windowed(stack, "Test")
    frame.setToExitOnClose()
    
    // Start the program
    frame.visible = true
}