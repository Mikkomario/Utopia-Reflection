package utopia.reflection.test

import utopia.reflection.shape.LengthExtensions._
import utopia.flow.async.ThreadPool
import utopia.genesis.color.Color
import utopia.genesis.generic.GenesisDataType
import utopia.genesis.handling.ActorLoop
import utopia.genesis.handling.mutable.ActorHandler
import utopia.genesis.shape.Y
import utopia.reflection.component.Button
import utopia.reflection.component.label.TextLabel
import utopia.reflection.container.{Frame, Stack}
import utopia.reflection.container.StackLayout.Leading
import utopia.reflection.container.WindowResizePolicy.User
import utopia.reflection.localization.{Localizer, NoLocalization}
import utopia.reflection.text.Font
import utopia.reflection.text.FontStyle.Plain

import scala.concurrent.ExecutionContext

/**
  * This is a simple test implementation of text labels in a stack
  * @author Mikko Hilpinen
  * @since 24.4.2019, v1+
  */
object TextLabelStackTest extends App
{
	GenesisDataType.setup()
	
	// Sets up localization context
	implicit val defaultLanguageCode: String = "EN"
	implicit val localizer: Localizer = NoLocalization
	
	// Creates the labels
	val basicFont = Font("Arial", 12, Plain, 2)
	val labels = Vector("Here are some labels", "just", "for you").map { s => TextLabel(s, basicFont, 16.any x 0.fixed) }
	labels.foreach { _.background = Color.yellow }
	
	// Creates a button too
	val button = new Button("A Button!", basicFont, Color.magenta, 32.any x 8.any, () => println("The Button was pressed"))
	
	// Creates the stack
	val stack = new Stack(Y, Leading, 8.any, 16.any)
	stack ++= labels
	stack += button
	stack.background = Color.black
	
	// Creates the frame and displays it
	val actorHandler = ActorHandler()
	val actionLoop = new ActorLoop(actorHandler)
	implicit val context: ExecutionContext = new ThreadPool("Reflection").executionContext
	
	val frame = Frame.windowed(stack, "TextLabel Stack Test", User)
	frame.setToExitOnClose()
	
	actionLoop.registerToStopOnceJVMCloses()
	actionLoop.startAsync()
	frame.startEventGenerators(actorHandler)
	frame.isVisible = true
}
