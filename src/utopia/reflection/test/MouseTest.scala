package utopia.reflection.test

import java.awt.Color

import javax.swing.JLabel
import utopia.flow.generic.DataType
import utopia.genesis.event.MouseMoveEvent
import utopia.genesis.handling.{MouseButtonStateListener, MouseMoveListener}
import utopia.genesis.shape.X
import utopia.genesis.shape.shape2D.Size
import utopia.inception.handling.immutable.Handleable
import utopia.reflection.component.{Area, JWrapper, Label, Wrapper}
import utopia.reflection.container.StackLayout.Fit
import utopia.reflection.container.{Frame, Stack}
import utopia.reflection.container.WindowResizePolicy.Program
import utopia.reflection.shape.{StackLength, StackSize}

/**
  * This app tests mouse listening in components
  * @author Mikko Hilpinen
  * @since 21.4.2019, v1+
  */
object MouseTest extends App
{
	DataType.setup()
	
	private class MouseEnterExitListener(val area: Area) extends MouseMoveListener with Handleable
	{
		override val mouseMoveEventFilter = e => e.enteredArea(area.bounds) || e.exitedArea(area.bounds)
		
		override def onMouseMove(event: MouseMoveEvent) = println("Mouse entered or exited area")
	}
	
	// Creates the basic components & wrap as Stackable
	def makeItem() =
	{
		val item = new Label().withStackSize(StackSize.any(Size(64, 64)))
		item.background = Color.BLUE
		item
	}
	
	// Creates the stack
	val stack = new Stack(X, Fit, StackLength.fixed(16), StackLength.fixed(16))
	val items = Vector.fill(3)(makeItem())
	stack ++= items
	
	stack.background = Color.ORANGE
	
	// Creates the frame
	val frame = Frame.windowed(stack, "Test", Program)
	frame.setToExitOnClose()
	
	// Sets up mouse listening
	items.head.addMouseMoveListener(new MouseEnterExitListener(items.head))
	items(2).addMouseButtonListener(MouseButtonStateListener.onLeftPressed(e => println(e.mousePosition)))
	
	// Starts the program
	frame.visible = true
}
