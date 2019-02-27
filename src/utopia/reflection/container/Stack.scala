package utopia.reflection.container

import utopia.reflection.component.JWrapper
import utopia.reflection.component.Stackable
import utopia.genesis.shape.Axis2D
import utopia.reflection.shape.StackLength
import utopia.genesis.shape.shape2D.Point
import utopia.genesis.shape.shape2D.Size
import utopia.flow.datastructure.mutable.Lazy
import utopia.reflection.component.Area
import utopia.reflection.shape.StackSize
import utopia.reflection.container.StackLayout.Fit
import utopia.reflection.component.Wrapper

/**
* A stack holds multiple stackable components in a stack-like manner either horizontally or vertically
* @author Mikko Hilpinen
* @since 25.2.2019
**/
class Stack(val direction: Axis2D, val layout: StackLayout, val margin: StackLength, 
        val cap: StackLength) extends Stackable with JWrapper
{
	// ATTRIBUTES    --------------------
    
    private val panel = new Panel()
    private var _components = Vector[CacheStackable]()
    
    def components = _components map { _.source }
    
    
    // COMPUTED    ----------------------
    
    private def numberOfMargins = _components.size - 1
    private def totalMarginsLength = margin * numberOfMargins
    
    def count = _components.size
    
    
    // IMPLEMENTED    -------------------
    
    def component = panel.component
    
    def stackSize = 
    {
        if (_components.isEmpty)
            StackSize.any.withLowPriorityFor(direction)
        else
        {
            // Checks component sizes
            val sizes = _components map { _.stackSize }
            val lengths = sizes map { _ along direction }
            val breadths = sizes map { _ perpendicularTo direction }
            
            // Determines total length & breadth
            val componentsLength = lengths reduce { _ + _ }
            // Length has low priority if any of the items has one
            val length = componentsLength + totalMarginsLength + (cap * 2)
            
            // Non-fit stacks don't have max breadth while fit versions do
            // Breadth is considered low priority only if all items are low priority
            val breadth = 
            {
                if (layout == Fit)
                {
                    val min = breadths.map { _.min }.max
                    val optimal = breadths.map { _.optimal }.max
                    val max = breadths.flatMap { _.max }.reduceOption { _ min _ }
                    
                    StackLength(min, optimal, max)
                }
                else
                    breadths.reduce { StackLength.max }.withMax(None)
            
            }.withPriority(breadths forall { _.lowPriority })
            
            // Returns the final size
            StackSize(length, breadth, direction)
        }
    }
    
    
    // OPERATORS    ---------------------
    
    def +=(component: Stackable) = 
    {
        _components :+= new CacheStackable(component)
        panel += component
    }
    
    def ++=(components: TraversableOnce[Stackable]) = components foreach { += }
    
    def -=(component: Wrapper) = 
    {
        _components = _components filterNot { component.equals }
        panel -= component
    }
    
    def --=(components: TraversableOnce[Wrapper]) = components foreach { -= }
    
    
    // OTHERS    -----------------------
    
    def clear() = 
    {
        _components = Vector()
        panel.clear()
    }
    
    def dropLast(amount: Int) = _components dropRight(amount) map { _.source } foreach { -= }
    
    def resetCachedSize() = _components foreach { _.resetStackSize() }
    
    def refreshContent() = ???
}

private class CacheStackable(val source: Stackable) extends Area
{
    // ATTRIBUTES    -------------------
    
    private var nextPosition: Option[Point] = None
    private var nextSize: Option[Size] = None
    
    private val sizes = new Lazy(() => source.stackSize)
    
    
    // IMPLEMENTED    -----------------
    
    def position = nextPosition getOrElse source.position
    def position_=(p: Point) = nextPosition = Some(p)
    
    def size = nextSize getOrElse source.size
    def size_=(s: Size) = nextSize = Some(s)
    
    
    // COMPUTED    --------------------
    
    def component = source.component
    
    def stackSize = sizes.get
    
    
    // OTHER    -----------------------
    
    def updateBounds() = 
    {
        nextPosition filterNot { _ ~== source.position } foreach { source.position = _ }
        nextPosition = None
        
        nextSize filterNot { _ ~== source.size } foreach { source.size = _ }
        nextSize = None
    }
    
    def resetStackSize() = sizes.reset()
}