package utopia.reflection.container.swing

import utopia.reflection.component.drawing.CustomDrawableWrapper
import utopia.reflection.component.stack.Stackable
import utopia.reflection.component.swing.{AwtComponentRelated, SwingComponentRelated}
import utopia.reflection.container.stack.CenterFrameLike

/**
 * A swing component that places the underlying component at the center, without upscaling it
 * @author Mikko Hilpinen
 * @since 3.11.2019, v1+
 */
// TODO: Contains a lot of copy-pasted code between this, SideFrame and Framing. Consider adding a common parent
//  trait and/or class
class CenterFrame[C <: Stackable with AwtComponentRelated](initialComponent: C)
	extends CenterFrameLike[C] with SwingComponentRelated with AwtContainerRelated with CustomDrawableWrapper
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
