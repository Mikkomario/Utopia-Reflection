package utopia.reflection.component.swing.label

import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.genesis.color.Color
import utopia.genesis.image.Image
import utopia.genesis.shape.Axis.{X, Y}
import utopia.reflection.component.{RefreshableWithPointer, TextComponent}
import utopia.reflection.component.drawing.immutable.TextDrawContext
import utopia.reflection.component.swing.{StackableAwtComponentWrapperWrapper, SwingComponentRelated}
import utopia.reflection.container.stack.StackLayout
import utopia.reflection.container.stack.StackLayout.{Leading, Trailing}
import utopia.reflection.container.swing.Stack
import utopia.reflection.localization.DisplayFunction
import utopia.reflection.shape.Alignment.{Bottom, Top}
import utopia.reflection.shape.{Alignment, StackInsets, StackLength}
import utopia.reflection.text.Font

/**
  * Used for displaying items with an image + text combination
  * @author Mikko Hilpinen
  * @since 19.3.2020, v1
  * @param contentPointer Pointer used for holding the displayed item
  * @param initialFont Font used when displaying item text
  * @param itemToImageFunction Function used for selecting proper image for each item
  * @param displayFunction Function used for displaying an item as text (default = toString)
  * @param textInsets Insets used in text display (default = any insets)
  * @param imageInsets Insets used in image display (default = any insets)
  * @param betweenItemsMargin Margin between the image and the text (default = any margin)
  * @param alignment Alignment used for placing the items + text alignment (default = left)
  * @param initialTextColor Text color used initially (default = black)
  * @param textHasMinWidth Whether text should always be fully displayed (default = true)
  * @param allowImageUpscaling Whether image should be allowed to scale up (default = false)
  */
// TODO: Replace image with a stack image
class ImageAndTextLabel[A](override val contentPointer: PointerWithEvents[A], initialFont: Font,
						   itemToImageFunction: A => Image, displayFunction: DisplayFunction[A] = DisplayFunction.raw,
						   textInsets: StackInsets = StackInsets.any, imageInsets: StackInsets = StackInsets.any,
						   betweenItemsMargin: StackLength = StackLength.any, alignment: Alignment = Alignment.Left,
						   initialTextColor: Color = Color.textBlack, textHasMinWidth: Boolean = true,
						   allowImageUpscaling: Boolean = false)
	extends StackableAwtComponentWrapperWrapper with RefreshableWithPointer[A] with TextComponent with SwingComponentRelated
{
	// ATTRIBUTES	-------------------------
	
	private val textLabel = new ItemLabel[A](contentPointer, displayFunction, initialFont, initialTextColor,
		textInsets, alignment, textHasMinWidth)
	private val imageLabel = new ImageLabel(itemToImageFunction(contentPointer.value), allowUpscaling = allowImageUpscaling)
	
	private val view =
	{
		val wrappedImageLabel = imageLabel.framed(imageInsets)
		// Determines stack layout based on alignment
		val (direction, items) = alignment.vertical match
		{
			case Top => Y -> Vector(textLabel, wrappedImageLabel)
			case Bottom => Y -> Vector(wrappedImageLabel, textLabel)
			case _ =>
				alignment.horizontal match
				{
					case Alignment.Left => X -> Vector(wrappedImageLabel, textLabel)
					case Alignment.Right => X -> Vector(textLabel, wrappedImageLabel)
					case _ => Y -> Vector(wrappedImageLabel, textLabel)
				}
				
		}
		val layout = alignment.horizontal match
		{
			case Alignment.Left => Leading
			case Alignment.Right => Trailing
			case _ => StackLayout.Center
		}
		Stack.withItems(items, direction, betweenItemsMargin, layout = layout)
	}
	
	
	// INITIAL CODE	-------------------------
	
	// Whenever content updates, image also updates
	addContentListener { e => imageLabel.image = itemToImageFunction(e.newValue) }
	
	
	// IMPLEMENTED	-------------------------
	
	override def component = view.component
	
	def text = textLabel.text
	
	override def drawContext = textLabel.drawContext
	
	override def drawContext_=(newContext: TextDrawContext) = textLabel.drawContext = newContext
	
	override protected def wrapped = view
}
