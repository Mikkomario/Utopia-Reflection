package utopia.reflection.component

import java.awt.Color

import utopia.reflection.localization.{LocalString, LocalizedString, Localizer, TextContext}

object TextLabel
{
	/**
	  * Creates a new label with auto-localized text
	  * @param text The non-localized text in this label
	  * @param context The context of this label
	  * @param localizer A localizer used for localizing the text (implicit)
	  * @return A new label with localized text
	  */
	def apply(text: LocalString, context: TextContext)(implicit localizer: Localizer[TextContext]) =
		new TextLabel(localizer.localize(text, context), context)
	
	/**
	  * Creates a new label with no text
	  * @param context Text context
	  * @return A label with no text
	  */
	def empty(context: TextContext) = new TextLabel(LocalizedString.empty, context)
}

/**
  * This label presents (localized) text
  * @author Mikko Hilpinen
  * @since 23.4.2019, v1+
  * @param initialText The text initially displayed in this label
  * @param context The text context for this label (used in localization)
  */
class TextLabel(initialText: LocalizedString, val context: TextContext) extends Label with Alignable
{
	// ATTRIBUTES	------------------
	
	private var _text = initialText
	
	
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
	
	
	// INITIAL CODE	------------------
	
	label.setText(initialText.string)
	
	
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
}
