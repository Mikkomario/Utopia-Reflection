package utopia.reflection.shape

import utopia.genesis.shape.shape2D.Size
import utopia.genesis.shape.shape2D.Point

object Insets
{
    /**
     * Converts an awt insets into insets
     */
    def of(insets: java.awt.Insets) = Insets(insets.left, insets.right, insets.top, insets.bottom)
    
    /**
     * Creates a symmetric set of insets where top = bottom and left = right
     * @param size the total size of the insets
     */
    def symmetric(size: Size) = 
    {
        val w = (size.width / 2).toInt
        val h = (size.height / 2).toInt
        
        Insets(w, w, h, h)
    }
}

/**
* Insets can be used for describing an area around a component (top, bottom, left and right)
* @author Mikko Hilpinen
* @since 25.3.2019
**/
case class Insets(val left: Int, val right: Int, val top: Int, val bottom: Int)
{
	// COMPUTED    ---------------
    
    /**
     * The total size of these insets
     */
    def total = Size(left + right, top + bottom)
    
    /**
     * Converts this insets instance to awt equivalent
     */
    def toAwt = new java.awt.Insets(top, left, bottom, right)
    
    /**
     * The top left position inside these insets
     */
    def toPoint = Point(left, top)
}