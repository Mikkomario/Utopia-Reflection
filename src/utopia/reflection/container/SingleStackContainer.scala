package utopia.reflection.container

import utopia.reflection.component.Stackable

/**
  * This single item container holds an stackable component
  * @author Mikko Hilpinen
  * @since 21.4.2019, v1+
  */
trait SingleStackContainer[C <: Stackable] extends SingleContainer[C] with StackContainer[C]
{
	override def set(content: C) =
	{
		// Removes old component from stack hierarchy first
		components.foreach { StackHierarchyManager.unregister }
		
		super.set(content)
		
		// Adds new connection to stack hierarchy
		StackHierarchyManager.registerConnection(this, content)
	}
}
