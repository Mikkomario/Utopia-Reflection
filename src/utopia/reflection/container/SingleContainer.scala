package utopia.reflection.container

import utopia.reflection.component.Wrapper

/**
  * This container contains only a single component at a time
  * @author Mikko Hilpinen
  * @since 21.4.2019, v1+
  */
trait SingleContainer[C <: Wrapper] extends Container[C]
{
	/**
	  * Changes the component inside this container
	  * @param content The new content for this container
	  */
	def set(content: C) =
	{
		// Removes any previous content first
		components.foreach(remove)
		// Then adds the new content
		add(content)
	}
}
