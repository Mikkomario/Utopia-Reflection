package utopia.reflection.component.label

import java.awt.{Color, Font}

import javax.swing.JLabel
import utopia.reflection.component.Alignment.Center
import utopia.reflection.component.{Alignable, Alignment}
import utopia.reflection.localization.{LocalString, LocalizedString, Localizer, TextContext}
import utopia.reflection.shape.StackLength

object TextLabel
{
	/**
	  * @param text The localized text displayed in this label
	  * @param font the font used in the label
	  * @param context The text context
	  * @return A new label with specified text
	  */
	def apply(text: LocalizedString, font: Font, context: TextContext) = new TextLabel(new JLabel(), text, font, context)
	
	/**
	  * Creates a new label with auto-localized text
	  * @param text The non-localized text in this label
	  * @param font the font used in the label
	  * @param context The context of this label
	  * @param localizer A localizer used for localizing the text (implicit)
	  * @return A new label with localized text
	  */
	def apply(text: LocalString, font: Font, context: TextContext)(implicit localizer: Localizer[TextContext]) =
		new TextLabel(new JLabel(), localizer.localize(text, context), font, context)
	
	/**
	  * Creates a new label with no text
	  * @param font the font used in the label
	  * @param context Text context
	  * @return A label with no text
	  */
	def empty(font: Font, context: TextContext) = new TextLabel(new JLabel(), LocalizedString.empty, font, context)
}

/**
  * This label presents (localized) text
  * @author Mikko Hilpinen
  * @since 23.4.2019, v1+
  * @param initialText The text initially displayed in this label
  * @param context The text context for this label (used in localization)
  */
class TextLabel protected(label: JLabel, initialText: LocalizedString, initialFont: Font, val context: TextContext) extends Label(label)
	with Alignable
{
	// ATTRIBUTES	------------------
	
	private var _text = initialText
	
	
	// INITIAL CODE	------------------
	
	label.setText(initialText.string)
	font = initialFont
	
	
	// COMPUTED	----------------------
	
	/**
	  * @return The text currently displayed in this label
	  */
	def text = _text
	/**
	  * @param newText The new text to be displayed on this label
	  */
	def text_=(newText: LocalizedString) =
	{
		_text = newText
		label.setText(newText.string)
	}
	/**
	  * @param newText The new text to be displayed on this label (non-localized)
	  * @param localizer A localizer that localizes the text (implicit)
	  */
	def text_=(newText: LocalString)(implicit localizer: Localizer[TextContext]): Unit = text = localizer.localize(newText, context)
	
	// TODO: Set the default color to 88% black
	/**
	  * @return The current text color for this label
	  */
	def textColor = label.getForeground
	/**
	  * @param newColor The new text color for this label
	  */
	def textColor_=(newColor: Color) = label.setForeground(newColor)
	
	/**
	  * @return The current text width of this component
	  */
	def textWidth: Option[Int] = textWidth(text.string)
	
	/**
	  * @return The current horizontal alignment of this label
	  */
	def horizontalAlignment = Alignment.forSwingAlignment(label.getHorizontalAlignment) getOrElse Center
	/**
	  * @return The current vertical alignment of this label
	  */
	def verticalAlignment = Alignment.forSwingAlignment(label.getVerticalAlignment) getOrElse Center
	
	
	// IMPLEMENTED	------------------
	
	override def toString = s"Label($text)"
	
	override def align(alignment: Alignment) =
	{
		label.setHorizontalAlignment(alignment.horizontal.toSwingAlignment)
		label.setVerticalAlignment(alignment.vertical.toSwingAlignment)
	}
	
	
	// OTHER	----------------------
	
	/**
	  * Clears all text from this label
	  */
	def clear() = text = LocalizedString.empty
	
	/**
	  * Creates a stackable copy of this label
	  * @param hMargin The horizontal margin beside text
	  * @param vMargin The vertical margin beside text
	  * @param hasMinWidth Whether the label's minimum width will at least contain the whole text for the label.
	  *                    If false, the text may not show completely. Default = true
	  * @return A new stackable version of this text label
	  */
	def stackableCopy(hMargin: StackLength, vMargin: StackLength, hasMinWidth: Boolean = true) =
		StackableTextLabel(text, font.get, context, hMargin, vMargin, hasMinWidth)
}
