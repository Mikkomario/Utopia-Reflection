package utopia.reflection.component.label

import javax.swing.JLabel
import utopia.genesis.color.Color
import utopia.reflection.component.Alignment.Center
import utopia.reflection.component.{Alignable, Alignment, TextComponent}
import utopia.reflection.localization.LocalizedString
import utopia.reflection.shape.StackSize
import utopia.reflection.text.Font

object TextLabel
{
	/**
	  * @param text The localized text displayed in this label
	  * @param font The text font
	  * @param margins The margins around the text in this label
	  * @param hasMinWidth Whether this label always presents the whole text (default = true)
	  * @return A new label with specified text
	  */
	def apply(text: LocalizedString, font: Font, margins: StackSize, hasMinWidth: Boolean = true) =
		new TextLabel(new JLabel(), text, font, margins, hasMinWidth)
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
class TextLabel protected(label: JLabel, initialText: LocalizedString, override val font: Font,
						  override val margins: StackSize, override val hasMinWidth: Boolean = true)
	extends Label(label) with TextComponent with Alignable
{
	// ATTRIBUTES	------------------
	
	private var _text = initialText
	
	
	// INITIAL CODE	------------------
	
	label.setText(initialText.string)
	label.setFont(font.toAwt)
	textColor = Color.textBlack
	
	
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
		label.setHorizontalAlignment(alignment.horizontal.toSwingAlignment)
		label.setVerticalAlignment(alignment.vertical.toSwingAlignment)
		revalidate()
	}
	
	
	// OTHER	----------------------
	
	/**
	  * Clears all text from this label
	  */
	def clear() = text = LocalizedString.empty
}
