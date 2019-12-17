package utopia.reflection.component.swing.button

import utopia.genesis.color.Color
import utopia.reflection.localization.LocalizedString
import utopia.reflection.shape.{Alignment, StackLength, StackSize}
import utopia.reflection.text.Font

/**
  * Used for creating buttons
  * @author Mikko Hilpinen
  * @since 1.8.2019, v1+
  */
object Button
{
	/**
	  * Creates a new button with only an image
	  * @param images Images used in the button
	  * @param action Action performed when button is pressed
	  * @return A new button
	  */
	def withImage(images: ButtonImageSet)(action: () => Unit) = ImageButton(images)(action)
	
	/**
	  * Creates a new button with only text
	  * @param text Text displayed in button
	  * @param font Font used in text
	  * @param color Button background color
	  * @param margins Margins around text
	  * @param borderWidth Width of border inside button
	  * @param action Action performed when button is pressed
	  * @return A new button
	  */
	def withText(text: LocalizedString, font: Font, color: Color, margins: StackSize, borderWidth: Double)
				(action: () => Unit) = TextButton(text, font, color, margins, borderWidth)(action)
	
	/**
	  * Creates a new button with both image and text
	  * @param images Images used in button
	  * @param text Text displayed in button
	  * @param font Font used in text
	  * @param color Button background color
	  * @param margins Margins around button content
	  * @param borderWidth Width of border inside button
	  * @param beforeTextMargin Margin before text portion (default = any)
	  * @param textAlignment Alignment used in text (default = left)
	  * @param action Action performed when button is pressed
	  * @return A new button
	  */
	def withImageAndText(images: ButtonImageSet, text: LocalizedString, font: Font, color: Color, margins: StackSize,
						 borderWidth: Double, beforeTextMargin: StackLength = StackLength.any,
						 textAlignment: Alignment = Alignment.Left)(action: () => Unit) =
		ImageAndTextButton(images, text, font, color, margins, borderWidth, beforeTextMargin, textAlignment)(action)
}
