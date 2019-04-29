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
    def symmetric(size: Size): Insets = symmetric(size.width / 2, size.height / 2)
    
    /**
      * Creates a set of insets where top = bottom and legt = right
      * @param w The left & right inset
      * @param h The top & bottom inset
      * @return A new set of insets
      */
    def symmetric(w: Double, h: Double) = Insets(w, w, h , h)
}

/**
* Insets can be used for describing an area around a component (top, bottom, left and right)
* @author Mikko Hilpinen
* @since 25.3.2019
**/
case class Insets(left: Double, right: Double, top: Double, bottom: Double)
{
	// COMPUTED    ---------------
    
    /**
     * The total size of these insets
     */
    def total = Size(left + right, top + bottom)
    
    /**
     * Converts this insets instance to awt equivalent
     */
    def toAwt = new java.awt.Insets(top.toInt, left.toInt, bottom.toInt, right.toInt)
    
    /**
     * The top left position inside these insets
     */
    def toPoint = Point(left, top)
    
    /**
      * @return A non-negative version of these insets
      */
    def positive = Insets(left max 0, right max 0, top max 0, bottom max 0)
    
    /**
      * @return A copy of these insets where up is down and left is right
      */
    def opposite = Insets(right, left, bottom, top)
    
    /**
      * @return A copy of these insets where left and right have been swapped
      */
    def hMirrored = Insets(right, left, top, bottom)
    
    /**
      * @return A copy of these insets where top and bottom have been swapped
      */
    def vMirrored = Insets(left, right, bottom, top)
    
    
    // OPERATORS    --------------
    
    /**
      * Multiplies each side of these insets
      * @param multi A multiplier
      * @return Multiplied insets
      */
    def *(multi: Double) = Insets(left * multi, right * multi, top * multi, bottom * multi)
    
    /**
      * Divides each side of these insets
      * @param div A divider
      * @return Divided insets
      */
    def /(div: Double) = this * (1/div)
    
    /**
      * Adds two insets together
      * @param other Another insets
      * @return A combination of these two insets
      */
    def +(other: Insets) = Insets(left + other.left, right + other.right, top + other.top, bottom + other.bottom)
    
    /**
      * Subtracts insets from each other
      * @param other Another insets
      * @return A subtraction of these two insets
      */
    def -(other: Insets) = Insets(left - other.left, right - other.right, top - other.top, bottom - other.bottom)
}