package utopia.reflection.test

import utopia.flow.async.ThreadPool
import utopia.genesis.color.Color
import utopia.genesis.generic.GenesisDataType
import utopia.genesis.handling.ActorLoop
import utopia.genesis.handling.mutable.ActorHandler
import utopia.genesis.shape.{X, Y}
import utopia.reflection.component.swing.Button
import utopia.reflection.component.swing.label.TextLabel
import utopia.reflection.container.stack.StackLayout.Fit
import utopia.reflection.container.stack.segmented.SegmentedGroup
import utopia.reflection.container.stack.StackHierarchyManager
import utopia.reflection.container.swing.{SegmentedRow, Stack}
import utopia.reflection.container.swing.window.Frame
import utopia.reflection.container.swing.window.WindowResizePolicy.User
import utopia.reflection.localization.{Localizer, NoLocalization}
import utopia.reflection.shape.LengthExtensions._
import utopia.reflection.text.Font
import utopia.reflection.text.FontStyle.Plain

import scala.concurrent.ExecutionContext

/**
  * This is a simple test implementation of segmented rows
  * @author Mikko Hilpinen
  * @since 24.4.2019, v1+
  */
object SegmentedRowTest extends App
{
	GenesisDataType.setup()
	
	// Sets up localization context
	implicit val defaultLanguageCode: String = "EN"
	implicit val localizer: Localizer = NoLocalization
	
	// Creates the labels
	val basicFont = Font("Arial", 12, Plain, 2)
	val labels = Vector("Here are some labels", "just for you", "once", "again!").map { s => TextLabel(s, basicFont, 16.any x 0.any) }
	labels.foreach { _.background = Color.yellow }
	labels.foreach { _.alignCenter() }
	
	// Creates a button too
	val largeFont = basicFont * 1.2
	
	val button1 = new Button("Yeah!", largeFont, Color.magenta, 32.any x 8.any, 4, () => labels(1).text += "!")
	val button2 = new Button("For Sure!", largeFont, Color.magenta, 32.any x 8.any, 4, () => labels(3).text += "!")
	
	// Creates the rows
	val hGroup = new SegmentedGroup(X)
	val row1 = SegmentedRow.partOfGroupWithItems(hGroup, Fit, 8.fixed, 0.fixed, Vector(labels(0), labels(1)))
	val row2 = SegmentedRow.partOfGroupWithItems(hGroup, Fit, 16.fixed, 4.fixed, Vector(labels(2), labels(3)))
	row1.background = Color.cyan
	row2.background = Color.green
	
	// Creates the columns
	val vGroup = new SegmentedGroup(Y)
	val column1 = SegmentedRow.partOfGroupWithItems(vGroup, Fit, 4.any, 0.fixed, Vector(row1, row2))
	val column2 = SegmentedRow.partOfGroupWithItems(vGroup, Fit, 2.upscaling, 2.upscaling, Vector(button1, button2))
	
	// Creates the main stack
	val stack = Stack.withItems(X, Fit, 16.any, 0.fixed, Vector(column1, column2))
	
	// val stack = Stack.withItems(Y, Fit, 16.any, 0.fixed, Vector(row1, row2))
	stack.background = Color.black
	
	
	// Creates the frame and displays it
	val actorHandler = ActorHandler()
	val actionLoop = new ActorLoop(actorHandler)
	implicit val context: ExecutionContext = new ThreadPool("Reflection").executionContext
	
	val frame = Frame.windowed(stack, "Segmented Row Test", User)
	frame.setToExitOnClose()
	
	actionLoop.registerToStopOnceJVMCloses()
	actionLoop.startAsync()
	StackHierarchyManager.startRevalidationLoop()
	frame.startEventGenerators(actorHandler)
	frame.isVisible = true
}
