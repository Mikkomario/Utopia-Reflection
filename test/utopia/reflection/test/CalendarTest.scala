package utopia.reflection.test

import java.nio.file.Paths
import java.time.format.TextStyle
import java.time.{DayOfWeek, Month, Year}
import java.util.Locale

import utopia.flow.async.ThreadPool
import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.reflection.shape.LengthExtensions._
import utopia.genesis.color.Color
import utopia.genesis.generic.GenesisDataType
import utopia.genesis.handling.ActorLoop
import utopia.genesis.handling.mutable.ActorHandler
import utopia.genesis.image.Image
import utopia.genesis.shape.shape2D.Size
import utopia.reflection.component.Alignment
import utopia.reflection.component.drawing.{CustomDrawableWrapper, SelectionCircleDrawer}
import utopia.reflection.component.input.InteractionWithPointer
import utopia.reflection.component.swing.label.ItemLabel
import utopia.reflection.component.swing.{Calendar, DropDown, StackableAwtComponentWrapperWrapper}
import utopia.reflection.component.swing.button.{ButtonImageSet, CustomDrawableButtonLike}
import utopia.reflection.container.stack.StackHierarchyManager
import utopia.reflection.container.swing.Framing
import utopia.reflection.container.swing.window.Frame
import utopia.reflection.container.swing.window.WindowResizePolicy.User
import utopia.reflection.localization.{DisplayFunction, Localizer, NoLocalization}
import utopia.reflection.shape.StackSize
import utopia.reflection.text.Font
import utopia.reflection.text.FontStyle.Plain

import scala.concurrent.ExecutionContext

/**
  * Tests calendar component visually
  * @author Mikko Hilpinen
  * @since 3.8.2019, v1+
  */
object CalendarTest extends App
{
	GenesisDataType.setup()
	
	// Sets up localization context
	implicit val defaultLanguageCode: String = "EN"
	implicit val localizer: Localizer = NoLocalization
	
	val basicFont = Font("Arial", 14, Plain, 2)
	val smallFont = basicFont * 0.75
	
	private class DateLabel(val date: Int) extends StackableAwtComponentWrapperWrapper with CustomDrawableWrapper
		with CustomDrawableButtonLike with InteractionWithPointer[Boolean]
	{
		// ATTRIBUTES	-----------------
		
		private val label = new ItemLabel[Int](date, DisplayFunction.raw, smallFont, StackSize.any)
		
		override val valuePointer = new PointerWithEvents[Boolean](false)
		
		
		// INITIAL CODE	-----------------
		
		label.alignCenter()
		addCustomDrawer(new SelectionCircleDrawer(Color.black.withAlpha(0.33), Color.cyan,
			() => value, () => state))
		
		valuePointer.addListener { _ => repaint() }
		registerAction(() => value = !value)
		initializeListeners()
		setHandCursor()
		
		
		// IMPLEMENTED	-----------------
		
		override protected def wrapped = label
		
		override def drawable = label
	}
	
	val yearSelect = new DropDown[Year](16.any x 4.upscaling, "Year", basicFont,
		Color.white, Color.magenta, initialContent = (1999 to 2050).map {Year.of}.toVector)
	val monthSelect = new DropDown[Month](16.any x 4.upscaling, "Month", basicFont,
		Color.white, Color.magenta, initialContent = Month.values().toVector)
	
	val buttonImage = Image.readFrom(Paths.get("test-images/arrow-back-48dp.png")).get
	val backImages = ButtonImageSet.varyingAlpha(buttonImage, 0.66, 1)
	val forwardImages = ButtonImageSet.varyingAlpha(buttonImage.flippedHorizontally, 0.66, 1)
	
	private def makeWeekDayLabel(day: DayOfWeek) = new ItemLabel[DayOfWeek](day,
		DisplayFunction.noLocalization[DayOfWeek] { _.getDisplayName(TextStyle.SHORT, Locale.getDefault) }, smallFont,
		initialAlignment = Alignment.Center)
	private def makeDateLabel(date: Int) = new DateLabel(date)
	
	val calendar = new Calendar(monthSelect, yearSelect, forwardImages, backImages, 8.any, 8.any, StackSize.fixed(Size.zero),
		makeWeekDayLabel, makeDateLabel)
	calendar.addValueListener { e => println(s"New selected date: ${e.newValue}") }
	
	// Creates the frame and displays it
	val actorHandler = ActorHandler()
	val actionLoop = new ActorLoop(actorHandler)
	implicit val context: ExecutionContext = new ThreadPool("Reflection").executionContext
	
	val framing = new Framing(calendar, 24.downscaling.square)
	framing.background = Color.white
	val frame = Frame.windowed(framing, "Calendar Test", User)
	frame.setToExitOnClose()
	
	actionLoop.registerToStopOnceJVMCloses()
	actionLoop.startAsync()
	StackHierarchyManager.startRevalidationLoop()
	frame.startEventGenerators(actorHandler)
	frame.isVisible = true
}
