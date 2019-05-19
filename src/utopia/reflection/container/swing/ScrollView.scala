package utopia.reflection.container.swing

import utopia.genesis.handling.mutable.ActorHandler
import utopia.genesis.shape.Axis2D
import utopia.genesis.shape.shape2D.Bounds
import utopia.reflection.component.drawing.CustomDrawableWrapper
import utopia.reflection.component.stack.Stackable
import utopia.reflection.component.swing.{AwtComponentRelated, AwtComponentWrapperWrapper, SwingComponentRelated}
import utopia.reflection.container.stack.{ScrollBarDrawer, ScrollViewLike, StackHierarchyManager}
import utopia.reflection.shape.StackLengthLimit

/**
  * This is a scroll view implemented with swing components
  * @author Mikko Hilpinen
  * @since 30.4.2019, v1+
  */
class ScrollView[C <: Stackable with AwtComponentRelated](override val content: C, override val axis: Axis2D,
														  actorHandler: ActorHandler, scrollPerWheelClick: Double,
														  scrollBarDrawer: ScrollBarDrawer, override val scrollBarWidth: Int,
														  override val scrollBarIsInsideContent: Boolean = false,
														  override val lengthLimit: StackLengthLimit = StackLengthLimit.noLimit,
														  override val limitsToContentSize: Boolean = false)
	extends ScrollViewLike with AwtComponentWrapperWrapper with CustomDrawableWrapper with AwtContainerRelated with SwingComponentRelated
{
	// ATTRIBUTES	----------------------
	
	private val panel = new Panel[C]()
	
	
	// INITIAL CODE	----------------------
	
	panel += content
	addResizeListener(updateLayout())
	addCustomDrawer(scrollBarDrawerToCustomDrawer(scrollBarDrawer))
	setupMouseHandling(actorHandler, scrollPerWheelClick)
	StackHierarchyManager.registerConnection(this, content)
	
	
	// IMPLEMENTED	----------------------
	
	override def repaint(bounds: Bounds) = panel.component.repaint(bounds.toAwt)
	
	override protected def wrapped = panel
	
	override def component = panel.component
	
	override def drawable = panel
	
	override protected def updateVisibility(visible: Boolean) = super[AwtComponentWrapperWrapper].isVisible_=(visible)
}
