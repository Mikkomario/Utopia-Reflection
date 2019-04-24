package utopia.reflection.component.label

import javax.swing.{JComponent, JLabel}
import utopia.reflection.component.Alignment.Center
import utopia.reflection.component.{Alignment, JWrapper}
import utopia.reflection.localization.LocalizedString
import utopia.reflection.shape.StackSize
import utopia.reflection.text.Font

object Label
{
	/**
	  * @return A new empty label
	  */
	def apply() = new EmptyLabel()
	
	/**
	  * @param text Localized text to be displayed in this label
	  * @param font the font used for displaying the text
	  * @return A new label that holds text
	  */
	def apply(text: LocalizedString, font: Font, margins: StackSize) = TextLabel(text, font, margins)
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
	// TODO: Set the default color to 88% black
	
	
	// COMPUTED	---------------------
	
	/**
	  * @return The alignment for this label's contents
	  */
	def alignment =
	{
		println(label.getVerticalAlignment)
		
		val vertical = Alignment.forSwingAlignment(label.getVerticalAlignment)
		
		if (vertical.exists { _ != Center})
			vertical.get
		else
		{
			Alignment.forSwingAlignment(label.getHorizontalAlignment) getOrElse Center
		}
	}
	
	
	// IMPLEMENTED	-----------------
	
	override def component: JComponent = label
}
