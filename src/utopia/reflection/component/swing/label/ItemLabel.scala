package utopia.reflection.component.swing.label

import utopia.reflection.component.swing.AwtTextComponentWrapper
import utopia.reflection.component.{Alignable, Alignment, Refreshable}
import utopia.reflection.localization.DisplayFunction
import utopia.reflection.shape.StackSize
import utopia.reflection.text.Font

/**
  * These labels display an item of a specific type, transforming it into text format
  * @author Mikko Hilpinen
  * @since 24.4.2019, v1+
  * @tparam A The type of item displayed in this label
  * @param initialContent The item first displayed in this label
  * @param displayFunction A function that transforms the item to displayable text
  * @param font The font used in this label
  * @param margins The margins (horizontal and vertical) around the text in this label
  * @param hasMinWidth Whether this label should have minimum width (always show all content text) (default = true)
  */
class ItemLabel[A](initialContent: A, val displayFunction: DisplayFunction[A], override val font: Font,
				   override val margins: StackSize, override val hasMinWidth: Boolean = true)
	extends Label with AwtTextComponentWrapper with Alignable with Refreshable[A]
{
	// ATTRIBUTES	--------------------
	
	private var _content = initialContent
	private var _text = displayFunction(initialContent)
	
	
	// INITIAL CODE	--------------------
	
	label.setFont(font.toAwt)
	label.setText(_text.string)
	
	
	// IMPLEMENTED	--------------------
	
	override def content = _content
	override def content_=(newContent: A) =
	{
		_content = newContent
		_text = displayFunction(newContent)
		label.setText(_text.string)
		revalidate()
	}
	
	override def text = _text
	
	override def align(alignment: Alignment) =
	{
		label.setHorizontalAlignment(alignment.horizontal.toSwingAlignment)
		label.setVerticalAlignment(alignment.vertical.toSwingAlignment)
		revalidate()
	}
	
	override def updateLayout() = Unit
}
