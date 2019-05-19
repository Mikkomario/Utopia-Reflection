package utopia.reflection.component.stack

import utopia.genesis.shape.Axis2D
import utopia.reflection.component.ComponentLike
import utopia.reflection.component.swing.AwtComponentRelated
import utopia.reflection.container.stack.StackLayout.Fit
import utopia.reflection.container.stack.{StackHierarchyManager, StackLayout}
import utopia.reflection.container.swing.{Framing, Stack}
import utopia.reflection.shape.{StackLength, StackSize}
import utopia.genesis.shape.Axis._

object Stackable
{
	// AwtComponent stackables can be stacked & framed easily
	implicit class AwtStackable[S <: Stackable with AwtComponentRelated](val s: S) extends AnyVal
	{
		/**
		  * Creates a stack with this item along with some others
		  * @param elements Other elements
		  * @param axis Stack axis
		  * @param margin The margin between items
		  * @param cap The cap at each end of stack (default = no cap = fixed to 0)
		  * @param layout The stack layout (default = Fit)
		  * @tparam S2 Stack element type
		  * @return A new stack
		  */
		def stackWith[S2 >: S <: Stackable with AwtComponentRelated](elements: Seq[S2], axis: Axis2D, margin: StackLength,
																	 cap: StackLength = StackLength.fixed(0),
																	 layout: StackLayout = Fit) =
			Stack.withItems(s +: elements, axis, margin, cap, layout)
		
		/**
		  * Creates a horizontal stack with this item along with some others
		  * @param elements Other elements
		  * @param margin Margin between elements
		  * @param cap Cap at each end of the stack (default = fixed to 0)
		  * @param layout Stack layout (default = Fit)
		  * @tparam S2 Stack element type
		  * @return A new stack with these items
		  */
		def rowWith[S2 >: S <: Stackable with AwtComponentRelated](elements: Seq[S2], margin: StackLength,
																   cap: StackLength = StackLength.fixed(0),
																   layout: StackLayout = Fit) =
			s.stackWith(elements, X, margin, cap, layout)
		
		/**
		  * Creates a vertical stack with this item along with some others
		  * @param elements Other elements
		  * @param margin margin between elements
		  * @param cap Cap at each end of the stack (default = fixed to 0)
		  * @param layout Stack layout (default = Fit)
		  * @tparam S2 Stack element type
		  * @return A new stack with these items
		  */
		def columnWith[S2 >: S <: Stackable with AwtComponentRelated](elements: Seq[S2], margin: StackLength,
																   cap: StackLength = StackLength.fixed(0),
																   layout: StackLayout = Fit) =
			s.stackWith(elements, Y, margin, cap, layout)
		
		/**
		  * Frames this item
		  * @param margins The margins placed around this item
		  * @return A framing with this item inside it
		  */
		def framed(margins: StackSize) = new Framing(s, margins)
	}
}

/**
* This trait is inherited by component classes that can be placed in stacks
* @author Mikko Hilpinen
* @since 25.2.2019
**/
trait Stackable extends ComponentLike
{
	// ABSTRACT	---------------------
	
	/**
	  * Updates the layout (and other contents) of this stackable instance. This method will be called if the component,
	  * or its child is revalidated. The stack sizes of this component, as well as those of revalidating children
	  * should be reset at this point.
	  */
	def updateLayout(): Unit
	
	/**
	  * The current sizes of this wrapper. Invisible wrappers should always have a stack size of zero.
	  */
	def stackSize: StackSize
	
	/**
	  * Resets cached stackSize, if there is one, so that it will be recalculated when requested next time
	  */
	def resetCachedSize(): Unit
	
	/**
	  * @return A unique identifier for this stackable instance. These id's are used in stack hierarchy to
	  *         distinquish between items. If this stackable simply wraps another item, it should use the same id,
	  *         otherwise the id should be unique (usually it is enough to return hashCode).
	  */
	def stackId: Int
	
	
	// COMPUTED	---------------------
	
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
