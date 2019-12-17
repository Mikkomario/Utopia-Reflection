package utopia.reflection.test

import utopia.flow.async.ThreadPool
import utopia.reflection.shape.LengthExtensions._
import utopia.genesis.color.Color
import utopia.genesis.generic.GenesisDataType
import utopia.genesis.handling.mutable.ActorHandler
import utopia.genesis.shape.shape2D.Size
import utopia.reflection.component.swing.{DropDown, MultiLineTextView, TextField}
import utopia.reflection.container.swing.Stack
import utopia.reflection.container.swing.window.Frame
import utopia.reflection.localization.{LocalString, Localizer, NoLocalization}
import utopia.reflection.shape.{Alignment, StackSize}
import utopia.reflection.text.Font
import utopia.reflection.text.FontStyle.Plain
import utopia.reflection.util.{ComponentContextBuilder, SingleFrameSetup}

import scala.concurrent.ExecutionContext

/**
  * Tests text display with multiple lines
  * @author Mikko Hilpinen
  * @since 17.12.2019, v1+
  */
object MultiLineTextViewTest extends App
{
	GenesisDataType.setup()
	
	// Sets up localization context
	implicit val defaultLanguageCode: String = "EN"
	implicit val localizer: Localizer = NoLocalization
	
	// Creates component context
	val actorHandler = ActorHandler()
	val baseCB = ComponentContextBuilder(actorHandler, Font("Arial", 12, Plain, 2), Color.green, Color.yellow, 320,
		insideMargins = 8.any x 8.any, stackMargin = 8.downscaling, relatedItemsStackMargin = Some(4.downscaling))
	
	val content = baseCB.use { implicit base =>
		Stack.buildColumnWithContext() { mainStack =>
			val textView = MultiLineTextView.contextual("Please type in some text and then press enter",
				useLowPriorityForScalingSides = true)(baseCB.withInnerMargins(StackSize.fixed(Size.zero)).result)
			mainStack += textView
			
			mainStack += Stack.buildRowWithContext(isRelated = true) { bottomRow =>
				val textInput = TextField.contextual(prompt = Some("Type your own text and press enter"))
				textInput.addEnterListener { _.foreach { s => textView.text = (s: LocalString).localizationSkipped } }
				
				bottomRow += textInput
				
				val alignSelect = DropDown.contextual("Select Alignment", initialChoices = Alignment.values)
				alignSelect.selectOne(Alignment.Left)
				alignSelect.addValueListener { _.newValue.foreach { a => textView.alignment = a } }
				
				bottomRow += alignSelect
			}
		}.framed(base.insideMargins, Color.white)
	}
	
	implicit val exc: ExecutionContext = new ThreadPool("Reflection").executionContext
	new SingleFrameSetup(actorHandler, Frame.windowed(content, "Multi Line Text View Test")).start()
}
