package utopia.reflection.shape

import utopia.genesis.shape.shape2D.Size
import utopia.genesis.shape.Axis2D
import utopia.genesis.shape.X
import utopia.genesis.shape.Y

object StackSize
{
    // ATTRIBUTES    --------------------
    
    /**
     * A stacksize that allows any value while preferring a zero size
     */
    val any: StackSize = any(Size.zero)
    
    
    // CONSTRUCTOR    -------------------
    
    /**
      * @param min Minimum size
      * @param optimal Optimal size
      * @param maxWidth Maximum width (None if not limited)
      * @param maxHeight Maximum height (None if not limited)
      * @return A new stack size
      */
    def apply(min: Size, optimal: Size, maxWidth: Option[Int], maxHeight: Option[Int]) = 
            new StackSize(StackLength(min.width.toInt, optimal.width.toInt, maxWidth), 
            StackLength(min.height.toInt, optimal.height.toInt, maxHeight))
    
    /**
      * @param min Minimum size
      * @param optimal Optimal size
      * @param max Maximum size (None if not limited)
      * @return A new stack size
      */
    def apply(min: Size, optimal: Size, max: Option[Size]): StackSize = apply(min, optimal, 
            max.map(_.width.toInt), max.map(_.height.toInt))
    
    /**
      * @param min Minimum size
      * @param optimal Optimal size
      * @param max Maximum size
      * @return A new stack size
      */
    def apply(min: Size, optimal: Size, max: Size): StackSize = apply(min, optimal, Some(max))
    
    /**
      * @param length Parallel stack length
      * @param breadth Perpendicular stack length
      * @param axis Axis that determines what is parallel and what is perpendicular
      * @return A new stack size
      */
    def apply(length: StackLength, breadth: StackLength, axis: Axis2D): StackSize = axis match 
    {
        case X => StackSize(length, breadth)
        case Y => StackSize(breadth, length)
    }
    
    /**
      * @param optimal Optimal size
      * @return A new stack size with no mimimum or maximum
      */
    def any(optimal: Size) = StackSize(Size.zero, optimal, None)
    
    /**
      * @param size Fixed size
      * @return A Stack size that is fixed to specified size
      */
    def fixed(size: Size) = StackSize(size, size, size)
    
    /**
      * @param min Minimum size
      * @param optimal Optimal size
      * @return A stack size that has no maximum
      */
    def upscaling(min: Size, optimal: Size) = StackSize(min, optimal, None)
    
    /**
      * @param optimal Optimal size
      * @return A stack size that has no maximum. Optimal is used as minimum.
      */
    def upscaling(optimal: Size): StackSize = upscaling(optimal, optimal)
    
    /**
      * @param optimal Optimal size
      * @param maxWidth Maximum width
      * @param maxHeight Maximum height
      * @return A stack size with no mimimum
      */
    def downscaling(optimal: Size, maxWidth: Int, maxHeight: Int) = StackSize(Size.zero, optimal, 
            Some(maxWidth), Some(maxHeight))
    
    /**
      * @param optimal Optimal size
      * @param max Maximum size
      * @return A stack size with no minimum
      */
    def downscaling(optimal: Size, max: Size): StackSize = StackSize(Size.zero, optimal, max)
    
    /**
      * @param max Maximum and optimal size
      * @return A stack size with no minimum. Max is used as optimal.
      */
    def downscaling(max: Size): StackSize = downscaling(max, max)
}

