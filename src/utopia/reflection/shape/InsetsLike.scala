package utopia.reflection.shape

import utopia.flow.util.CollectionExtensions._
import utopia.genesis.shape.Axis.{X, Y}
import utopia.genesis.shape.Axis2D
import utopia.genesis.shape.shape2D.Direction2D._
import utopia.genesis.shape.shape2D.Direction2D

import scala.collection.immutable.HashMap

trait InsetsFactory[L, S, +Repr, +I <: InsetsLike[L, S, Repr]]
{
    // ABSTRACT ---------------------------
    
    /**
      * Creates a new set of insets
      * @param amounts Lengths of each side in these insets
      * @return A set of insets with specified lengths
      */
    def apply(amounts: Map[Direction2D, L]): I
    
    
    // OTHER    ---------------------------
    
    /**
      * Creates new insets
      * @param left Amount of insets on the left side
      * @param right Amount of insets on the right side
      * @param top Amount of insets on the top side
      * @param bottom Amount of insets on the bottom side
      * @return A new set of insets
      */
    def apply(left: L, right: L, top: L, bottom: L): I = apply(
        HashMap(Left -> left, Right -> right, Up -> top, Down -> bottom))
    
    /**
      * Creates a set of insets where top = bottom and left = right
      * @param w The left & right inset
      * @param h The top & bottom inset
      * @return A new set of insets
      */
    def symmetric(w: L, h: L) = apply(w, w, h , h)
    
    /**
      * @param sideWidth The width of each side on these insets
      * @return Insets with all sides equal
      */
    def symmetric(sideWidth: L) = apply(sideWidth, sideWidth, sideWidth, sideWidth)
    
    /**
      * Creates a horizontal set of insets
      * @param left Left side
      * @param right Right side
      * @return New insets
      */
    def horizontal(left: L, right: L) = apply(HashMap(Left -> left, Right -> right))
    
    /**
      * Creates a horizontal set of insets
      * @param w left / right side
      * @return New insets
      */
    def horizontal(w: L): I = horizontal(w, w)
    
    /**
      * Creates a vertical set of insets
      * @param top Top side
      * @param bottom Bottom side
      * @return New insets
      */
    def vertical(top: L, bottom: L) = apply(HashMap(Up -> top, Down -> bottom))
    
    /**
      * Creates a vertical set of insets
      * @param h Top / bottom side
      * @return New insets
      */
    def vertical(h: L): I = vertical(h, h)
    
    /**
      * @param direction Target direction
      * @param amount length of inset
      * @return An inset with only one side
      */
    def towards(direction: Direction2D, amount: L) = apply(HashMap(direction -> amount))
    
    /**
      * @param amount Length of inset
      * @return An inset with only left side
      */
    def left(amount: L) = towards(Left, amount)
    
    /**
      * @param amount Length of inset
      * @return An inset with only right side
      */
    def right(amount: L) = towards(Right, amount)
    
    /**
      * @param amount Length of inset
      * @return An inset with only top side
      */
    def top(amount: L) = towards(Up, amount)
    
    /**
      * @param amount Length of inset
      * @return An inset with only bottom side
      */
    def bottom(amount: L) = towards(Down, amount)
}

/**
* Insets can be used for describing an area around a component (top, bottom, left and right)
* @author Mikko Hilpinen
* @since 25.3.2019
**/
trait InsetsLike[L, +S, +Repr]
{
    // ABSTRACT ------------------
    
    /**
      * @return Lengths of each side in these insets
      */
    def amounts: Map[Direction2D, L]
    
    /**
      * @param newAmounts New lengths
      * @return A new copy of these insets with specified lengths
      */
    protected def makeCopy(newAmounts: Map[Direction2D, L]): Repr
    
    /**
      * @return A zero length item
      */
    protected def makeZero: L
    
    /**
      * @param first The first item
      * @param second The second item
      * @return Combination (+) of these two items
      */
    protected def combine(first: L, second: L): L
    
    /**
      * @param a The item to multiply
      * @param multiplier Multiplier
      * @return A multiplied copy of the item
      */
    protected def multiply(a: L, multiplier: Double): L
    
    /**
      * Combines two lengths into a size
      * @param horizontal Horizontal length
      * @param vertical Vertical length
      * @return A size
      */
    protected def make2D(horizontal: L, vertical: L): S
    
    
	// COMPUTED    ---------------
    
    /**
     * @return Insets for the left side
     */
    def left = amounts.getOrElse(Left, makeZero)
    
    /**
     * @return Insets for the right side
     */
    def right = amounts.getOrElse(Right, makeZero)
    
    /**
     * @return Insets for the top side
     */
    def top = amounts.getOrElse(Up, makeZero)
    
    /**
     * @return Insets for the bottom side
     */
    def bottom = amounts.getOrElse(Down, makeZero)
    
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
    def total = make2D(horizontal, vertical)
    
    /**
      * @return A copy of these insets where up is down and left is right
      */
    def opposite = makeCopy(amounts.map { case (k, v) => k.opposite -> v })
    
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
    def average = amounts.values.reduceOption(combine).map { multiply(_, 0.25) }.getOrElse(makeZero)
    
    
    // OPERATORS    --------------
    
    /**
      * Multiplies each side of these insets
      * @param multi A multiplier
      * @return Multiplied insets
      */
    def *(multi: Double) = makeCopy(amounts.map { case (side, length) => side -> multiply(length, multi) })
    
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
    def +(other: InsetsLike[L, _, _]) = makeCopy(amounts.mergedWith[L](other.amounts, combine))
    
    /**
      * Subtracts insets from each other
      * @param other Another insets
      * @return A subtraction of these two insets
      */
    def -(other: InsetsLike[L, _, _]) = makeCopy(amounts.mergedWith[L](other.amounts, (a, b) => combine(a, multiply(b, -1))))
    
    
    // OTHER    ------------------
    
    /**
     * @param axis Target axis
     * @return Total length of these insets along specified axis
     */
    def along(axis: Axis2D) = amounts.filterKeys { _.axis == axis }.values.reduceOption(combine).getOrElse(makeZero)
    
    /**
      * @param axis Target axis
      * @return The two sides of insets along that axis (FFor x-axis, returns left -> right and for y-axis top -> bottom)
      */
    def sidesAlong(axis: Axis2D) = axis match
    {
        case X => left -> right
        case Y => top -> bottom
    }
    
    /**
     * @param axis Target axis
     * @return A copy of these insets where values on targeted axis are swapped
     */
    def mirroredAlong(axis: Axis2D) = makeCopy(amounts.map { case (k, v) => (if (k.axis == axis) k.opposite else k) -> v })
    
    /**
     * @param direction The direction taken away from these insets
     * @return A copy of these insets without specified direction
     */
    def withoutSide(direction: Direction2D) = makeCopy(amounts - direction)
}