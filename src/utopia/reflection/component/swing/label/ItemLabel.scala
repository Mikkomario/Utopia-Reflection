package utopia.reflection.component.swing.label

import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.flow.event.{ChangeEvent, ChangeListener}
import utopia.reflection.component.swing.AwtTextComponentWrapper
import utopia.reflection.component.{Alignable, Alignment, RefreshableWithPointer}
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
	extends Label with AwtTextComponentWrapper with Alignable with RefreshableWithPointer[A]
{
	// ATTRIBUTES	--------------------
	
	private var _text = displayFunction(initialContent)
	
	override val contentPointer = new PointerWithEvents[A](initialContent)
	
	
	// INITIAL CODE	--------------------
	
	label.setFont(font.toAwt)
	label.setText(_text.string)
	
	
	// IMPLEMENTED	--------------------
	
	override def toString = s"Label($text)"
	
	override def text = _text
	
	override def align(alignment: Alignment) =
	{
		label.setHorizontalAlignment(alignment.horizontal.toSwingAlignment)
		label.setVerticalAlignment(alignment.vertical.toSwingAlignment)
		revalidate()
	}
	
	override def updateLayout() = Unit
	
	
	// NESTED CLASSES	----------------
	
	private class ContentUpdateListener extends ChangeListener[A]
	{
		override def onChangeEvent(event: ChangeEvent[A]) =
		{
			_text = displayFunction(event.newValue)
			label.setText(_text.string)
			revalidate()
		}
	}
}