/**
* This class represents a size that may vary within minimum and maximum limits
* @author Mikko Hilpinen
* @since 25.2.2019
**/
case class StackSize(width: StackLength, height: StackLength)
{
    // COMPUTED PROPERTIES    --------
    
    /**
      * @return Minimum size
      */
    def min = Size(width.min, height.min)
    
    /**
      * @return Optimum size
      */
    def optimal = Size(width.optimal, height.optimal)
    
    /**
      * @return Maximum size. None if not specified
      */
    def max = 
    {
        if (width.max.isDefined && height.max.isDefined)
            Some(Size(width.max.get, height.max.get))
        else
            None
    }
    
    /**
      * @return The minimum width of this stack size
      */
    def minWidth = width.min
    
    /**
      * @return The minimum height of this stack size
      */
    def minHeight = height.min
    
    /**
      * @return The optimal width of this stack size
      */
    def optimalWidth = width.optimal
    
    /**
      * @return The optimal height of this stack size
      */
    def optimalHeight = height.optimal
    
    /**
      * @return The maximum width of this size. None if not specified.
      */
    def maxWidth = width.max
    
    /**
      * @return The maximum heigth of this size. None if not specified.
      */
    def maxHeight = height.max
    
    /**
      * @return A copy of this size with no width limits
      */
    def withAnyWidth = mapWidth { _.noLimits }
    
    /**
      * @return A copy of this size with no height limits
      */
    def withAnyHeight = mapHeight { _.noLimits }
    
    /**
      * @return A copy of this size with low priority for both width and height
      */
    def withLowPriority = mapWidth { _.withLowPriority }.mapHeight { _.withLowPriority }
    
    
    // IMPLEMENTED    ----------------
    
    override def toString = s"[$width, $height]"
    
    
    // OPERATORS    ------------------
    
    /**
      * @param size An increase in size
      * @return An increased version of this size
      */
    def +(size: Size) = StackSize(width + size.width.toInt, height + size.height.toInt)
    
    /**
      * @param other Another stack size
      * @return A sum of these two sizes (with combined min, optimal and max (if defined))
      */
    def +(other: StackSize) = StackSize(width + other.width, height + other.height)
    
    /**
      * @param size A decrease in size
      * @return A decreased version of this size
      */
    def -(size: Size) = this + (-size)
    
    /**
      * @param multi A multiplier
      * @return A multiplied version of this size
      */
    def *(multi: Double) = StackSize(width * multi, height * multi)
    
    /**
      * @param div A divider
      * @return A divided version of this size
      */
    def /(div: Double) = this * (1/div)
    
    
	// OTHER    ----------------------
    
    /**
      * @param axis Target axis
      * @return The length of this size along the specified axis
      */
    def along(axis: Axis2D) = axis match 
    {
        case X => width
        case Y => height
    }
    
    /**
      * @param axis Target axis
      * @return The length of this size perpendicular to the specified axis
      */
    def perpendicularTo(axis: Axis2D) = along(axis.perpendicular)
    
    /**
      * @param axis Target axis
      * @return Whether this size is considered of low priority for the specified axis
      */
    def isLowPriorityFor(axis: Axis2D) = along(axis).isLowPriority
    
    /**
      * @param w New width
      * @return A copy of this size with specified width
      */
    def withWidth(w: StackLength) = StackSize(w, height)
    
    /**
      * @param h New height
      * @return A copy of this size with specified height
      */
    def withHeight(h: StackLength) = StackSize(width, h)
    
    /**
      * @param side New length
      * @param axis Axis that specifies whether lenght is width or height
      * @return A copy of this size with new side
      */
    def withSide(side: StackLength, axis: Axis2D) = axis match 
    {
        case X => withWidth(side)
        case Y => withHeight(side)
    }
    
    /**
      * @param map A mapping function
      * @return A copy of this size with mapped width
      */
    def mapWidth(map: StackLength => StackLength) = withWidth(map(width))
    
    /**
      * @param map A mapping function
      * @return A copy of this size with mapped height
      */
    def mapHeight(map: StackLength => StackLength) = withHeight(map(height))
    
    /**
      * @param axis Target axis that determines mapped side
      * @param map A mapping function
      * @return A copy of this size with a mapped side
      */
    def mapSide(axis: Axis2D, map: StackLength => StackLength) = axis match 
    {
        case X => mapWidth(map)
        case Y => mapHeight(map)
    }
    
    /**
      * @param axis Target axis
      * @return A copy of this size with low priority for specified axis
      */
    def withLowPriorityFor(axis: Axis2D) = if (isLowPriorityFor(axis)) this else mapSide(axis, _.withLowPriority)
    
    /**
      * @param length New fixed length
      * @param axis Target axis
      * @return A copy of this size with fixed length for specified side
      */
    def withFixedSide(length: Int, axis: Axis2D) = withSide(StackLength.fixed(length), axis)
    
    /**
      * @param w New fixed width
      * @return A copy of this size with fixed width
      */
    def withFixedWidth(w: Int) = withFixedSide(w, X)
    
    /**
      * @param h new fixed height
      * @return A copy of this size with fixed height
      */
    def withFixedHeight(h: Int) = withFixedSide(h, Y)
    
    /**
      * @param maxLength New maximum length
      * @param axis target axis
      * @return A copy of this size with new maximum length for specified side
      */
    def withMaxSide(maxLength: Int, axis: Axis2D) = mapSide(axis, _.withMax(maxLength))
    
    /**
      * @param maxW New maximum width
      * @return A copy of this size with specified maximum width
      */
    def withMaxWidth(maxW: Int) = withMaxSide(maxW, X)
    
    /**
      * @param maxH New maximum height
      * @return A copy of this size with specified maximum height
      */
    def withMaxHeight(maxH: Int) = withMaxSide(maxH, Y)
    
    /**
      * @param max New max size
      * @return A copy of this size with specified maximum
      */
    def withMax(max: Size) = withMaxWidth(max.width.toInt).withMaxHeight(max.height.toInt)
    
    /**
      * @param optimalLength New optimal length
      * @param axis Target axis
      * @return A copy of this size with specified optimal length for the specified axis
      */
    def withOptimalSide(optimalLength: Int, axis: Axis2D) = mapSide(axis, _.withOptimal(optimalLength))
    
    /**
      * @param optimalW New optimal width
      * @return A copy of this size with specified optimal width
      */
    def withOptimalWidth(optimalW: Int) = withOptimalSide(optimalW, X)
    
    /**
      * @param optimalH New optimal height
      * @return A copy of this size with specified optimal height
      */
    def withOptimalHeight(optimalH: Int) = withOptimalSide(optimalH, Y)
    
    /**
      * @param optimal New optimal size
      * @return A copy of this size with specified optimal size
      */
    def withOptimal(optimal: Size) = withOptimalWidth(optimal.width.toInt).withOptimalHeight(optimal.height.toInt)
    
    /**
      * @param minLength New minimum length
      * @param axis Target axis
      * @return A copy of this size with specified minimum length for the specified axis
      */
    def withMinSide(minLength: Int, axis: Axis2D) = mapSide(axis, _.withMin(minLength))
    
    /**
      * @param minW A new minimum width
      * @return A copy of this size with specified minimum width
      */
    def withMinWidth(minW: Int) = withMinSide(minW, X)
    
    /**
      * @param minH A new minimum height
      * @return A copy of this size with specified minimum height
      */
    def withMinHeight(minH: Int) = withMinSide(minH, Y)
    
    /**
      * @param min A new minimum size
      * @return A copy of this size with specified minimum size
      */
    def withMin(min: Size) = withMinWidth(min.width.toInt).withMinHeight(min.height.toInt)
    
    /**
      * @param size A limiting size
      * @return A copy of this size that always fits to the specified size
      */
    def limitedTo(size: Size) = min(StackSize.fixed(size))
    
    /**
      * @param other Another size
      * @return A minimum between these two sizes
      */
    def min(other: StackSize) = StackSize(width min other.width, height min other.height)
    
    /**
      * @param other Another size
      * @return A maximum between these two sizes
      */
    def max(other: StackSize) = StackSize(width max other.width, height max other.height)
    
    /**
      * @param other Another size
      * @return A combination of these sizes that fulfills constraints of both sizes
      */
    def combine(other: StackSize) = StackSize(width combine other.width, height combine other.height)
}