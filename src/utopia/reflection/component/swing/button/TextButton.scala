package utopia.reflection.component.swing.button

import utopia.genesis.color.Color
import utopia.reflection.component.Alignment
import utopia.reflection.component.Alignment.Center
import utopia.reflection.component.swing.AwtTextComponentWrapper
import utopia.reflection.component.swing.label.Label
import utopia.reflection.localization.LocalizedString
import utopia.reflection.shape.{Border, StackSize}
import utopia.reflection.text.Font

object TextButton
{
	/**
	  * Creates a new button
	  * @param text Text displayed in this button
	  * @param font Font used when displaying text
	  * @param color This button's background color
	  * @param margins Margins placed around this button's text
	  * @param borderWidth Width of the border inside this button (in pixels)
	  * @param action Action performed when this button is pressed
	  * @return A new button
	  */
	def apply(text: LocalizedString, font: Font, color: Color, margins: StackSize, borderWidth: Double)
			 (action: () => Unit) =
	{
		val button = new TextButton(text, font, color, margins, borderWidth)
		button.registerAction(action)
		button
	}
}

/**
  * Buttons are used for user interaction
  * @author Mikko Hilpinen
  * @since 25.4.2019, v1+
  * @param text Text displayed in this button
  * @param font Font used when displaying text
  * @param color This button's background color
  * @param margins Margins placed around this button's text
  * @param borderWidth Width of the border inside this button (in pixels)
  */
class TextButton(override val text: LocalizedString, override val font: Font, val color: Color, override val margins: StackSize,
				 val borderWidth: Double) extends Label with AwtTextComponentWrapper with ButtonLike
{
	// INITIAL CODE	------------------
	
	label.setFont(font.toAwt)
	label.setFocusable(true)
	setHandCursor()
	label.setText(text.string)
	background = color
	label.setHorizontalAlignment(Alignment.Center.toSwingAlignment)
	
	initializeListeners()
	
	
	// IMPLEMENTED	------------------
	
	override protected def updateStyleForState(newState: ButtonState) =
	{
		val newColor = newState.modify(color)
		background = newColor
		updateBorder(newColor)
	}
	
	override def alignment = Center
	
	override def hasMinWidth = true
	
	override def updateLayout() = Unit
	
	override def toString = s"Button($text)"
	
	
	// OTHER	----------------------
	
	private def updateBorder(baseColor: Color) = setBorder(Border.raised(borderWidth, baseColor, 0.5))
}
