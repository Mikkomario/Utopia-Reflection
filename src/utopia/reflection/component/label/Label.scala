package utopia.reflection.component.label

import java.awt.Font

import javax.swing.{JComponent, JLabel}
import utopia.reflection.component.JWrapper
import utopia.reflection.localization.{LocalizedString, TextContext}
import utopia.reflection.shape.StackSize

object Label
{
	/**
	  * @return A new empty label
	  */
	def apply() = new EmptyLabel()
	
	/**
	  * @param text Localized text to be displayed in this label
	  * @param font the font used in the label
	  * @param context The text context
	  * @return A new label that holds text
	  */
	def apply(text: LocalizedString, font: Font, context: TextContext, margins: StackSize) = TextLabel(text, font, context, margins)
}

/**
  * Labels are used as basic UI-elements to display either text or an image. Labels may, of course, also be empty
  * @author Mikko Hilpinen
  * @since 21.4.2019, v1+
  */
class Label protected(protected val label: JLabel) extends JWrapper
{
	// INITIAL CODE	-----------------
	
	label.setOpaque(false)
	label.setFocusable(false)
	
	
	// IMPLEMENTED	-----------------
	
	override def component: JComponent = label
}
