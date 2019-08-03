package utopia.reflection.test

import java.nio.file.Paths
import java.time.format.TextStyle
import java.time.{DayOfWeek, Month, Year}
import java.util.Locale

import utopia.reflection.shape.LengthExtensions._
import utopia.genesis.color.Color
import utopia.genesis.generic.GenesisDataType
import utopia.genesis.image.Image
import utopia.reflection.component.swing.label.ItemLabel
import utopia.reflection.component.swing.DropDown
import utopia.reflection.component.swing.button.ButtonImageSet
import utopia.reflection.localization.{DisplayFunction, Localizer, NoLocalization}
import utopia.reflection.shape.StackSize
import utopia.reflection.text.Font
import utopia.reflection.text.FontStyle.Plain

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
	
	val yearSelect = new DropDown[Year](16.any x 4.upscaling, "Year", basicFont,
		Color.white, Color.magenta, initialContent = (1999 to 2050).map {Year.of}.toVector)
	val monthSelect = new DropDown[Month](16.any x 4.upscaling, "Month", basicFont,
		Color.white, Color.magenta, initialContent = Month.values().toVector)
	
	val buttonImage = Image.readFrom(Paths.get("test-images/arrow-back-48dp.png")).get
	val backImages = ButtonImageSet.varyingAlpha(buttonImage, 0.66, 1)
	val forwardImages = ButtonImageSet.varyingAlpha(buttonImage.flippedHorizontally, 0.66, 1)
	
	val smallFont = basicFont * 0.75
	def makeWeekDayLabel(day: DayOfWeek) = new ItemLabel[DayOfWeek](day,
		DisplayFunction.noLocalization[DayOfWeek] { _.getDisplayName(TextStyle.SHORT, Locale.getDefault) }, smallFont, StackSize.any)
	
	// TODO: Continue by making number buttons
}
