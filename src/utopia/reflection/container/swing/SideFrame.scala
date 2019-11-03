package utopia.reflection.container.swing

import utopia.genesis.shape.shape2D.Direction2D
import utopia.reflection.component.drawing.CustomDrawableWrapper
import utopia.reflection.component.stack.Stackable
import utopia.reflection.component.swing.{AwtComponentRelated, SwingComponentRelated}
import utopia.reflection.container.stack.SideFrameLike

/**
 * A swing component that places the underlying component to a single side
 * @author Mikko Hilpinen
 * @since 3.11.2019, v1+
 */
class SideFrame[C <: Stackable with AwtComponentRelated](initialComponent: C, override val contentSide: Direction2D)
	extends SideFrameLike[C] with SwingComponentRelated with AwtContainerRelated with CustomDrawableWrapper
{
	// ATTRIBUTES	---------------------
	
	private val panel = new Panel[C]
	
	
	// INITIAL CODE	--------------------
	
	set(initialComponent)
	// Updates content layout each time size changes
	addResizeListener(updateLayout())
	
	
	// IMPLEMENTED	---------------------
	
	override protected def container = panel
	
	override def component = panel.component
	
	override def drawable = panel
	
	override protected def add(component: C) = panel += component
	
	override protected def remove(component: C) = panel -= component
}
