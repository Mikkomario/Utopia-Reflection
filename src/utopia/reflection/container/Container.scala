package utopia.reflection.container

import utopia.reflection.component.Wrapper

/**
* This trait is extended by classes that may contain one or multiple components
* @author Mikko Hilpinen
* @since 25.3.2019
**/
trait Container[C <: Wrapper] extends Wrapper
{
    // ABSTRACT    ----------------
    
    def component: java.awt.Container
    
    /**
     * The current components in this container
     */
	def components: Vector[C]
	
	/**
	 * Adds a new item to this container
	 */
	protected def add(component: C): Unit
	
	/**
	 * Removes an item from this container
	 */
	protected def remove(component: C): Unit
	
	
	// COMPUTED    ----------------
	
	/**
	 * The number of items in this container
	 */
	def count = components.size
	
	/**
	 * Whether this container is currently empty
	 */
	def isEmpty = components.isEmpty
	
	
	// IMPLEMENTED	----------------
	
	override def toString = s"[${ components.mkString(", ") }]"
}