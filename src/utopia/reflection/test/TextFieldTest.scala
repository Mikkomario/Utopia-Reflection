package utopia.reflection.test

import utopia.flow.generic.ValueConversions._
import utopia.flow.async.ThreadPool
import utopia.genesis.color.Color
import utopia.genesis.generic.GenesisDataType
import utopia.genesis.handling.ActorLoop
import utopia.genesis.handling.mutable.ActorHandler
import utopia.genesis.shape.X
import utopia.reflection.component.swing.{TabSelection, TextField}
import utopia.reflection.component.swing.label.TextLabel
import utopia.reflection.container.stack.StackHierarchyManager
import utopia.reflection.container.stack.StackLayout.Fit
import utopia.reflection.container.swing.window.Frame
import utopia.reflection.container.swing.window.WindowResizePolicy.User
import utopia.reflection.container.swing.{Framing, Stack}
import utopia.reflection.localization.{Localizer, NoLocalization}
import utopia.reflection.shape.LengthExtensions._
import utopia.reflection.shape.StackLength
import utopia.reflection.text.{Font, Prompt}
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
	productField.alignLeft(16)
	
	val amountPrompt = Prompt("1-999", basicFont)
	val amountField = TextField.forPositiveInts(128.downTo(64), 4.upTo(8), basicFont, prompt = Some(amountPrompt))
	
	val pricePrompt = Prompt("€", basicFont)
	val priceField = TextField.forPositiveDoubles(160.downTo(64), 4.upTo(8), basicFont, prompt = Some(pricePrompt))
	
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
	
	// Also creates a tab selection because testing
	val color = Color.magenta
	val tab = new TabSelection[String](basicFont, color, 16, StackLength(0, 4, 8),
		initialChoices = Vector("Goods", "for", "Purchase"))
	tab.addListener { s => println(s.getOrElse("No item") + " selected") }
	
	val framing1 = new Framing(stack, 8.downscaling x 8.downscaling)
	framing1.background = color
	
	val stack2 = tab.columnWith(Vector(framing1), 0.fixed)
	
	// Creates the frame and displays it
	val actorHandler = ActorHandler()
	val actionLoop = new ActorLoop(actorHandler)
	implicit val context: ExecutionContext = new ThreadPool("Reflection").executionContext
	
	val framing2 = stack2.framed(16.any x 8.any)
	framing2.background = Color.white
	val frame = Frame.windowed(framing2, "TextLabel Stack Test", User)
	frame.setToExitOnClose()
	
	actionLoop.registerToStopOnceJVMCloses()
	actionLoop.startAsync()
	StackHierarchyManager.startRevalidationLoop()
	frame.startEventGenerators(actorHandler)
	frame.isVisible = true
}
