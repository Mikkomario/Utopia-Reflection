package utopia.reflection.test

import utopia.flow.async.ThreadPool
import utopia.genesis.color.Color
import utopia.genesis.generic.GenesisDataType
import utopia.genesis.handling.ActorLoop
import utopia.genesis.handling.mutable.ActorHandler
import utopia.genesis.shape.Y
import utopia.reflection.component.swing.label.ItemLabel
import utopia.reflection.container.stack.{BoxScrollBarDrawer, StackHierarchyManager}
import utopia.reflection.container.stack.StackLayout.Fit
import utopia.reflection.container.swing.window.Frame
import utopia.reflection.container.swing.window.WindowResizePolicy.User
import utopia.reflection.container.swing.{ScrollView, Stack}
import utopia.reflection.localization.{DisplayFunction, Localizer, NoLocalization}
import utopia.reflection.shape.LengthExtensions._
import utopia.reflection.text.Font
import utopia.reflection.text.FontStyle.Plain

import scala.concurrent.ExecutionContext

/**
  * This is a simple test implementation of scroll view
  * @author Mikko Hilpinen
  * @since 24.4.2019, v1+
  */
object ScrollViewTest extends App
{
	GenesisDataType.setup()
	
	// Sets up localization context
	implicit val defaultLanguageCode: String = "EN"
	implicit val localizer: Localizer = NoLocalization
	
	// Creates the labels
	val basicFont = Font("Arial", 12, Plain, 2)
	val labels = (1 to 50).toVector.map { i => new ItemLabel(i,
		DisplayFunction.noLocalization[Int] { n => s"Label number $n" }, basicFont, 16.any x 4.fixed) }
	labels.foreach { _.background = Color.yellow }
	labels.foreach { _.alignCenter() }
	
	// Creates the main stack
	val stack = Stack.withItems(Y, Fit, 8.fixed, 4.fixed, labels)
	stack.background = Color.yellow.minusHue(33).darkened(1.2)
	
	val actorHandler = ActorHandler()
	
	// Creates the scroll view
	val scrollView = new ScrollView(stack, Y, actorHandler, 16,
		BoxScrollBarDrawer(Color.gray(0.33), Color.gray(0.55)), 16,
		maxOptimalLength = Some(480))
	
	// Creates the frame and displays it
	val actionLoop = new ActorLoop(actorHandler)
	implicit val context: ExecutionContext = new ThreadPool("Reflection").executionContext
	
	val frame = Frame.windowed(scrollView, "Scroll View Test", User)
	frame.setToExitOnClose()
	
	actionLoop.registerToStopOnceJVMCloses()
	actionLoop.startAsync()
	StackHierarchyManager.startRevalidationLoop()
	frame.startEventGenerators(actorHandler)
	frame.isVisible = true
}
