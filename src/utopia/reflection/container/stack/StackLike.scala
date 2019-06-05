package utopia.reflection.container.stack

import utopia.flow.util.CollectionExtensions._
import utopia.flow.datastructure.mutable.Pointer
import utopia.genesis.shape.Axis2D
import utopia.genesis.shape.shape2D.{Bounds, Point, Size}
import utopia.reflection.component.stack.{StackSizeCalculating, Stackable, StackableWrapper}
import utopia.reflection.component.Area
import utopia.reflection.shape.{StackLength, StackSize}
import utopia.reflection.container.stack.StackLayout._

import scala.collection.immutable.VectorBuilder

/**
* A stack holds multiple stackable components in a stack-like manner either horizontally or vertically
* @author Mikko Hilpinen
* @since 25.2.2019
**/
trait StackLike[C <: Stackable] extends MultiStackContainer[C] with StackSizeCalculating
{
	// ATTRIBUTES    --------------------
    
    private var _components = Vector[StackItem[C]]()
    
    
    // ABSTRACT -------------------------
    
    /**
      * @return The direction of this stack (components will be placed along this direction)
      */
    def direction: Axis2D
    /**
      * @return The layout this stack uses
      */
    def layout: StackLayout
    /**
      * @return The margin between components
      */
    def margin: StackLength
    /**
      * @return The cap at each end of this stack
      */
    def cap: StackLength
    
    
    // COMPUTED    ----------------------
    
    private def numberOfMargins = (_components.count { _.isVisible } - 1) max 0
    private def totalMarginsLength = margin * numberOfMargins
    
    /**
      * @return The current length of this stack
      */
    def length = size.along(direction)
    /**
      * @return The current breadth (perpendicular length) of this stack
      */
    def breadth = size.perpendicularTo(direction)
    
    /**
     * The length (min, optimal, max) of this stack
     */
    def stackLength = stackSize.along(direction)
    /**
     * The breadth (min, optimal, max) of this stack
     */
    def stackBreadth = stackSize.perpendicularTo(direction)
    
    
    // IMPLEMENTED    -------------------
    
    override def components = _components map { _.source }
    
    override def +=(component: C) =
    {
        _components :+= new StackItem(component)
        super.+=(component)
    }
    
    override def -=(component: C) =
    {
        _components = _components.filterNot { _.source == component }
        super.-=(component)
    }
    
