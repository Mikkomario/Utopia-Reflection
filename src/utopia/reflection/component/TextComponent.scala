package utopia.reflection.component

import utopia.genesis.color.Color
import utopia.reflection.component.stack.Stackable
import utopia.reflection.localization.LocalizedString
import utopia.reflection.shape.{Alignment, StackSize}
import utopia.reflection.text.Font

/**
  * Common trait for components that present text
  * @author Mikko Hilpinen
  * @since 10.12.2019, v1+
  */
trait TextComponent extends Stackable
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
}
