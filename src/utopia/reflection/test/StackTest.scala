package utopia.reflection.test

import javax.swing.JLabel
import utopia.reflection.shape.StackSize
import utopia.genesis.shape.shape2D.Size
import utopia.reflection.component.JWrapper
import java.awt.Color

import utopia.genesis.shape.X
import utopia.reflection.shape.StackLength
import utopia.reflection.event.ResizeListener
import utopia.flow.generic.DataType
import utopia.reflection.container.stack.Stack
import utopia.reflection.container.stack.StackLayout.Fit
import utopia.reflection.container.window.Frame

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
    val stack = Stack.withItems(X, Fit, StackLength.fixed(16), StackLength.fixed(16), Vector.fill(3)(makeItem()))
    
    stack.resizeListeners :+= ResizeListener(e => println(e.newSize))
    stack.background = Color.ORANGE
    
    // Creates the frame
    val frame = Frame.windowed(stack, "Test")
    frame.setToExitOnClose()
    
    // Start the program
    frame.isVisible = true
}