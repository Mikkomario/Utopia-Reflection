package utopia.reflection.container.stack

import utopia.reflection.component.stack.Stackable
import utopia.reflection.container.SingleContainer

/**
  * This single item container holds a stackable component
  * @author Mikko Hilpinen
  * @since 21.4.2019, v1+
  */
trait SingleStackContainer[C <: Stackable] extends SingleContainer[C] with StackContainer[C]
{
	override def set(content: C) =
	{
		// Removes old component from stack hierarchy first
		components.foreach(StackHierarchyManager.unregister)
		
		super.set(content)
		
		// Adds new connection to stack hierarchy
		StackHierarchyManager.registerConnection(this, content)
		
		// Revalidates the hierarchy
		revalidate()
	}
}
