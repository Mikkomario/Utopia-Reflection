package utopia.reflection.component

import utopia.genesis.shape.shape2D.Point
import utopia.genesis.shape.shape2D.Size
import utopia.genesis.shape.shape2D.Bounds

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
}