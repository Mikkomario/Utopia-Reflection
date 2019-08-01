package utopia.reflection.component.swing.label

import utopia.genesis.color.Color
import utopia.reflection.component.Alignment
import utopia.reflection.component.Alignment.Center
import utopia.reflection.component.swing.{AwtTextComponentWrapper, ButtonLike, ButtonState}
import utopia.reflection.localization.LocalizedString
import utopia.reflection.shape.{Border, StackSize}
import utopia.reflection.text.Font

/**
  * Buttons are used for user interaction
  * @author Mikko Hilpinen
  * @since 25.4.2019, v1+
  */
class TextButton(override val text: LocalizedString, override val font: Font, val color: Color, override val margins: StackSize,
				 val borderWidth: Double, val action: () => Unit) extends Label with AwtTextComponentWrapper with ButtonLike
{
	// INITIAL CODE	------------------
	
	label.setFont(font.toAwt)
	label.setFocusable(true)
	setHandCursor()
	label.setText(text.string)
	background = color
	label.setHorizontalAlignment(Alignment.Center.toSwingAlignment)
	
	
	// IMPLEMENTED	------------------
	
	override protected def performAction() = action()
	
	override protected def updateStyleForState(newState: ButtonState) =
	{
		val newColor = newState.modify(color)
		background = newColor
		updateBorder(newColor)
	}
	
	override def alignment = Center
	
	override def hasMinWidth = true
	
	override def updateLayout() = Unit
	
	override def toString = s"Button($text)"
	
	
	// OTHER	----------------------
	
	private def updateBorder(baseColor: Color) = setBorder(Border.raised(borderWidth, baseColor, 0.5))
}
