package utopia.reflection.component.swing.button

import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.reflection.component.drawing.CustomDrawableWrapper
import utopia.reflection.component.input.InteractionWithPointer
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.label.ImageLabel

/**
  * Used for switching a value on or off
  * @author Mikko Hilpinen
  * @since 1.8.2019, v1+
  */
class ImageCheckBox(offImages: ButtonImageSet, onImages: ButtonImageSet, initialState: Boolean = false)
	extends StackableAwtComponentWrapperWrapper with CustomDrawableWrapper with ButtonLike
		with InteractionWithPointer[Boolean]
{
	// ATTRIBUTES	---------------------
	
	private val label = new ImageLabel(if (initialState) onImages.defaultImage else offImages.defaultImage)
	override val valuePointer = new PointerWithEvents(initialState)
	
	
	// INITIAL CODE	---------------------
	
	initializeListeners()
	valuePointer.addListener { _ => updateStyleForState(state) }
	registerAction { () => value = !value }
	
	
	// COMPUTED	-------------------------
	
	def isOn = value
	def isOff = !isOn
	
	
	// IMPLEMENTED	---------------------
	
	override protected def wrapped = label
	
	override def drawable = label
	
	override protected def updateStyleForState(newState: ButtonState) =
	{
		if (isOn)
			label.image = onImages(newState)
		else
			label.image = offImages(newState)
	}
}
