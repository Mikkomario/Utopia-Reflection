package utopia.reflection.component

import utopia.genesis.shape.shape2D.Point
import utopia.genesis.shape.shape2D.Size
import utopia.genesis.shape.shape2D.Bounds
import utopia.genesis.shape.Axis2D
import utopia.genesis.shape.X
import utopia.genesis.shape.Y

/**
* This trait is extended by classes that occupy a certain 2D space (position + size)
* @author Mikko Hilpinen
* @since 26.2.2019
**/
trait Area
{
    // ABSTRACT    ------------------------
    
    def position: Point
    def position_=(p: Point)
    
    def size: Size
    def size_=(s: Size)
    
    
    // COMPUTED    -----------------------
    
	def x = position.x
    def x_=(newX: Double) = position = position.withX(newX)
    
    def y = position.y
    def y_=(newY: Double) = position = position.withY(newY)
    
    def width = size.width
    def width_=(w: Double) = size = size.withWidth(w)
    
    def height = size.height
    def height_=(h: Double) = size = size.withHeight(h)
    
    def bounds = Bounds(position, size)
    def bounds_=(b: Bounds) = 
    {
        position = b.position
        size = b.size
    }
    
    
    // OTHER    ------------------------
    
    /**
     * The x or y coordinate of this component
     */
    def coordinateAlong(axis: Axis2D) = axis match 
    {
        case X => x
        case Y => y
    }
    
    /**
     * Changes either x- or y-coordinate of this area
     * @param position the target coordinate
     * @param axis the target axis (X or Y)
     */
    def setCoordinate(position: Double, axis: Axis2D) = axis match 
    {
        case X => x = position
        case Y => y = position
    }
    
    /**
     * Adjusts either x- or y-coordinate of this area
     */
    def adjustCoordinate(adjustment: Double, axis: Axis2D) = axis match 
    {
        case X => x += adjustment
        case Y => y += adjustment
    }
    
    /**
     * The length of this component along the specified axis
     */
    def lengthAlong(axis: Axis2D) = axis match 
    {
        case X => width
        case Y => height
    }
    
    /**
     * Changes either the width or height of this area
     * @length the new side length
     * @param axis the target axis (X for width, Y for height)
     */
    def setLength(length: Double, axis: Axis2D) = axis match 
    {
        case X => width = length
        case Y => height = length
    }
    
    /**
     * Adjusts either the width (for X-axis) or height (for Y-axis) of this component
     */
    def adjustLength(adjustment: Double, axis: Axis2D) = axis match 
    {
        case X => width += adjustment
        case Y => height += adjustment
    }
}