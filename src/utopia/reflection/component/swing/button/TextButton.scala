package utopia.reflection.component.swing.button

import utopia.genesis.color.Color
import utopia.genesis.shape.Axis.X
import utopia.reflection.component.SingleLineTextComponent
import utopia.reflection.util.Alignment.Center
import utopia.reflection.component.swing.AwtTextComponentWrapper
import utopia.reflection.component.swing.label.Label
import utopia.reflection.localization.LocalizedString
import utopia.reflection.shape.{Border, StackSize}
import utopia.reflection.text.Font
import utopia.reflection.util.{Alignment, ComponentContext}

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
	
	/**
	  * Creates a new text button using external context
	  * @param text Button text
	  * @param action The action performed when this button is pressed, if any (Default = None)
	  * @param context Button context (implicit)
	  * @return The new button
	  */
	def contextual(text: LocalizedString, action: Option[() => Unit] = None)(implicit context: ComponentContext): TextButton =
	{
		val button = new TextButton(text, context.font, context.buttonBackground, context.insideMargins, context.borderWidth)
		action.foreach(button.registerAction)
		button
	}
	
	/**
	  * Creates a new text button using external context
	  * @param text Button text
	  * @param action Button action
	  * @param context Button context (implicit)
	  * @return The new button
	  */
	def contextual(text: LocalizedString, action: () => Unit)(implicit context: ComponentContext): TextButton =
		contextual(text, Some(action))
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
				 val borderWidth: Double) extends Label with AwtTextComponentWrapper with SingleLineTextComponent with ButtonLike
{
	// INITIAL CODE	------------------
	
	label.setFont(font.toAwt)
	label.setFocusable(true)
	setHandCursor()
	label.setText(text.string)
	background = color
	Alignment.Center.swingComponents.get(X).foreach(label.setHorizontalAlignment)
	
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
