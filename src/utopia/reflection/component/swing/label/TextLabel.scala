package utopia.reflection.component.swing.label

import utopia.genesis.color.Color
import utopia.genesis.shape.Axis.{X, Y}
import utopia.reflection.component.swing.AwtTextComponentWrapper
import utopia.reflection.component.Alignable
import utopia.reflection.localization.LocalizedString
import utopia.reflection.shape.StackSize
import utopia.reflection.text.Font
import utopia.reflection.util.{Alignment, ComponentContext}

object TextLabel
{
	/**
	  * @param text The localized text displayed in this label
	  * @param font The text font
	  * @param margins The margins around the text in this label
	  * @param hasMinWidth Whether this label always presents the whole text (default = true)
	  * @return A new label with specified text
	  */
	def apply(text: LocalizedString, font: Font, margins: StackSize = StackSize.any, hasMinWidth: Boolean = true,
			  alignment: Alignment = Alignment.Left) = new TextLabel(text, font, margins, hasMinWidth, alignment)
	
	/**
	  * Creates a new label using contextual information
	  * @param text Label text
	  * @param context Component creation context
	  * @return A new label
	  */
	def contextual(text: LocalizedString, isHint: Boolean = false)(implicit context: ComponentContext) =
	{
		val label = new TextLabel(text, context.font, context.insideMargins, context.textHasMinWidth,
			context.textAlignment)
		context.setBorderAndBackground(label)
		label.textColor = if (isHint) context.hintTextColor else context.textColor
		label
	}
}

/**
  * This label presents (localized) text
  * @author Mikko Hilpinen
  * @since 23.4.2019, v1+
  * @param initialText The text initially displayed in this label
  * @param font The font used in this label
  * @param margins The margins placed around the text
  * @param hasMinWidth Whether this text label always presents the whole text (default = true)
  */
class TextLabel(initialText: LocalizedString, override val font: Font, override val margins: StackSize = StackSize.any,
					 override val hasMinWidth: Boolean = true, initialAlignment: Alignment = Alignment.Left)
	extends Label with AwtTextComponentWrapper with Alignable
{
	// ATTRIBUTES	------------------
	
	private var _text = initialText
	
	
	// INITIAL CODE	------------------
	
	label.setText(initialText.string)
	label.setFont(font.toAwt)
	textColor = Color.textBlack
	align(initialAlignment)
	
	
	// IMPLEMENTED	------------------
	
	/**
	  * @return The text currently displayed in this label
	  */
	override def text = _text
	/**
	  * @param newText The new text to be displayed on this label
	  */
	def text_=(newText: LocalizedString) =
	{
		_text = newText
		label.setText(newText.string)
		revalidate()
	}
	
	override def toString = s"Label($text)"
	
	override def updateLayout() = Unit
	
	override def align(alignment: Alignment) =
	{
		val comps = alignment.swingComponents
		comps.get(X).foreach(label.setHorizontalAlignment)
		comps.get(Y).foreach(label.setVerticalAlignment)
		revalidate()
	}
	
	
	// OTHER	----------------------
	
	/**
	  * Clears all text from this label
	  */
	def clear() = text = LocalizedString.empty
}
