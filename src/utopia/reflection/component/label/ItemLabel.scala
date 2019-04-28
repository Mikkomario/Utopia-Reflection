package utopia.reflection.component.label

import javax.swing.JLabel
import utopia.reflection.component.{Alignable, Alignment, AwtTextComponentWrapper}
import utopia.reflection.localization.DisplayFunction
import utopia.reflection.shape.StackSize
import utopia.reflection.text.Font

/**
  * These labels display an item of a specific type, transforming it into text format
  * @author Mikko Hilpinen
  * @since 24.4.2019, v1+
  * @tparam A The type of item displayed in this label
  */
class ItemLabel[A](initialItem: A, val displayFunction: DisplayFunction[A], override val font: Font,
				   override val margins: StackSize, override val hasMinWidth: Boolean = true)
	extends Label(new JLabel()) with AwtTextComponentWrapper with Alignable
{
	// ATTRIBUTES	--------------------
	
	private var _item = initialItem
	private var _text = displayFunction(initialItem)
	
	
	// INITIAL CODE	--------------------
	
	label.setFont(font.toAwt)
	label.setText(_text.string)
	
	
	// COMPUTED	------------------------
	
	def item = _item
	def item_=(newItem: A) =
	{
		_item = newItem
		_text = displayFunction(newItem)
		label.setText(_text.string)
		revalidate()
	}
	
	
	// IMPLEMENTED	--------------------
	
	override def text = _text
	
	override def align(alignment: Alignment) =
	{
		label.setHorizontalAlignment(alignment.horizontal.toSwingAlignment)
		label.setVerticalAlignment(alignment.vertical.toSwingAlignment)
		revalidate()
	}
	
	override def updateLayout() = Unit
}
