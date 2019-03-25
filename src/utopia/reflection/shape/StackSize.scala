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
    
    def apply(min: Size, optimal: Size, maxWidth: Option[Int], maxHeight: Option[Int]) = 
            new StackSize(StackLength(min.width.toInt, optimal.width.toInt, maxWidth), 
            StackLength(min.height.toInt, optimal.height.toInt, maxHeight));
    
    def apply(min: Size, optimal: Size, max: Option[Size]): StackSize = apply(min, optimal, 
            max.map(_.width.toInt), max.map(_.height.toInt));
    
    def apply(min: Size, optimal: Size, max: Size): StackSize = apply(min, optimal, Some(max))
    
    def apply(length: StackLength, breadth: StackLength, axis: Axis2D): StackSize = axis match 
    {
        case X => StackSize(length, breadth)
        case Y => StackSize(breadth, length)
    }
    
    def any(optimal: Size) = StackSize(Size.zero, optimal, None)
    
    def fixed(size: Size) = StackSize(size, size, size)
    
    def upscaling(min: Size, optimal: Size) = StackSize(min, optimal, None)
    
    def upscaling(optimal: Size): StackSize = upscaling(optimal, optimal)
    
    def downscaling(optimal: Size, maxWidth: Int, maxHeight: Int) = StackSize(Size.zero, optimal, 
            Some(maxWidth), Some(maxHeight));
    
    def downscaling(optimal: Size, max: Size): StackSize = StackSize(Size.zero, optimal, max)
    
    def downscaling(max: Size): StackSize = downscaling(max, max)
    
    
    // OTHER    ---------------------
    
    def min(a: StackSize, b: StackSize) = StackSize(StackLength.min(a.width, b.width), 
            StackLength.min(a.height, b.height));
    
    def max(a: StackSize, b: StackSize) = StackSize(StackLength.max(a.width, b.width), 
            StackLength.max(a.height, b.height))
}

/**
* This class represents a size that may vary within minimum and maximum limits
* @author Mikko Hilpinen
* @since 25.2.2019
**/
case class StackSize(val width: StackLength, val height: StackLength)
{
    // COMPUTED PROPERTIES    --------
    
    def min = Size(width.min, height.min)
    
    def optimal = Size(width.optimal, height.optimal)
    
    def max = 
    {
        if (width.max.isDefined && height.max.isDefined)
            Some(Size(width.max.get, height.max.get))
        else
            None
    }
    
    
    // IMPLEMENTED    ----------------
    
    override def toString() = s"[$width, $height]"
    
    
    // OPERATORS    ------------------
    
    def +(size: Size) = StackSize(width + size.width.toInt, height + size.height.toInt)
    
    
	// OTHER    ----------------------
    
    def along(axis: Axis2D) = axis match 
    {
        case X => width
        case Y => height
    }
    
    def perpendicularTo(axis: Axis2D) = along(axis.perpendicular)
    
    def isLowPriorityFor(axis: Axis2D) = along(axis).lowPriority
    
    def withWidth(w: StackLength) = StackSize(w, height)
    
    def withHeight(h: StackLength) = StackSize(width, h)
    
    def withSide(side: StackLength, axis: Axis2D) = axis match 
    {
        case X => withWidth(side)
        case Y => withHeight(side)
    }
    
    def mapWidth(map: StackLength => StackLength) = withWidth(map(width))
    
    def mapHeight(map: StackLength => StackLength) = withHeight(map(height))
    
    def mapSide(axis: Axis2D, map: StackLength => StackLength) = axis match 
    {
        case X => mapWidth(map)
        case Y => mapHeight(map)
    }
    
    def withLowPriorityFor(axis: Axis2D) = mapSide(axis, _.withLowPriority)
    
    def withAnyWidth = withWidth(StackLength.any)
    
    def withAnyHeight = withHeight(StackLength.any)
    
    def withFixedSide(length: Int, axis: Axis2D) = withSide(StackLength.fixed(length), axis)
    
    def withFixedWidth(w: Int) = withFixedSide(w, X)
    
    def withFixedHeight(h: Int) = withFixedSide(h, Y)
    
    def withMaxSide(maxLength: Int, axis: Axis2D) = mapSide(axis, _.withMax(maxLength))
    
    def withMaxWidth(maxW: Int) = withMaxSide(maxW, X)
    
    def withMaxHeight(maxH: Int) = withMaxSide(maxH, Y)
    
    def withOptimalSide(optimalLength: Int, axis: Axis2D) = mapSide(axis, _.withOptimal(optimalLength))
    
    def withOptimalWidth(optimalW: Int) = withOptimalSide(optimalW, X)
    
    def withOptimalHeight(optimalH: Int) = withOptimalSide(optimalH, Y)
    
    def withMinSide(minLength: Int, axis: Axis2D) = mapSide(axis, _.withMin(minLength))
    
    def withMinWidth(minW: Int) = withMinSide(minW, X)
    
    def withMinHeight(minH: Int) = withMinSide(minH, Y)
    
    def limitedTo(size: Size) = StackSize.min(this, StackSize.fixed(size))
    
    def combine(other: StackSize) = StackSize(width combine other.width, height combine other.height)
}