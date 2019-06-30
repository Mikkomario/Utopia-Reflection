package utopia.reflection.component

import utopia.flow.event.{ChangeListener, Changing}

/**
  * This pool provides access to a changing element
  * @author Mikko Hilpinen
  * @since 29.6.2019, v1+
  */
trait PoolWithPointer[A, +P <: Changing[A]] extends Pool[A]
{
	// ABSTRACT	----------------
	
	/**
	  * @return A pointer into this pool's contents
	  */
	def contentPointer: P
	
	
	// IMPLEMENTED	------------
	
	override def content = contentPointer.value
	
	
	// OTHER	----------------
	
	/**
	  * Adds a new listener to be informed about content changes
	  * @param listener The new listener to be added
	  */
	def addContentListener(listener: ChangeListener[A]) = contentPointer.addListener(listener)
	
	/**
	  * Removes a listener from informed listeners
	  * @param listener A listener
	  */
	def removeContentListener(listener: Any) = contentPointer.removeListener(listener)
}
