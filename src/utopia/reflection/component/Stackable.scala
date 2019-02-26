package utopia.reflection.component

import utopia.reflection.shape.StackSize
import java.awt.Component

object Stackable
{
    /**
     * Wraps a component as stackable
     * @param component wrapped component
     * @param getSize a function for retrieving component size
     */
    def apply(component: Component, getSize: () => StackSize): Stackable = new StackWrapper(component, getSize)    
    
    /**
     * Wraps a component as stackable
     * @param component wrapped component
     * @param size fixed component sizes
     */
    def apply(component: Component, size: StackSize): Stackable = apply(component, () => size)
}

/**
* This trait is inherited by component classes that can be placed in stacks
* @author Mikko Hilpinen
* @since 25.2.2019
**/
trait Stackable extends Wrapper
{
    /**
     * The current sizes of this wrapper
     */
	def stackSize: StackSize
	
	/**
	 * Sets the size of this component to optimal (by stack size)
	 */
	def setToOptimalSize() = size = stackSize.optimal
	
	/**
	 * Sets the size of this component to minimum (by stack size)
	 */
	def setToMinSize() = size = stackSize.min
}

private class StackWrapper(val component: Component, val getSize: () => StackSize) extends Stackable
{
    def stackSize = getSize()
}