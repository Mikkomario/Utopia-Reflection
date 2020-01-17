package utopia.reflection.container.swing

import utopia.reflection.component.Alignable
import utopia.reflection.component.drawing.CustomDrawableWrapper
import utopia.reflection.component.stack.CachingStackable
import utopia.reflection.component.swing.{AwtComponentWrapperWrapper, SwingComponentRelated}
import utopia.reflection.container.stack.StackHierarchyManager
import utopia.reflection.container.swing.Stack.AwtStackable
import utopia.reflection.shape.Alignment

/**
 * This container consists of two views, one in the background and another in the foreground
 * @author Mikko Hilpinen
 * @since 18.1.2020, v1
 */
class LayeredView[Background <: AwtStackable, Foreground <: AwtStackable]
(background: Background, foreground: Foreground, initialAlignment: Alignment)
	extends AwtComponentWrapperWrapper with CachingStackable with SwingComponentRelated with AwtContainerRelated
		with CustomDrawableWrapper with Alignable
{
	// ATTRIBUTES	---------------------
	
	private val foregroundContainer = new AlignFrame(foreground, initialAlignment)
	private val panel = new Panel[AwtStackable]()
	
	
	// INITIAL CODE	---------------------
	
	panel += foregroundContainer
	panel += background
	
	// Updates content layout each time this component is resized
	addResizeListener(updateLayout())
	
	// Registers connection to stack hierarchy manager as well
	StackHierarchyManager.registerConnection(this, background)
	StackHierarchyManager.registerConnection(this, foregroundContainer)
	
	
	// IMPLEMENTED	---------------------
	
	override def component = panel.component
	
	override protected def wrapped = panel
	
	override protected def updateVisibility(visible: Boolean) = super.isVisible_=(visible)
	
	override def drawable = panel
	
	override def align(alignment: Alignment) = foregroundContainer.align(alignment)
	
	// The stack size is defined by both the background and foreground
	override protected def calculatedStackSize = background.stackSize max foregroundContainer.stackSize
	
	override def updateLayout() =
	{
		// Both background and foreground are set to span this component's area
		background.size = size
		foregroundContainer.size = size
	}
}
