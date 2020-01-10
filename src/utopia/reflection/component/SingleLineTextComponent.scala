package utopia.reflection.component

import utopia.reflection.component.stack.StackSizeCalculating
import utopia.reflection.shape.Alignment.Center
import utopia.reflection.shape.{StackLength, StackSize}

/**
  * This is a commom trait for components that present text on a single line
  * @author Mikko Hilpinen
  * @since 24.4.2019, v1+
  */
trait SingleLineTextComponent extends TextComponent with StackSizeCalculating
{
	// ABSTRACT	--------------------------
	
	/**
	  * @return Whether this component has a minimum width based on text size. If false, text may not always show.
	  */
	def hasMinWidth: Boolean
	
	
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
		val textH = textHeight.getOrElse(32)
		val h = StackLength.fixed(textH) + totalVMargin
		
		StackSize(w, h)
	}
}
