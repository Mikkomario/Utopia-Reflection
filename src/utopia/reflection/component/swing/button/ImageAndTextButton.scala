package utopia.reflection.component.swing.button

import utopia.genesis.color.Color
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.label.{ImageLabel, TextLabel}
import utopia.reflection.localization.LocalizedString
import utopia.reflection.shape.{Alignment, Border, StackLength, StackSize}
import utopia.reflection.text.Font
import utopia.reflection.util.ComponentContext

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
	  * @param beforeTextMargin Margin placed between this button's image and text (labels) (default = any, preferring 0)
	  * @param textAlignment Alignment used with this button's text (default = left)
	 *  @param textColor Color used for the text (default = black)
	  * @param action Action that is performed when this button is triggered
	  * @return A new button
	  */
	def apply(images: ButtonImageSet, text: LocalizedString, font: Font, color: Color, margins: StackSize,
			  borderWidth: Double, beforeTextMargin: StackLength = StackLength.any,
			  textAlignment: Alignment = Alignment.Left, textColor: Color = Color.textBlack)(action: () => Unit) =
	{
		val button = new ImageAndTextButton(images, text, font, color, margins, borderWidth, beforeTextMargin,
			textAlignment, textColor)
		button.registerAction(action)
		button
	}
	
	/**
	  * Creates a new button using contextual information
	  * @param images Images used in this button
	  * @param text Text displayed in this button
	  * @param action Action performed when this button is pressed
	  * @param context Component creation context
	  * @return A new button
	  */
	def contextual(images: ButtonImageSet, text: LocalizedString)(action: () => Unit)(implicit context: ComponentContext) =
		apply(images, text, context.font, context.buttonBackground, context.insideMargins, context.borderWidth,
			context.relatedItemsStackMargin, context.textAlignment, context.textColor)(action)
	
	/**
	 * Creates a new button using contextual information. An action for the button needs to be registered separately.
	 * @param images Images used in this button
	 * @param text Text displayed in this button
	 * @param context Component creation context
	 * @return A new button
	 */
	def contextualWithoutAction(images: ButtonImageSet, text: LocalizedString)(implicit context: ComponentContext) =
		new ImageAndTextButton(images, text, context.font, context.buttonBackground, context.insideMargins,
			context.borderWidth, context.relatedItemsStackMargin, context.textAlignment, context.textColor)
}

/**
  * This button implementation displays both an image and some text
  * @author Mikko Hilpinen
  * @since 1.8.2019, v1+
  * @param initialImages Images displayed in this button
  * @param initialText Text displayed in this button
  * @param font Font used in this button's text
  * @param color This button's background color
  * @param margins Margins around this button's contents
  * @param borderWidth Width of border around this button's contents (in pixels)
  * @param beforeTextMargin Margin placed between this button's image and text (labels)
  * @param textAlignment Alignment used with this button's text
 *  @param textColor Color for this button's text (default = black)
  */
class ImageAndTextButton(initialImages: ButtonImageSet, initialText: LocalizedString, font: Font, val color: Color,
						 margins: StackSize, borderWidth: Double, beforeTextMargin: StackLength = StackLength.any,
						 textAlignment: Alignment = Alignment.Left, textColor: Color = Color.textBlack)
	extends StackableAwtComponentWrapperWrapper with ButtonLike
{
	// ATTRIBUTES	------------------------
	
	private var _images = initialImages
	
	private val imageLabel = new ImageLabel(initialImages.defaultImage)
	private val textLabel = new TextLabel(initialText, font, StackSize.any.withLowPriority,
		initialAlignment = textAlignment, initialTextColor = textColor)
	private val content = imageLabel.rowWith(Vector(textLabel), margin = beforeTextMargin).framed(margins)
	
	
	// INITIAL CODE	------------------------
	
	content.background = color
	updateBorder(color)
	setHandCursor()
	content.component.setFocusable(true)
	initializeListeners()
	
	
	// COMPUTED	----------------------------
	
	/**
	 * @return The currently used button image set
	 */
	def images = _images
	def images_=(newImages: ButtonImageSet) =
	{
		_images = newImages
		imageLabel.image = _images(state)
	}
	
	/**
	 * @return The text currently displayed on this button
	 */
	def text = textLabel.text
	def text_=(newText: LocalizedString) = textLabel.text = newText
	
	
	// IMPLEMENTED	------------------------
	
	override protected def wrapped = content
	
	override protected def updateStyleForState(newState: ButtonState) =
	{
		val newColor = newState.modify(color)
		background = newColor
		updateBorder(newColor)
		imageLabel.image = _images(newState)
	}
	
	
	// OTHER	----------------------------
	
	private def updateBorder(baseColor: Color) = content.setBorder(Border.raised(borderWidth, baseColor, 0.5))
}
