package utopia.reflection.component.label

import java.awt.Font

import javax.swing.JLabel
import utopia.reflection.component.Alignment.Center
import utopia.reflection.component.{Alignable, Alignment, TextComponent}
import utopia.reflection.localization.{LocalizedString, TextContext}
import utopia.reflection.shape.{StackLength, StackSize}

object TextLabel
{
	/**
	  * @param text The localized text displayed in this label
	  * @param font the font used in the label
	  * @param context The text context
	  * @param margins The margins around the text in this label
	  * @param hasMinWidth Whether this label always presents the whole text (default = true)
	  * @return A new label with specified text
	  */
	def apply(text: LocalizedString, font: Font, context: TextContext, margins: StackSize, hasMinWidth: Boolean = true) =
		new TextLabel(new JLabel(), text, font, context, margins, hasMinWidth)
}

/**
  * This label presents (localized) text
  * @author Mikko Hilpinen
  * @since 23.4.2019, v1+
  * @param initialText The text initially displayed in this label
  * @param context The text context for this label (used in localization)
  */
class TextLabel protected(label: JLabel, initialText: LocalizedString, initialFont: Font, val context: TextContext,
						  override val margins: StackSize, override val hasMinWidth: Boolean = true)
	extends Label(label) with TextComponent with Alignable
{
	// ATTRIBUTES	------------------
	
	private var _text = initialText
	
	
	// INITIAL CODE	------------------
	
	label.setText(initialText.string)
	label.setFont(initialFont)
	
	// TODO: Set the default color to 88% black
	
	
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
	
	override def alignment =
	{
		val vertical = Alignment.forSwingAlignment(label.getVerticalAlignment)
		if (vertical.exists { _ != Center})
			vertical.get
		else
			Alignment.forSwingAlignment(label.getHorizontalAlignment) getOrElse Center
	}
	
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
