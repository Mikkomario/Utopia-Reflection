package utopia.reflection.container.stack

import utopia.reflection.component.{JWrapper, Stackable}
import utopia.reflection.container.Panel
import utopia.reflection.shape.StackSize

/**
  * Switch panels may switch the component they contain
  * @author Mikko Hilpinen
  * @since 27.4.2019, v1+
  */
class SwitchPanel(initialContent: Stackable) extends SingleStackContainer[Stackable] with JWrapper
{
	// ATTRIBUTES	-------------------
	
	private val panel = new Panel[Stackable]()
	private var content: Option[Stackable] = Some(initialContent)
	
	
	// INITIAL CODE	-------------------
	
	set(initialContent)
	addResizeListener(updateLayout())
	
	
	// IMPLEMENTED	-------------------
	
	override def component = panel.component
	
	// Content size matches that of this panel
	override def updateLayout() = content.foreach { _.size = this.size }
	
	override protected def calculatedStackSize = content.map { _.stackSize } getOrElse StackSize.any
	
	override def components = panel.components
	
	override protected def add(component: Stackable) =
	{
		panel += component
		content = Some(component)
	}
	
	override protected def remove(component: Stackable) =
	{
		panel -= component
		content = None
	}
}
