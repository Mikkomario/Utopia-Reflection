package utopia.reflection.component.swing.button

import utopia.genesis.color.Color
import utopia.reflection.component.Alignment
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.label.{ImageLabel, TextLabel}
import utopia.reflection.localization.LocalizedString
import utopia.reflection.shape.{Border, StackLength, StackSize}
import utopia.reflection.text.Font

object ImageAndTextButton
{
	/**
	  * Creates a new button
	  * @param images Images displayed in this button
	  * @param text Text displayed in this button
	  * @param font Font used in this button's text
	  * @param color This button's background color
	  * @param margins Margins around this button's contents
	  * @param borderWidth Width of border around this button's contents (in pixels)
	  * @param beforeTextMargin Margin placed between this button's image and text (labels)
	  * @param textAlignment Alignment used with this button's text
	  * @param action Action that is performed when this button is triggered
	  * @return A new button
	  */
	def apply(images: ButtonImageSet, text: LocalizedString, font: Font, color: Color, margins: StackSize,
			  borderWidth: Double, beforeTextMargin: StackLength = StackLength.any,
			  textAlignment: Alignment = Alignment.Left)(action: () => Unit) =
	{
		val button = new ImageAndTextButton(images, text, font, color, margins, borderWidth, beforeTextMargin, textAlignment)
		button.registerAction(action)
		button
	}
}

/**
  * This button implementation displays both an image and some text
  * @author Mikko Hilpinen
  * @since 1.8.2019, v1+
  * @param images Images displayed in this button
  * @param text Text displayed in this button
  * @param font Font used in this button's text
  * @param color This button's background color
  * @param margins Margins around this button's contents
  * @param borderWidth Width of border around this button's contents (in pixels)
  * @param beforeTextMargin Margin placed between this button's image and text (labels)
  * @param textAlignment Alignment used with this button's text
  */
class ImageAndTextButton(val images: ButtonImageSet, text: LocalizedString, font: Font, val color: Color,
						 margins: StackSize, borderWidth: Double, beforeTextMargin: StackLength = StackLength.any,
						 textAlignment: Alignment = Alignment.Left)
	extends StackableAwtComponentWrapperWrapper with ButtonLike
{
	// ATTRIBUTES	------------------------
	
	private val imageLabel = new ImageLabel(images.defaultImage)
	private val content = imageLabel.rowWith(Vector(new TextLabel(text, font, initialAlignment = textAlignment)),
		margin = beforeTextMargin).framed(margins)
	
	
	// INITIAL CODE	------------------------
	
	content.background = color
	updateBorder(color)
	setHandCursor()
	content.component.setFocusable(true)
	initializeListeners()
	
	
	// IMPLEMENTED	------------------------
	
	override protected def wrapped = content
	
	override protected def updateStyleForState(newState: ButtonState) =
	{
		val newColor = newState.modify(color)
		background = newColor
		updateBorder(newColor)
		imageLabel.image = images(newState)
	}
	
	
	// OTHER	----------------------------
	
	private def updateBorder(baseColor: Color) = content.setBorder(Border.raised(borderWidth, baseColor, 0.5))
}
