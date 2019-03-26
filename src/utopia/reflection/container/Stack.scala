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
import utopia.flow.datastructure.mutable.Pointer
import scala.collection.immutable.VectorBuilder
import utopia.reflection.container.StackLayout.Leading
import utopia.reflection.container.StackLayout.Trailing
import utopia.reflection.event.ResizeListener

/**
* A stack holds multiple stackable components in a stack-like manner either horizontally or vertically
* @author Mikko Hilpinen
* @since 25.2.2019
**/
class Stack(val direction: Axis2D, val layout: StackLayout, val margin: StackLength, 
        val cap: StackLength) extends Container[Stackable] with Stackable with JWrapper
{
	// ATTRIBUTES    --------------------
    
    private val panel = new Panel()
    private var _components = Vector[CacheStackable]()
    
    
    // INITIAL CODE    ------------------
    
    // TODO: Remove this feature if another style of resize is implemented
    // Each time size changes, also updates content (doesn't reset stack sizes at this time)
    resizeListeners :+= ResizeListener(e => refreshContent())
    
    
    // COMPUTED    ----------------------
    
    private def numberOfMargins = _components.size - 1
    private def totalMarginsLength = margin * numberOfMargins
    
    /**
     * The length (min, optimal, max) of this stack
     */
    def stackLength = stackSize.along(direction)
    
    /**
     * The breadth (min, optimal, max) of this stack
     */
    def stackBreadth = stackSize.perpendicularTo(direction)
    
    
    // IMPLEMENTED    -------------------
    
    def component = panel.component
    
    def components = _components map { _.source }
    
    def +=(component: Stackable) = 
    {
        _components :+= new CacheStackable(component)
        panel += component
    }
    
    def -=(component: Stackable) = 
    {
        _components = _components filterNot { component.equals }
        panel -= component
    }
    
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
    
    
    // OTHERS    -----------------------
    
    def dropLast(amount: Int) = _components dropRight(amount) map { _.source } foreach { -= }
    
    def resetCachedSize() = _components foreach { _.resetStackSize() }
    
    def refreshContent() = 
    {
        if (!_components.isEmpty)
        {
            // Calculates the necessary length adjustment
            val stackSize = this.stackSize
            val lengthAdjustment = lengthAlong(direction) - stackSize.along(direction).optimal
            
            // Arranges the mutable items in a vector first. Treats margins and caps as separate items
            val caps = Vector.fill(2)(Pointer(0.0))
            val margins = Vector.fill(count - 1)(Pointer(0.0))
            val targets = 
            {
                val builder = new VectorBuilder[LengthAdjust]()
                
                // Starts with a cap
                builder += new GapLengthAdjust(caps.head, cap)
                
                // Next adds items with margins
                _components.zip(margins).foreach
                {
                    case (component, margin) =>
                        builder += new StackableLengthAdjust(component, direction)
                        builder += new GapLengthAdjust(margin, this.margin)
                }
                
                // Adds final component and final cap
                builder += new StackableLengthAdjust(_components.last, direction)
                builder += new GapLengthAdjust(caps.last, cap)
                
                builder.result()
            }
            
            // First adjusts the length of low priority items, then of all remaining items (if necessary)
            val groupedTargets = targets.groupBy { _.isLowPriority }
            val lowPrioTargets = groupedTargets.getOrElse(true, Vector())
            
            val remainingAdjustment = 
            {
                if (lowPrioTargets.isEmpty)
                    lengthAdjustment
                else
                    adjustLength(lowPrioTargets, lengthAdjustment)
            }
            
            if (remainingAdjustment != 0.0)
                groupedTargets.get(false).foreach { adjustLength(_, remainingAdjustment) }
            
            // Applies the length adjustments
            targets.foreach { _() }
            
            // Positions the components length-wise (first components with margin and then the final component)
            var cursor = caps.head.get
            _components.zip(margins).foreach 
            {
                case (component, margin) =>  
                    component.setCoordinate(cursor, direction)
                    cursor += component.lengthAlong(direction) + margin.get
            }
            _components.last.setCoordinate(cursor, direction)
            
            // Handles the breadth of the components too, as well as their perpendicular positioning
            val breadthAxis = direction.perpendicular
            val newBreadth = lengthAlong(breadthAxis)
            _components.foreach
            {
                component =>
                    val breadth = component.stackSize.along(breadthAxis)
                    
                    // Component breadth may be affected by minimum and maximum
                    val newComponentBreadth = 
                    {
                        if (breadth.min > newBreadth)
                            breadth.min
                        else if (breadth.max.exists { newBreadth < _ })
                            breadth.max.get
                        else
                        {
                            // In fit-style stacks, stack breadth is used over component optimal 
                            // whereas in other styles optimal is prioritized
                            if (layout == Fit)
                                newBreadth
                            else
                                newBreadth min breadth.optimal
                        }
                    }
                    
                    component.setLength(newComponentBreadth, breadthAxis)
                    
                    // Component positioning depends on the layout
                    val newComponentPosition = 
                    {
                        if (layout == Leading)
                            0
                        else if (layout == Trailing)
                            newBreadth - newComponentBreadth
                        else
                            (newBreadth - newComponentBreadth) / 2
                    }
                    
                    component.setCoordinate(newComponentPosition, breadthAxis)
            }
        }
    }
    
    private def adjustLength(targets: Traversable[LengthAdjust], adjustment: Double): Double = 
    {
        // Finds out how much each item should be adjusted
        val adjustmentPerComponent = adjustment / targets.size
        
        // Adjusts the items (some may be maxed) and caches results
        val results = targets map { target => target -> (target += adjustment) }
        
        // Finds out the remaining adjustment and available targets
        val remainingAdjustment = results.foldLeft(0.0) { case (total, next) => total + next._2 }
        val availableTargets = results.filter { _._2 != 0.0 }.map { _._1 }
        
        // If necessary and possible, goes for the second round. Returns remaining adjustment
        if (availableTargets.isEmpty)
            remainingAdjustment
        else if (remainingAdjustment == 0.0)
            0.0
        else
            adjustLength(availableTargets, remainingAdjustment)
    }
    
    /*
    private def adjustLength(targets: Traversable[CacheStackable], adjustment: Double): Unit = 
    {
        // First finds the items that can be adjusted (and how much)
        val adjustable = targets.map { c => c -> c.maxAdjustment(direction, adjustment) }.filterNot 
        { _._2.exists { _.abs < 1 } };
        
        // Prefers items with low priority
        val topPriority = adjustable.filter { _._1.stackSize.isLowPriorityFor(direction) }
        val finalTargets = if (topPriority.isEmpty) adjustable else topPriority
        
        // Tries to adjust each target the same amount (limited by max adjust)
        val adjustmentPerTarget = adjustment / finalTargets.size
        
        // Will not care for < 1 pixel changes (the system is not accurate enough)
        if (adjustmentPerTarget.abs >= 1)
        {
            // Finds out which targets will be maxed out and which won't
            val groupedTargets = finalTargets groupBy { 
                    case (c, max) => max.exists { adjustmentPerTarget.abs < _.abs } }
            val maxed = groupedTargets.getOrElse(true, Vector()).map { case (c, max) => c -> max.get }
            val nonMaxed = groupedTargets.getOrElse(false, Vector()).map { _._1 }
            
            // Performs the actual length adjustment
            maxed foreach { case (c, max) => c.adjustLength(max, direction) }
            nonMaxed foreach { _.adjustLength(adjustmentPerTarget, direction) }
            
            // Finds out if more targets need to be adjusted, uses recursion if necessary
            if (!maxed.isEmpty)
            {
                // Finds the targets for the next iteration
                // Either a) remaining high priority items (if present) + other adjustable items (adjustable)
                // or b) lower priority items (after all high priority items are maxed)
                // or c) remaining non-maxed items (if there were no high priority items)
                val nextTargets = 
                {
                    if (topPriority.isEmpty)
                        nonMaxed
                    else
                        nonMaxed ++ (adjustable.map { _._1 }.filterNot { _.stackSize.isLowPriorityFor(direction) })
                }
                
                if (!nextTargets.isEmpty)
                {
                    // Calculates remaining adjustment
                    val remainingAdjustment = maxed.map { _._2 }.foldLeft(0.0) { 
                            case (total, max) => total + (adjustmentPerTarget - max) }
                    
                    // Performs the next iteration
                    adjustLength(nextTargets, remainingAdjustment)
                }
            }
        }
    }*/
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
    
    /**
     * How much this component may still be enlarged before reaching maximum size (None if no maximum)
     */
    @deprecated("Use LengthAdjust", "v0.1")
    def maxIncrease(direction: Axis2D) = stackSize.along(direction).max.map { 
            max => max - size.lengthAlong(direction) }
    
    /**
     * How much this component may still be shrinked before reaching minimum size
     */
    @deprecated("Use LengthAdjust", "v0.1")
    def maxShrink(direction: Axis2D) = size.lengthAlong(direction) - stackSize.along(direction).min
    
    /**
     * The maximum size adjustment, depending on the targetAdjustment type (shrink or increase)
     * @direction adjustment direction
     * @targetAdjustment an example adjustment
     * @return a positive (increase) or negative (shrink) number for maximum adjustment (None if no maximum)
     */
    @deprecated("Use LengthAdjust", "v0.1")
    def maxAdjustment(direction: Axis2D, targetAdjustment: Double) = 
    {
        if (targetAdjustment > 0)
            maxIncrease(direction)
        else
            Some(-maxShrink(direction))
    }
}