    protected def calculatedStackSize =
    {
        val visibleComponents = _components.filter { _.isVisible }
        
        if (visibleComponents.isEmpty)
            StackSize.any.withLowPriorityFor(direction)
        else
        {
            // Checks component sizes
            val sizes = visibleComponents map { _.stackSize }
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
                    breadths.reduce { _ max _ }.withMax(None)
            
            }.withPriority(breadths forall { _.isLowPriority })
            
            // Returns the final size
            StackSize(length, breadth, direction)
        }
    }
    
    def updateLayout() =
    {
        val visibleComponents = _components.filter { _.isVisible }
        
        if (visibleComponents.nonEmpty)
        {
            // Calculates the necessary length adjustment
            val lengthAdjustment = lengthAlong(direction) - stackSize.along(direction).optimal
            
            // Arranges the mutable items in a vector first. Treats margins and caps as separate items
            val caps = Vector.fill(2)(Pointer(0.0))
            val margins = Vector.fill(numberOfMargins)(Pointer(0.0))
            val targets =
            {
                val builder = new VectorBuilder[LengthAdjust]()
                
                // Starts with a cap
                builder += new GapLengthAdjust(caps.head, cap)
                
                // Next adds items with margins
                visibleComponents.zip(margins).foreach
                {
                    case (component, marginPointer) =>
                        builder += new StackableLengthAdjust(component, direction)
                        builder += new GapLengthAdjust(marginPointer, margin)
                }
                
                // Adds final component and final cap
                builder += new StackableLengthAdjust(visibleComponents.last, direction)
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
            visibleComponents.zip(margins).foreach
            {
                case (component, marginPointer) =>
                    component.setCoordinate(cursor, direction)
                    cursor += component.lengthAlong(direction) + marginPointer.get
            }
            visibleComponents.last.setCoordinate(cursor, direction)
            
            // Handles the breadth of the components too, as well as their perpendicular positioning
            val breadthAxis = direction.perpendicular
            val newBreadth = lengthAlong(breadthAxis)
            visibleComponents.foreach
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
            
            // Finally applies the changes
            visibleComponents.foreach { _.updateBounds() }
        }
    }
    
    
    // OTHER    -----------------------
    
    /**
      * Inserts an item at a specific index in this stack
      * @param component The new component to be added
      * @param index The index where the component will be added
      */
    def insert(component: C, index: Int) =
    {
        _components = (_components.take(index) :+ new StackItem(component)) ++ _components.drop(index)
        super.+=(component)
    }
    
    /**
      * Drops the last n items from this stack
      * @param amount The amount of items to remove from this stack
      */
    def dropLast(amount: Int) = _components dropRight amount map { _.source } foreach { -= }
    
    /**
      * Finds the area of a single element in this stack, including the area around the object
      * @param item An item in this stack
      * @return The bounds around the item. None if the item isn't in this stack
      */
    def areaOf(item: C) =
    {
        // Caches components so that indexes won't change in between
        val c = components
        c.optionIndexOf(item).map
        {
            i =>
                if (c.size == 1)
                    Bounds(Point.origin, size)
                else
                {
                    // Includes half of the area between items (if there is no item, uses cap)
                    val top = if (i > 0) (item.coordinateAlong(direction) - c(i - 1).maxCoordinateAlong(direction)) / 2 else
                        item.coordinateAlong(direction)
                    val bottom = if (i < c.size - 1) (c(i + 1).coordinateAlong(direction) - item.maxCoordinateAlong(direction)) / 2 else
                        length - item.maxCoordinateAlong(direction)
                
                    Bounds(item.position - (top, direction), item.size + (top + bottom, direction))
                }
        }
    }
    
    /**
      * Finds the item that's neares to a <b>relative</b> point
      * @param relativePoint A point relative to this Stack's position ((0, 0) = stack origin)
      * @return The component that's nearest to the provided point. None if this stack is empty
      */
    def itemNearestTo(relativePoint: Point) =
    {
        val p = relativePoint.along(direction)
        val c = components
        // Finds the first item past the relative point
        c.indexWhereOption { _.coordinateAlong(direction) > p }.map
        {
            nextIndex =>
                // Selects the next item if a) it's the first item or b) it's closer to point than the previous item
                if (nextIndex == 0 || c(nextIndex).coordinateAlong(direction) - p < p - c(nextIndex - 1).maxCoordinateAlong(direction))
                    c(nextIndex)
                else
                    c(nextIndex - 1)
                
        }.orElse(c.lastOption)
    }
    
    private def adjustLength(targets: Traversable[LengthAdjust], adjustment: Double): Double = 
    {
        // println(s"Adjusting length for ${targets.size} targets. Remaining adjustment: $adjustment")
        
        // Finds out how much each item should be adjusted
        val adjustmentPerComponent = adjustment / targets.size
        
        // Adjusts the items (some may be maxed) and caches results
        val results = targets map { target => target -> (target += adjustmentPerComponent) }
        
        // println(s"Remaining: (${results.map { _._2 }.fold(""){ _ + ", " + _ }})")
        
        // Finds out the remaining adjustment and available targets
        val remainingAdjustment = results.foldLeft(0.0) { case (total, next) => total + next._2 }
        val availableTargets = results.filter { _._2 == 0.0 }.map { _._1 }
        
        // println(s"Remaining total: $remainingAdjustment. Available targets: ${availableTargets.size}")
        
        // If necessary and possible, goes for the second round. Returns remaining adjustment
        if (availableTargets.isEmpty)
            remainingAdjustment
        else if (remainingAdjustment == 0.0)
            0.0
        else
            adjustLength(availableTargets, remainingAdjustment)
    }
}

private class StackItem[C <: Stackable](val source: C) extends Area with StackableWrapper
{
    // ATTRIBUTES    -------------------
    
    private var nextPosition: Option[Point] = None
    private var nextSize: Option[Size] = None
    
    
    // IMPLEMENTED    -----------------
    
    override def position = nextPosition getOrElse source.position
    override def position_=(p: Point) = nextPosition = Some(p)
    
    override def size = nextSize getOrElse source.size
    override def size_=(s: Size) = nextSize = Some(s)
    
    override protected def wrapped = source
    
    
    // OTHER    -----------------------
    
    def updateBounds() = 
    {
        nextPosition filterNot { _ ~== source.position } foreach { source.position = _ }
        nextPosition = None
        
        nextSize filterNot { _ ~== source.size } foreach { source.size = _ }
        nextSize = None
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
    
    def isLowPriority = length.isLowPriority
    
    private def max = length.max map { _ - length.optimal }
    
    private def min = length.min - length.optimal
    
    
    // OPERATORS    ------------------
    
    // Adjusts length, returns remaining adjustment
    def +=(amount: Double) = 
    {
        val target = currentAdjust + amount
        
        // Adjustment may be negative (shrinking) or positive (enlarging)
        if (amount < 0)
        {
            if (target > min)
            {
                currentAdjust = target
                0.0
            }
            // If trying to shrink below minimum, goes to minimum and returns remaining amount (negative)
            else
            {
                currentAdjust = min
                target - min
            }
        }
        else if (amount > 0)
        {
            // If trying to enlarge beyond maximum, caps at maximum and returns remaining amount (positive)
            if (max exists { target > _ })
            {
                currentAdjust = max.get
                target - max.get
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

private class StackableLengthAdjust(private val target: Stackable, private val direction: Axis2D) extends LengthAdjust
{
    def length = target.stackSize.along(direction)
    
    def setLength(length: Double) = target.setLength(length, direction)
}

private class GapLengthAdjust(private val target: Pointer[Double], val length: StackLength) extends LengthAdjust
{
    def setLength(length: Double) = target.set(length)
}