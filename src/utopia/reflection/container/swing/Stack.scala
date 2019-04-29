package utopia.reflection.container.swing

import utopia.genesis.color.Color
import utopia.genesis.shape.Axis2D
import utopia.reflection.component.swing.{AwtComponentRelated, AwtComponentWrapperWrapper, SwingComponentRelated}
import utopia.reflection.component.{CachingStackable, Stackable}
import utopia.reflection.container.stack.{StackLayout, StackLike}
import utopia.reflection.shape.StackLength

object Stack
{
    type AwtStackable = Stackable with AwtComponentRelated
    
    /**
      * Creates a new stack with a set of items to begin with
      * @param direction Direction of the stack
      * @param layout Stack layout
      * @param margin Margin between items
      * @param cap Cap at each end of the stack
      * @param items Items to be added to stack
      * @tparam C The type of items in the stack
      * @return A new stack
      */
    def withItems[C <: AwtStackable](direction: Axis2D, layout: StackLayout, margin: StackLength, cap: StackLength,
                                     items: TraversableOnce[C]) =
    {
        val stack = new Stack[C](direction, layout, margin, cap)
        stack ++= items
        stack
    }
}

/**
* A stack holds multiple stackable components in a stack-like manner either horizontally or vertically
* @author Mikko Hilpinen
* @since 25.2.2019
**/
class Stack[C <: Stack.AwtStackable](override val direction: Axis2D, override val layout: StackLayout,
                                                     override val margin: StackLength, override val cap: StackLength)
    extends StackLike[C] with AwtComponentWrapperWrapper with CachingStackable with SwingComponentRelated with AwtContainerRelated
{
	// ATTRIBUTES    --------------------
    
    private val panel = new Panel[C]()
    
    
    // INITIAL CODE    ------------------
    
    // Each time size changes, also updates content (doesn't reset stack sizes at this time)
    addResizeListener(updateLayout())
    
    
    // IMPLEMENTED    -------------------
    
    override def background_=(color: Color) = super[SwingComponentRelated].background_=(color)
    
    override def component = panel.component
    
    override protected def wrapped = panel
    
    override protected def updateVisibility(visible: Boolean) = panel.isVisible = visible
    
    override def isVisible_=(isVisible: Boolean) = super[CachingStackable].isVisible_=(isVisible)
    
    protected def add(component: C) = panel += component
    
    protected def remove(component: C) = panel -= component
}