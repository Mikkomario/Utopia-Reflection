package utopia.reflection.component

import javax.swing.{JComponent, JLabel}
import utopia.reflection.localization.{LocalString, LocalizedString, Localizer, TextContext}

object Label
{
	/**
	  * @return A new empty label
	  */
	def apply() = new EmptyLabel()
	
	/**
	  * @param text Localized text to be displayed in this label
	  * @param context The text context
	  * @return A new label that holds text
	  */
	def apply(text: LocalizedString, context: TextContext) = new TextLabel(text, context)
	
	/**
	  * @param text Non-localized text to be localized and then displayed in this label
	  * @param context Text context
	  * @param localizer A localizer that will localize the text (implicit)
	  * @return A new label that holds the localized text
	  */
	def apply(text: LocalString, context: TextContext)(implicit localizer: Localizer[TextContext]) =
		TextLabel(text, context)
}

/**
  * Labels are used as basic UI-elements to display either text or an image. Labels may, of course, also be empty
  * @author Mikko Hilpinen
  * @since 21.4.2019, v1+
  */
abstract class Label extends JWrapper
{
	// ATTRIBUTES	-----------------
	
	protected val label = new JLabel()
	
	
	// INITIAL CODE	-----------------
	
	label.setOpaque(false)
	label.setFocusable(false)
	
	
	// IMPLEMENTED	-----------------
	
	override def component: JComponent = label
}
