package utopia.reflection.test

import java.awt.Color

import utopia.genesis.shape.Y
import utopia.reflection.component.label.TextLabel
import utopia.reflection.container.{Frame, Stack}
import utopia.reflection.container.StackLayout.Leading
import utopia.reflection.container.WindowResizePolicy.User
import utopia.reflection.localization.{Localizer, NoLocalization}
import utopia.reflection.shape.{StackLength, StackSize}
import utopia.reflection.text.Font
import utopia.reflection.text.FontStyle.Plain

/**
  * This is a simple test implementation of text labels in a stack
  * @author Mikko Hilpinen
  * @since 24.4.2019, v1+
  */
object TextLabelStackTest extends App
{
	// Sets up localization context
	implicit val defaultLanguageCode: String = "EN"
	implicit val localizer: Localizer = NoLocalization
	
	// Creates the labels
	val basicFont = Font("Arial", 12, Plain, 2)
	val labels = Vector("Here are some labels", "just", "for you").map { s => TextLabel(s, basicFont,
		StackSize(StackLength.any(16), StackLength.fixed(0))) }
	labels.foreach { _.background = Color.YELLOW }
	
	println(labels.map { _.alignment }.mkString(", "))
	
	// Creates the stack
	val stack = new Stack(Y, Leading, StackLength.any(8), StackLength.any(16))
	stack ++= labels
	stack.background = Color.BLACK
	
	// Creates the frame and displays it
	val frame = Frame.windowed(stack, "TextLabel Stack Test", User)
	frame.setToExitOnClose()
	frame.visible = true
}
