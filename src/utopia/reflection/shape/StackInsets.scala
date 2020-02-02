package utopia.reflection.shape

import utopia.reflection.shape.LengthExtensions._
import utopia.genesis.shape.shape2D.Direction2D

object StackInsets extends InsetsFactory[StackLength, StackSize, StackInsets, StackInsets]
{
	/**
	  * Creates a symmetric set of stack insets
	  * @param margins Combined stack insets on each side
	  * @return A symmetric set of insets where the total value is equal to the provided size
	  */
	def symmetric(margins: StackSize): StackInsets = symmetric(margins.width / 2, margins.height / 2)
}

/**
  * A set of insets made of stack lengths
  * @author Mikko Hilpinen
  * @since 2.2.2020, v1
  */
case class StackInsets(amounts: Map[Direction2D, StackLength]) extends InsetsLike[StackLength, StackSize, StackInsets]
{
	// IMPLEMENTED	-----------------------
	
	override protected def makeCopy(newAmounts:  Map[Direction2D, StackLength])  = StackInsets(amounts)
	override protected def makeZero  = 0.fixed
	override protected def combine(first: StackLength, second: StackLength)  = first + second
	override protected def multiply(a: StackLength, multiplier: Double)  = a * multiplier
	override protected def make2D(horizontal: StackLength, vertical: StackLength)  = StackSize(horizontal, vertical)
}