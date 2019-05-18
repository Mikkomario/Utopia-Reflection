package utopia.reflection.container.swing

import utopia.genesis.shape.Axis._
import utopia.genesis.shape.Axis2D
import utopia.reflection.component.drawing.CustomDrawableWrapper
import utopia.reflection.component.stack.{CachingStackable, Stackable}
import utopia.reflection.component.swing.{AwtComponentRelated, AwtComponentWrapperWrapper, SwingComponentRelated}
import utopia.reflection.container.stack.StackLayout.Fit
import utopia.reflection.container.stack.{StackLayout, StackLike}
import utopia.reflection.shape.StackLength

object Stack
{
    type AwtStackable = Stackable with AwtComponentRelated
    
    /**
      * Creates a new stack with a set of items to begin with
      * @param items Items to be added to stack
      * @param direction Direction of the stack
      * @param margin Margin between items
      * @param cap Cap at each end of the stack (default = no cap)
      * @param layout Stack layout (default = Fit)
      * @tparam C The type of items in the stack
      * @return A new stack
      */
    def withItems[C <: AwtStackable](items: TraversableOnce[C], direction: Axis2D, margin: StackLength,
                                     cap: StackLength = StackLength.fixed(0), layout: StackLayout = Fit) =
    {
        val stack = new Stack[C](direction, margin, cap, layout)
        stack ++= items
        stack
    }
    
    /**
      * Creates a new horizontal stack
      * @param margin The margin between items
      * @param cap The cap at each end of this stack (default = no cap)
      * @param layout The layout used (default = Fit)
      * @tparam C The type of items in this stack
      * @return A new stack
      */
    def row[C <: AwtStackable](margin: StackLength, cap: StackLength = StackLength.fixed(0), layout: StackLayout = Fit) =
        new Stack[C](X, margin, cap, layout)
    
    /**
      * Creates a new vertical stack
      * @param margin The margin between items
      * @param cap The cap at each end of this stack (default = no cap)
      * @param layout The layout used (default = Fit)
      * @tparam C The type of items in this stack
      * @return A new stack
      */
    def column[C <: AwtStackable](margin: StackLength, cap: StackLength = StackLength.fixed(0), layout: StackLayout = Fit) =
        new Stack[C](Y, margin, cap, layout)
    
    /**
      * Creates a new horizontal stack
      * @param items The items placed in this stack
      * @param margin The margin between items
      * @param cap The cap at each end of this stack (default = no cap)
      * @param layout The layout used (default = Fit)
      * @tparam C The type of items in this stack
      * @return A new stack
      */
    def rowWithItems[C <: AwtStackable](items: TraversableOnce[C], margin: StackLength, cap: StackLength = StackLength.fixed(0),
                                        layout: StackLayout = Fit) = withItems(items, X, margin, cap, layout)
    
    /**
      * Creates a new vertical stack
      * @param items The items placed in this stack
      * @param margin The margin between items
      * @param cap The cap at each end of this stack (default = no cap)
      * @param layout The layout used (default = Fit)
      * @tparam C The type of items in this stack
      * @return A new stack
      */
    def columnWithItems[C <: AwtStackable](items: TraversableOnce[C], margin: StackLength, cap: StackLength = StackLength.fixed(0),
                                           layout: StackLayout = Fit) = withItems(items, Y, margin, cap, layout)
}

/**
* A stack holds multiple stackable components in a stack-like manner either horizontally or vertically
* @author Mikko Hilpinen
* @since 25.2.2019
  * @param direction The direction of this stack (X = row, Y = column)
  * @param margin The margin placed between items
  * @param cap The cap at each end of this stack (default = no cap)
  * @param layout The layout of this stack's components perpendicular to the 'direction' (default = Fit)
**/
class Stack[C <: Stack.AwtStackable](override val direction: Axis2D, override val margin: StackLength,
                                     override val cap: StackLength = StackLength.fixed(0), override val layout: StackLayout = Fit)
    extends StackLike[C] with AwtComponentWrapperWrapper with CachingStackable with SwingComponentRelated
        with AwtContainerRelated with CustomDrawableWrapper
{
	// ATTRIBUTES    --------------------
    
    private val panel = new Panel[C]()
    
    
    // INITIAL CODE    ------------------
    
    // Each time size changes, also updates content (doesn't reset stack sizes at this time)
    addResizeListener(updateLayout())
    
    
    // IMPLEMENTED    -------------------
    
    override def drawable = panel
    
    override def component = panel.component
    
    override protected def wrapped = panel
    
    override protected def updateVisibility(visible: Boolean) = panel.isVisible = visible
    
    override def isVisible_=(isVisible: Boolean) = super[CachingStackable].isVisible_=(isVisible)
    
    protected def add(component: C) = panel += component
    
    protected def remove(component: C) = panel -= component
}