package utopia.reflection.component.swing

import utopia.reflection.component.swing.label.ImageLabel

/**
  * This button only displays an image
  * @author Mikko Hilpinen
  * @since 1.8.2019, v1+
  * @param images Images used in this button
  * @param action Action that is triggered when this button is pressed
  */
class ImageButton(val images: ButtonImageSet, val action: () => Unit) extends StackableAwtComponentWrapperWrapper with ButtonLike
{
	// ATTRIBUTES	--------------------
	
	private val label = new ImageLabel(images.defaultImage)
	
	
	// INITIAL CODE	--------------------
	
	// Uses hand cursor on buttons by default
	label.setHandCursor()
	
	
	// IMPLEMENTED	--------------------
	
	override protected def wrapped = label
	
	override protected def performAction() = action()
	
	override protected def updateStyleForState(newState: ButtonState) = label.image = images(newState)
}
