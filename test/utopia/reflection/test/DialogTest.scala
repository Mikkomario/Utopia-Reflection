package utopia.reflection.test

import utopia.flow.async.ThreadPool
import utopia.reflection.shape.LengthExtensions._
import utopia.genesis.color.Color
import utopia.reflection.component.ComponentLike
import utopia.reflection.component.stack.Stackable
import utopia.reflection.component.swing.AwtComponentRelated
import utopia.reflection.container.swing.Panel
import utopia.reflection.container.swing.window.{Dialog, Frame}
import utopia.reflection.shape.StackSize

import scala.concurrent.ExecutionContext

/**
  * This is a test implementation for dialogs
  * @author Mikko Hilpinen
  * @since 8.5.2019, v1+
  */
object DialogTest extends App
{
	private class ContentPanel(override val stackSize: StackSize) extends Panel[ComponentLike with AwtComponentRelated] with Stackable
	{
		background = Color.white
		
		override def stackId = hashCode()
		
		override def updateLayout() = Unit
		override def resetCachedSize() = Unit
	}
	
	private val frame = Frame.windowed(new ContentPanel(640.any x 480.any), "Frame")
	frame.setToExitOnClose()
	
	private val dialog = new Dialog(frame.component, new ContentPanel(320.any x 240.any), "Dialog")
	implicit val exc: ExecutionContext = new ThreadPool("Reflection").executionContext
	dialog.closeFuture.foreach { u => frame.background = Color.yellow }
	
	frame.isVisible = true
	dialog.isVisible = true
}
