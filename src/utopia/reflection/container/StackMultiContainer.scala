package utopia.reflection.container

import utopia.reflection.component.Stackable

/**
  * Stack containers hold stackable items, which means that they might update their content when content changes
  * @tparam C The type of content inside this container
  * @author Mikko Hilpinen
  * @since 15.4.2019, v0.1+
  */
trait StackMultiContainer[C <: Stackable] extends MultiContainer[C] with StackContainer[C]
{
	// IMPLEMENTED	---------------------
	
	override def +=(component: C) =
	{
		// Adds the component, but also registers it to stack hierarchy manager
		super.+=(component)
		StackHierarchyManager.registerConnection(this, component)
	}
	
	override def -=(component: C) =
	{
		// Removes the component, but also unregisters it from stack hierarchy manager
		super.-=(component)
		StackHierarchyManager.unregister(component)
		
		// If this container was left empty, unregisters it as well
		if (isEmpty)
			StackHierarchyManager.unregister(this)
	}
}
