package utopia.reflection.component.swing.label

import java.awt.Graphics

import javax.swing.{JComponent, JLabel}
import utopia.genesis.color.Color
import utopia.genesis.shape.shape2D.{Bounds, Point, Size}
import utopia.reflection.component.drawing.{CustomDrawable, CustomDrawableWrapper}
import utopia.reflection.component.swing.{CustomDrawComponent, JWrapper}
import utopia.reflection.localization.LocalizedString
import utopia.reflection.shape.StackSize
import utopia.reflection.text.Font
import utopia.reflection.util.Alignment

object Label
{
	/**
	  * @return A new empty label
	  */
	def apply() = new EmptyLabel()
	
	/**
	  * @param text Localized text to be displayed in this label
	  * @param font the font used for displaying the text
	  * @return A new label that holds text
	  */
	def apply(text: LocalizedString, font: Font, margins: StackSize) = TextLabel(text, font, margins)
}

/**
  * Labels are used as basic UI-elements to display either text or an image. Labels may, of course, also be empty
  * @author Mikko Hilpinen
  * @since 21.4.2019, v1+
  */
class Label extends JWrapper with CustomDrawableWrapper
{
	// ATTRIBUTES	-----------------
	
	private val _label = new CustomJLabel()
	
	
	// COMPUTED	---------------------
	
	/**
	  * @return The label this label wraps
	  */
	protected def label: JLabel = _label
	
	
	// COMPUTED	---------------------
	
	/**
	  * @return The alignment for this label's contents
	  */
	def alignment = Alignment.forSwingAlignments(_label.getHorizontalAlignment, _label.getVerticalAlignment)
	
	
	// IMPLEMENTED	-----------------
	
	override def drawable: CustomDrawable = _label
	
	override def component: JComponent = label
	
	override def children = Vector()
}

private class CustomJLabel extends JLabel with CustomDrawComponent
{
	// INITIAL CODE	-----------------
	
	setOpaque(false)
	setFocusable(false)
	setForeground(Color.textBlack.toAwt)
	
	
	// IMPLEMENTED	-----------------
	
	override def drawBounds = Bounds(Point.origin, Size.of(getSize()) - (1, 1))
	
	override def paintComponent(g: Graphics) = customPaintComponent(g, super.paintComponent)
	
	override def paintChildren(g: Graphics) = customPaintChildren(g, super.paintChildren)
	
	override def isPaintingOrigin = shouldPaintOrigin()
}
