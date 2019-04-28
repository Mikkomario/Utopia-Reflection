package utopia.reflection.component

import utopia.genesis.color.Color

/**
  * This is a commom trait for components that present text
  * @author Mikko Hilpinen
  * @since 24.4.2019, v1+
  */
trait AwtTextComponentWrapper extends StackableAwtComponentWrapper with TextComponent
{
	// IMPLEMENTED	--------------------------
	
	/**
	  * @return The color of the text in this component
	  */
	def textColor: Color = component.getForeground
	def textColor_=(newColor: Color) = component.setForeground(newColor.toAwt)
}
