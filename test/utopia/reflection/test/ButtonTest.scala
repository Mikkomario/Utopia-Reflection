package utopia.reflection.test

import java.nio.file.Paths

import utopia.flow.async.ThreadPool
import utopia.reflection.shape.LengthExtensions._
import utopia.genesis.color.Color
import utopia.genesis.handling.ActorLoop
import utopia.genesis.handling.mutable.ActorHandler
import utopia.genesis.image.Image
import utopia.genesis.shape.shape2D.Size
import utopia.reflection.component.Alignment
import utopia.reflection.component.swing.label.TextButton
import utopia.reflection.component.swing.{ButtonImageSet, ImageAndTextButton, ImageButton}
import utopia.reflection.container.stack.StackHierarchyManager
import utopia.reflection.container.stack.StackLayout.{Center, Fit}
import utopia.reflection.container.swing.window.Frame
import utopia.reflection.container.swing.window.WindowResizePolicy.User
import utopia.reflection.localization.{Localizer, NoLocalization}
import utopia.reflection.text.Font
import utopia.reflection.text.FontStyle.Plain

import scala.concurrent.ExecutionContext

/**
  * Used for visually testing buttons
  * @author Mikko Hilpinen
  * @since 1.8.2019, v1+
  */
object ButtonTest extends App
{
	private def run() =
	{
		implicit val defaultLanguageCode: String = "EN"
		implicit val localizer: Localizer = NoLocalization
		val basicFont = Font("Arial", 12, Plain, 2)
		
		val image = Image.readFrom(Paths.get("test-images/mushrooms.png")).get.withSize(Size(64, 64)).downscaled
		val images = ButtonImageSet.brightening(image)
		
		println(s"Default: ${images.defaultImage.hashCode()}")
		println(s"Hover: ${images.focusImage.hashCode()}")
		println(s"Pressed: ${images.actionImage.hashCode()}")
		
		val action = () => println("Button pressed!")
		val color = Color.magenta
		val textMargins = 8.any x 4.any
		val borderWitdh = 2
		
		// Creates the buttons
		val imageButton = new ImageButton(images, action)
		val textButton = new TextButton("Text Button", basicFont, color, textMargins, borderWitdh, action)
		val comboButton = new ImageAndTextButton(images, "Button", basicFont, color, textMargins, borderWitdh,
			4.downscaling, Alignment.Left, action)
		
		val row = imageButton.rowWith(Vector(textButton, comboButton), margin = 16.any, layout = Fit)
		
		// Creates the frame and displays it
		val actorHandler = ActorHandler()
		val actionLoop = new ActorLoop(actorHandler)
		implicit val context: ExecutionContext = new ThreadPool("Reflection").executionContext
		
		val framing = row.framed(16.any x 8.any)
		framing.background = Color.white
		val frame = Frame.windowed(framing, "Button Test", User)
		frame.setToExitOnClose()
		
		actionLoop.registerToStopOnceJVMCloses()
		actionLoop.startAsync()
		StackHierarchyManager.startRevalidationLoop()
		frame.startEventGenerators(actorHandler)
		frame.isVisible = true
	}
	
	run()
}
