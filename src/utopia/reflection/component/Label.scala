package utopia.reflection.component

import javax.swing.{JComponent, JLabel}

// TODO: Later change this to trait and create separate implementations for text and icon + stackable support
// (Maybe use special wrapper classes)

/**
  * Labels are used as UI-elements that present text
  * @author Mikko Hilpinen
  * @since 21.4.2019, v1+
  */
class Label extends JWrapper
{
	// ATTRIBUTES	-----------------
	
	private val label = new JLabel()
	
	
	// COMPUTED	---------------------
	
	// TODO: Add localization support (use implicit translator parameter?)
	// TODO: Also move these sort of things to a separate trait
	def text = label.getText
	def text_=(newText: String) = label.setText(newText)
	
	
	// IMPLEMENTED	-----------------
	
	override def component: JComponent = label
}
