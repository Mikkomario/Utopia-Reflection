package utopia.reflection.test

import utopia.flow.async.{Loop, ThreadPool}
import utopia.genesis.color.{Color, RGB}
import utopia.genesis.generic.GenesisDataType
import utopia.genesis.handling.mutable.ActorHandler
import utopia.genesis.shape.shape2D.{Direction2D, Size}
import utopia.reflection.component.swing.SearchFromField
import utopia.reflection.component.swing.button.TextButton
import utopia.reflection.container.swing.Stack
import utopia.reflection.container.swing.Stack.AwtStackable
import utopia.reflection.container.swing.window.Frame
import utopia.reflection.container.swing.window.WindowResizePolicy.Program
import utopia.reflection.localization.{Localizer, NoLocalization}
import utopia.reflection.shape.StackSize
import utopia.reflection.text.Font
import utopia.reflection.text.FontStyle.Plain
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder, SingleFrameSetup}
import utopia.reflection.shape.LengthExtensions._

import scala.concurrent.ExecutionContext

/**
  * Tests SearchFromField
  * @author Mikko Hilpinen
  * @since 29.2.2020, v1
  */
object SearchFromFieldTest extends App
{
	GenesisDataType.setup()
	
	// Sets up localization context
	implicit val defaultLanguageCode: String = "EN"
	implicit val localizer: Localizer = NoLocalization
	
	// Creates component context
	val actorHandler = ActorHandler()
	val baseCB = ComponentContextBuilder(actorHandler, Font("Arial", 12, Plain, 2), Color.green, Color.yellow, 320,
		insideMargins = 8.any x 8.any, stackMargin = 8.downscaling, relatedItemsStackMargin = Some(4.downscaling))
	
	implicit val baseContext: ComponentContext = baseCB.result
	implicit val exc: ExecutionContext = new ThreadPool("Reflection").executionContext
	
	val field = SearchFromField.contextualWithTextOnly[String]("Search for string")
	val button = TextButton.contextual("OK", () => println(field.value))
	val content = Stack.buildColumnWithContext() { s =>
		s += field
		s += button
	}.framed(16.any x 16.any, Color.black)
	
	field.content = Vector("The first string", "Another piece of text", "More text", "Lorem ipsum", "Tramboliini",
		"Keijupuisto", "Ääkkösiä", "Pulppura", "Potentiaalinen koneisto")
	
	val frame = Frame.windowed(content, "Search Field Test", Program)
	frame.setToCloseOnEsc()
	new SingleFrameSetup(actorHandler, frame).start()
}
