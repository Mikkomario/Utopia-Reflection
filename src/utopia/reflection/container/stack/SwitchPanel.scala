package utopia.reflection.container.stack

import utopia.genesis.color.Color
import utopia.reflection.component.{AwtComponentRelated, AwtComponentWrapperWrapper, CachingStackable, JWrapper, Stackable, StackableAwtComponentWrapper, SwingComponentRelated}
import utopia.reflection.container.{AwtContainerRelated, Panel}
import utopia.reflection.shape.StackSize

/**
  * Switch panels may switch the component they contain
  * @author Mikko Hilpinen
  * @since 27.4.2019, v1+
  */
class SwitchPanel[C <: Stackable with AwtComponentRelated](initialContent: C) extends SingleStackContainer[C]
	with AwtComponentWrapperWrapper with SwingComponentRelated with AwtContainerRelated with CachingStackable
{
	// ATTRIBUTES	-------------------
	
	private val panel = new Panel[C]()
	
	
	// INITIAL CODE	-------------------
	
	set(initialContent)
	addResizeListener(updateLayout())
	
	
	// IMPLEMENTED	-------------------
	
	override protected def wrapped = panel
	
	override protected def updateVisibility(visible: Boolean) = super[AwtComponentWrapperWrapper].isVisible_=(visible)
	
	override def component = panel.component
	
	// Content size matches that of this panel
	override def updateLayout() = content.foreach { _.size = this.size }
	
	override protected def calculatedStackSize = content.map { _.stackSize } getOrElse StackSize.any
	
	override def components = panel.components
	
	override protected def add(component: C) = panel += component
	
	override protected def remove(component: C) = panel -= component
	
	override def background_=(color: Color) = super[AwtComponentWrapperWrapper].background_=(color)
}
