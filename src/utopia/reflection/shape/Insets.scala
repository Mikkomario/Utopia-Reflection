package utopia.reflection.shape

import utopia.flow.util.CollectionExtensions._
import utopia.genesis.shape.Axis.{X, Y}
import utopia.genesis.shape.Axis2D
import utopia.genesis.shape.shape2D.{Direction2D, Point, Size}
import utopia.genesis.shape.shape2D.Direction2D._

import scala.collection.immutable.HashMap

object Insets
{
    /**
     * Creates new insets
     * @param left Amount of insets on the left side
     * @param right Amount of insets on the right side
     * @param top Amount of insets on the top side
     * @param bottom Amount of insets on the bottom side
     * @return A new set of insets
     */
    def apply(left: Double, right: Double, top: Double, bottom: Double): Insets = Insets(
        HashMap(Left -> left, Right -> right, Up -> top, Down -> bottom))
    
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
      * Creates a set of insets where top = bottom and left = right
      * @param w The left & right inset
      * @param h The top & bottom inset
      * @return A new set of insets
      */
    def symmetric(w: Double, h: Double) = Insets(w, w, h , h)
    
    /**
      * Creates a horizontal set of insets
      * @param left Left side
      * @param right Right side
      * @return New insets
      */
    def horizontal(left: Double, right: Double) = Insets(HashMap(Left -> left, Right -> right))
    
    /**
      * Creates a horizontal set of insets
      * @param w left / right side
      * @return New insets
      */
    def horizontal(w: Double): Insets = horizontal(w, w)
    
    /**
      * Creates a vertical set of insets
      * @param top Top side
      * @param bottom Bottom side
      * @return New insets
      */
    def vertical(top: Double, bottom: Double) = Insets(HashMap(Up -> top, Down -> bottom))
    
    /**
      * Creates a vertical set of insets
      * @param h Top / bottom side
      * @return New insets
      */
    def vertical(h: Double): Insets = vertical(h, h)
    
    /**
     * @param direction Target direction
     * @param amount length of inset
     * @return An inset with only one side
     */
    def towards(direction: Direction2D, amount: Double) = Insets(HashMap(direction -> amount))
    
    /**
     * @param amount Length of inset
     * @return An inset with only left side
     */
    def left(amount: Double) = towards(Left, amount)
    
    /**
     * @param amount Length of inset
     * @return An inset with only right side
     */
    def right(amount: Double) = towards(Right, amount)
    
    /**
     * @param amount Length of inset
     * @return An inset with only top side
     */
    def top(amount: Double) = towards(Up, amount)
    
    /**
     * @param amount Length of inset
     * @return An inset with only bottom side
     */
    def bottom(amount: Double) = towards(Down, amount)
}

/**
* Insets can be used for describing an area around a component (top, bottom, left and right)
* @author Mikko Hilpinen
* @since 25.3.2019
**/
case class Insets(amounts: Map[Direction2D, Double])
{
	// COMPUTED    ---------------
    
    /**
     * @return Insets for the left side
     */
    def left = amounts.getOrElse(Left, 0.0)
    
    /**
     * @return Insets for the right side
     */
    def right = amounts.getOrElse(Right, 0.0)
    
    /**
     * @return Insets for the top side
     */
    def top = amounts.getOrElse(Up, 0.0)
    
    /**
     * @return Insets for the bottom side
     */
    def bottom = amounts.getOrElse(Down, 0.0)
    
    /**
     * @return Total length of this inset's horizontal components
     */
    def horizontal = along(X)
    
    /**
     * @return Total length of this inset's vertical components
     */
    def vertical = along(Y)
    
    /**
     * The total size of these insets
     */
    def total = Size(horizontal, vertical)
    
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
    def positive = Insets(amounts.map { case (k, v) => k -> (v max 0) })
    
    /**
      * @return A copy of these insets where up is down and left is right
      */
    def opposite = Insets(amounts.map { case (k, v) => k.opposite -> v })
    
    /**
      * @return A copy of these insets where left and right have been swapped
      */
    def hMirrored = mirroredAlong(X)
    
    /**
      * @return A copy of these insets where top and bottom have been swapped
      */
    def vMirrored = mirroredAlong(Y)
    
    /**
      * @return The average width of these insets
      */
    def average = amounts.values.sum / 4
    
    
    // OPERATORS    --------------
    
    /**
      * Multiplies each side of these insets
      * @param multi A multiplier
      * @return Multiplied insets
      */
    def *(multi: Double) = Insets(amounts.map { case (k, v) => k -> v * multi })
    
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
    def +(other: Insets) = Insets(amounts.mergedWith(other.amounts,  _ + _ ))
    
    /**
      * Subtracts insets from each other
      * @param other Another insets
      * @return A subtraction of these two insets
      */
    def -(other: Insets) = Insets(amounts.mergedWith(other.amounts, _ - _))
    
    
    // OTHER    ------------------
    
    /**
     * @param axis Target axis
     * @return Total length of these insets along specified axis
     */
    def along(axis: Axis2D) = amounts.filterKeys { _.axis == axis }.values.reduceOption { _ + _ }.getOrElse(0.0)
    
    /**
     * @param axis Target axis
     * @return A copy of these insets where values on targeted axis are swapped
     */
    def mirroredAlong(axis: Axis2D) = Insets(amounts.map { case (k, v) => (if (k.axis == axis) k.opposite else k) -> v })
}