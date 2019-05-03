package utopia.reflection.test

import utopia.flow.generic.ValueConversions._
import utopia.flow.async.ThreadPool
import utopia.genesis.color.Color
import utopia.genesis.generic.GenesisDataType
import utopia.genesis.handling.ActorLoop
import utopia.genesis.handling.mutable.ActorHandler
import utopia.genesis.shape.X
import utopia.reflection.component.swing.{FilterDocument, TextField}
import utopia.reflection.component.swing.label.TextLabel
import utopia.reflection.container.stack.StackHierarchyManager
import utopia.reflection.container.stack.StackLayout.Fit
import utopia.reflection.container.swing.window.Frame
import utopia.reflection.container.swing.window.WindowResizePolicy.User
import utopia.reflection.container.swing.Stack
import utopia.reflection.localization.{Localizer, NoLocalization}
import utopia.reflection.shape.LengthExtensions._
import utopia.reflection.text.{Font, Prompt, Regex}
import utopia.reflection.text.FontStyle.Plain

import scala.concurrent.ExecutionContext

/**
  * This is a simple test implementation of text fields with content filtering
  * @author Mikko Hilpinen
  * @since 24.4.2019, v1+
  */
object TextFieldTest extends App
{
	GenesisDataType.setup()
	
	// Sets up localization context
	implicit val defaultLanguageCode: String = "EN"
	implicit val localizer: Localizer = NoLocalization
	
	// Creates the hint labels
	val basicFont = Font("Arial", 12, Plain, 2)
	val labels = Vector("Product", "Amount", "Price").map { s => TextLabel(s, basicFont, 8.any x 0.any) }
	labels.foreach
	{
		l =>
			l.textColor = Color.textBlackDisabled
			l.alignBottom()
			l.alignLeft()
	}
	
	// Creates three text fields
	val productPrompt = Prompt("Describe product", basicFont)
	val productField = new TextField(320.downTo(128), 4.upTo(8), basicFont, prompt = Some(productPrompt))
	
	val amountDoc = FilterDocument(Regex.digit, 3)
	val amountPrompt = Prompt("1-999", basicFont)
	val amountField = new TextField(128.downTo(64), 4.upTo(8), basicFont, prompt = Some(amountPrompt), document = amountDoc)
	
	val priceDoc = FilterDocument(Regex.decimalPositiveParts, 5)
	val pricePrompt = Prompt("€", basicFont)
	val priceField = new TextField(160.downTo(64), 4.upTo(8), basicFont, prompt = Some(pricePrompt),
		document = priceDoc, resultFilter = Some(Regex.decimalPositive))
	
	// Creates the stacks
	def combine(label: TextLabel, field: TextField) = label.columnWith(Vector(field), 4.downscaling)
	val productStack = combine(labels(0), productField)
	val amountStack = combine(labels(1), amountField)
	val priceStack = combine(labels(2), priceField)
	
	val stack = Stack.withItems(X, Fit, 8.downscaling, 0.fixed, Vector(productStack, amountStack, priceStack))
	
	// Adds listening to field(s)
	priceField.addEnterListener
	{
		p =>
			val product = productField.value
			val amount = amountField.intValue
			val price = p.double
			
			if (product.isDefined && amount.isDefined && price.isDefined)
			{
				println(s"${amount.get} x ${product.get} = ${amount.get * price.get} €")
				productField.clear()
				amountField.clear()
				priceField.clear()
				productField.requestFocus()
			}
			else
				println("Please select product + amount + price")
	}
	
	// Creates the frame and displays it
	val actorHandler = ActorHandler()
	val actionLoop = new ActorLoop(actorHandler)
	implicit val context: ExecutionContext = new ThreadPool("Reflection").executionContext
	
	val framing = stack.framed(16.any x 8.any)
	framing.background = Color.yellow
	val frame = Frame.windowed(framing, "TextLabel Stack Test", User)
	frame.setToExitOnClose()
	
	actionLoop.registerToStopOnceJVMCloses()
	actionLoop.startAsync()
	StackHierarchyManager.startRevalidationLoop()
	frame.startEventGenerators(actorHandler)
	frame.isVisible = true
}