private trait LengthAdjust
{
    // ATTRIBUTES    -----------------
    
    private var currentAdjust = 0.0
    
    
    // ABSTRACT    -------------------
    
    def length: StackLength
    
    protected def setLength(length: Double): Unit
    
    
    // COMPUTED    -------------------
    
    def isLowPriority = length.lowPriority
    
    private def max = length.max map { _ - length.optimal }
    
    private def min = length.min - length.optimal
    
    
    // OPERATORS    ------------------
    
    def +=(amount: Double) = 
    {
        val target = currentAdjust + amount
        
        if (amount < 0)
        {
            if (target > min)
            {
                currentAdjust = target
                0.0
            }
            else
            {
                currentAdjust = min
                min - target
            }
        }
        else if (amount > 0)
        {
            if (max exists { target > _ })
            {
                currentAdjust = max.get
                max.get - target
            }
            else
            {
                currentAdjust = target
                0.0
            }
        }
        else
            0.0
    }
    
    
    // OTHER    ---------------------
    
    def apply() = setLength(length.optimal + currentAdjust)
}

private class StackableLengthAdjust(private val target: CacheStackable, 
        private val direction: Axis2D) extends LengthAdjust
{
    def length = target.stackSize.along(direction)
    
    def setLength(length: Double) = target.setLength(length, direction)
}

private class GapLengthAdjust(private val target: Pointer[Double], 
        val length: StackLength) extends LengthAdjust
{
    def setLength(length: Double) = target.set(length)
}