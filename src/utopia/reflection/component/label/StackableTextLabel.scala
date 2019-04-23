package utopia.reflection.component.label

import java.awt.Font

import javax.swing.JLabel
import utopia.reflection.component.Alignment.Center
import utopia.reflection.component.{Alignment, JStackable}
import utopia.reflection.localization.{LocalizedString, TextContext}
import utopia.reflection.shape.{StackLength, StackSize}

object StackableTextLabel
{
	/**
	  * Creates a new stackable text label
	  * @param text Text in this label
	  * @param font The font used by this label
	  * @param context The text context
	  * @param hMargin Horizontal margin used around the text
	  * @param vMargin Vertical margin used around the text
	  * @param hasMinWidth Whether minimum width is set in a way that will always show the text
	  * @return a new stackable text label
	  */
	def apply(text: LocalizedString, font: Font, context: TextContext, hMargin: StackLength, vMargin: StackLength,
			  hasMinWidth: Boolean) = new StackableTextLabel(new JLabel(), text, font, context, hMargin, vMargin, hasMinWidth)
}

/**
  * This is a stackable version of text label
  * @author Mikko Hilpinen
  * @since 23.4.2019, v1+
  */
class StackableTextLabel protected(label: JLabel, initialText: LocalizedString, initialFont: Font, context: TextContext,
						 val hMargin: StackLength, val vMargin: StackLength, val hasMinWidth: Boolean)
	extends TextLabel(label, initialText, initialFont, context) with JStackable
{
	// IMPLEMENTED	-----------------------
	
	// Revalidates this object when text, font or alignment changes
	override def text_=(newText: LocalizedString) =
	{
		super.text_=(newText)
		revalidate()
	}
	override def font_=(newFont: Font) =
	{
		super.font_=(newFont)
		revalidate()
	}
	override def align(alignment: Alignment) =
	{
		super.align(alignment)
		revalidate()
	}
	
	override def updateLayout() = Unit // Doesn't need to update layout
	
	override protected def calculatedStackSize =
	{
		// Adds margins to base text size. Alignment matters on this one.
		val textW = textWidth.getOrElse(128)
		val totalHMargin = if (horizontalAlignment == Center) hMargin * 2 else hMargin
		val totalVMargin = if (verticalAlignment == Center) vMargin * 2 else vMargin
		val w = (if (hasMinWidth) StackLength.fixed(textW) else StackLength.downscaling(textW)) + totalHMargin
		val h = StackLength.fixed(textHeight.getOrElse(32)) + totalVMargin
		
		StackSize(w, h)
	}
}