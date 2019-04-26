package utopia.reflection.component

import utopia.reflection.shape.StackSize
import java.awt.Component

import utopia.flow.datastructure.mutable.Lazy
import utopia.reflection.container.{Framing, StackHierarchyManager}

object Stackable
{
    /**
     * Wraps a component as stackable
     * @param component wrapped component
     * @param getSize a function for retrieving component size
     */
    def apply(component: Component, getSize: () => StackSize, update: () => Unit = () => Unit): Stackable =
		new StackWrapper(component, getSize, update)
    
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
	// ATTRIBUTES	-----------------
	
	private val cachedStackSize = new Lazy[StackSize](() => calculatedStackSize)
	
	
	// ABSTRACT	---------------------
	
	/**
	  * Updates the layout (and other contents) of this stackable instance. This method will be called if the component,
	  * or its child is revalidated. The stack sizes of this component, as well as those of revalidating children
	  * should be reset at this point.
	  */
	def updateLayout(): Unit
	
	/**
	  * Calculates an up-to-date stack size for this component
	  * @return An up-to-date stack size for this component
	  */
	protected def calculatedStackSize: StackSize
	
	
	// COMPUTED	---------------------
	
    /**
     * The current sizes of this wrapper
     */
	def stackSize = cachedStackSize.get
	
	/**
	  * @return Whether this component is now larger than its maximum size
	  */
	def isOverSized = stackSize.maxWidth.exists { _ < width } || stackSize.maxHeight.exists { _ < height }
	
	/**
	  * @return Whether this component is now smaller than its minimum size
	  */
	def isUnderSized = width < stackSize.minWidth || height < stackSize.minHeight
	
	
	// OTHER	---------------------
	
	/**
	  * @param margins The margins placed around this instance
	  * @return A framing that contains this stackable item
	  */
	def framed(margins: StackSize) = new Framing[Stackable](this, margins)
	
	/**
	  * Resets cached stackSize, if there is one, so that it will be recalculated when requested next time
	  */
	def resetCachedSize() = cachedStackSize.reset()
	
	/**
	  * Requests a revalidation for this item
	  */
	def revalidate() = StackHierarchyManager.requestValidationFor(this)
	
	/**
	 * Sets the size of this component to optimal (by stack size)
	 */
	def setToOptimalSize() = size = stackSize.optimal
	
	/**
	 * Sets the size of this component to minimum (by stack size)
	 */
	def setToMinSize() = size = stackSize.min
}

private class StackWrapper(val component: Component, val getSize: () => StackSize, val update: () => Unit) extends Stackable
{
	override def updateLayout() = update()
	
	override protected def calculatedStackSize = getSize()
}