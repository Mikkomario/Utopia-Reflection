package utopia.reflection.component

import utopia.genesis.color.Color
import utopia.reflection.util.Alignment.Center
import utopia.reflection.component.stack.{StackSizeCalculating, Stackable}
import utopia.reflection.localization.LocalizedString
import utopia.reflection.shape.{StackLength, StackSize}
import utopia.reflection.text.Font
import utopia.reflection.util.Alignment

/**
  * This is a commom trait for components that present text
  * @author Mikko Hilpinen
  * @since 24.4.2019, v1+
  */
trait TextComponent extends Stackable with StackSizeCalculating
{
	// ABSTRACT	--------------------------
	
	/**
	  * @return The margins around the text in this component
	  */
	def margins: StackSize
	
	/**
	  * @return This component's text alignment
	  */
	def alignment: Alignment
	/**
	  * @return The text currently presented in this component
	  */
	def text: LocalizedString
	/**
	  * @return The font used in this component
	  */
	def font: Font
	/**
	  * @return Whether this component has a minimum width based on text size. If false, text may not always show.
	  */
	def hasMinWidth: Boolean
	
	/**
	  * @return The color of the text in this component
	  */
	def textColor: Color
	def textColor_=(newColor: Color): Unit
	
	
	// COMPUTED	--------------------------
	
	/**
	  * @return The length of a horizontal margin around this component
	  */
	def hMargin = margins.width
	/**
	  * @return The length of a vertical margin around this component
	  */
	def vMargin = margins.height
	
	/**
	  * @return The width of the current text in this component. None if width couldn't be calculated.
	  */
	def textWidth: Option[Int] = textWidth(text.string)
	
	
	// IMPLEMENTED	----------------------
	
	/**
	  * @return The calculated stack size of this component
	  */
	protected def calculatedStackSize =
	{
		// Adds margins to base text size. Alignment also matters.
		val textW = textWidth.getOrElse(128)
		val totalHMargin = if (alignment.horizontal == Center) hMargin * 2 else hMargin
		val totalVMargin = if (alignment.vertical == Center) vMargin * 2 else vMargin
		
		val w = (if (hasMinWidth) StackLength.fixed(textW) else StackLength.downscaling(textW)) + totalHMargin
		val h = StackLength.fixed(textHeight.getOrElse(32)) + totalVMargin
		
		StackSize(w, h)
	}
}
