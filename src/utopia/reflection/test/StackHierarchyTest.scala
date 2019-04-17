package utopia.reflection.test

import java.awt.Color
import java.time.Duration

import javax.swing.JLabel
import utopia.flow.async.{Loop, ThreadPool}
import utopia.flow.generic.DataType
import utopia.genesis.shape.X
import utopia.genesis.shape.shape2D.Size
import utopia.reflection.component.{JStackable, JWrapper}
import utopia.reflection.container.{Frame, Stack, StackHierarchyManager}
import utopia.reflection.container.StackLayout.Fit
import utopia.reflection.container.WindowResizePolicy.Program
import utopia.reflection.shape.{StackLength, StackSize}

import scala.concurrent.ExecutionContext

/**
 * This test creates a simple stack and sees whether the components are positioned properly
 * @author Mikko Hilpinen
 * @since 26.3.2019
 */
object StackHierarchyTest extends App
{
    DataType.setup()
    
    private class ChangingWrapper extends JStackable
    {
        // ATTRIBUTES   -----------------
        
        val component = new JLabel()
        
        private var currentSize = StackSize.fixed(Size(64, 64))
        private var isBuffed = false
        
        
        // INITIAL CODE -----------------
        
        component.setBackground(Color.RED)
        component.setOpaque(true)
        
        
        // IMPLEMENTED  -----------------
        
        override def updateLayout() = Unit
    
        override protected def calculatedStackSize =
        {
            println("Requesting up-to-date stack size calculation")
            currentSize
        }
        
        
        // OTHER    ---------------------
        
        def pulse() =
        {
            if (isBuffed)
                currentSize /= 2
            else
                currentSize *= 2
            
            isBuffed = !isBuffed
            revalidate()
        }
    }
    
    // Creates the basic components & wrap as Stackable
    def makeItem() = 
    {
        val item = JWrapper(new JLabel()).withStackSize(StackSize.any(Size(64, 64)))
        item.background = Color.BLUE
        item
    }
    
    // Creates the stack
    private val item = new ChangingWrapper()
    val stack = new Stack(X, Fit, StackLength.fixed(16), StackLength.fixed(16))
    stack ++= Vector.fill(3)(makeItem())
    stack += item
    
    stack.background = Color.ORANGE
    
    // Creates the frame
    val frame = Frame.windowed(stack, "Test", Program)
    frame.setToExitOnClose()
    
    // The last item will pulse every second
    implicit val context: ExecutionContext = new ThreadPool("Test").executionContext
    
    val pulseLoop = Loop(Duration.ofSeconds(1), () => item.pulse())
    pulseLoop.registerToStopOnceJVMCloses()
    
    // Start the program
    pulseLoop.startAsync()
    StackHierarchyManager.startRevalidationLoop()
    
    frame.visible = true
}