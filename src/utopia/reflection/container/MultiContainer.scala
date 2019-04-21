package utopia.reflection.container

import utopia.reflection.component.Wrapper

/**
* This trait is extended by classes that may contain one or multiple components
* @author Mikko Hilpinen
* @since 25.3.2019
**/
trait MultiContainer[C <: Wrapper] extends Container[C] with Wrapper
{
	// OPERATORS    ---------------
	
	/**
	  * Adds a new item to this container
	  */
	def +=(component: C) = add(component)
	
	/**
	  * Removes an item from this container
	  */
	def -=(component: C) = remove(component)
	
	/**
	 * Adds multiple items to this container
	 */
	def ++=(components: TraversableOnce[C]) = components.foreach(+=)
	
	/**
	 * Adds multiple items to this container
	 */
	def ++=(first: C, second: C, more: C*): Unit = ++=(Vector(first, second) ++ more)
	
	/**
	 * Removes multiple items from this container
	 */
	def --=(components: TraversableOnce[C]) = components.foreach(-=)
	
	/**
	 * Removes multiple items from this container
	 */
	def --=(first: C, second: C, more: C*): Unit = --=(Vector(first, second) ++ more)
	
	
	// OTHER    -------------------
	
	/**
	 * Removes all items from this container
	 */
	def clear() = components.foreach(-=)
}