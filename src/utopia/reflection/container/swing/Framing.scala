package utopia.reflection.container.swing

import utopia.genesis.color.Color
import utopia.reflection.component.CustomDrawableWrapper
import utopia.reflection.component.stack.Stackable
import utopia.reflection.component.swing.{AwtComponentRelated, SwingComponentRelated}
import utopia.reflection.container.stack.FramingLike
import utopia.reflection.shape.StackSize

/**
  * Framings are containers that present a component with scaling 'frames', like a painting
  * @author Mikko Hilpinen
  * @since 26.4.2019, v1+
  */
class Framing[C <: Stackable with AwtComponentRelated](initialComponent: C, val margins: StackSize) extends
	FramingLike[C] with SwingComponentRelated with AwtContainerRelated with CustomDrawableWrapper
{
	// ATTRIBUTES	--------------------
	
	private val panel = new Panel[C]()
	
	
	// INITIAL CODE	--------------------
	
	set(initialComponent)
	// Each time Framing size changes, changes content size too
	addResizeListener(updateLayout())
	
	
	// IMPLEMENTED	--------------------
	
	override def drawable = panel
	
	override def background_=(color: Color) = super[SwingComponentRelated].background_=(color)
	
	override protected def container = panel
	
	override def component = panel.component
	
	override protected def add(component: C) = panel += component
	
	override protected def remove(component: C) = panel -= component
}
