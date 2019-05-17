package utopia.reflection.container.stack

import utopia.genesis.shape.Axis2D
import utopia.reflection.shape.StackLengthLimit

import scala.collection.immutable.HashMap

/**
  * Scroll views are containers that allow horizontal or vertical content scrolling
  * @author Mikko Hilpinen
  * @since 30.4.2019, v1+
  */
trait ScrollViewLike extends ScrollAreaLike
{
	// ABSTRACT	--------------------
	
	/**
	  * @return The scrolling axis of this scroll view
	  */
	def axis: Axis2D
	/**
	  * @return The length limits of this scroll view
	  */
	def lengthLimit: StackLengthLimit
	
	
	// COMPUTED	--------------------
	
	/**
	  * @return The length of this scroll view
	  */
	def length = lengthAlong(axis)
	def length_=(newLength: Double) = setLength(newLength, axis)
	/**
	  * @return The breadth of this scroll view
	  */
	def breadth = lengthAlong(axis.perpendicular)
	def breadth_=(newBreadth: Double) = setLength(newBreadth, axis.perpendicular)
	
	/**
	  * @return The length of this view's contents
	  */
	def contentLength = contentSize.along(axis)
	/**
	  * @return The breadth of this view's contents
	  */
	def contentBreadth = contentSize.perpendicularTo(axis)
	
	/**
	  * @return The current position of this view's contents (negative)
	  */
	def contentPosition = contentOrigin.along(axis)
	def contentPosition_=(pos: Double) = contentOrigin = contentOrigin.withCoordinate(pos, axis)
	
	/**
	  * @return The smallest possible content position (= position when scrolled at bottom)
	  */
	def minContentPosition = length - contentLength
	
	/**
	  * @return The current scroll modifier / percentage [0, 1]
	  */
	def scrollPercent = -contentPosition / contentLength
	def scrollPercent_=(newPercent: Double) = scrollTo(newPercent, axis)
	
	/**
	  * @return Whether the content is currently scrolled to the top
	  */
	def isAtTop = contentPosition >= 0
	/**
	  * @return Whether the content is currently scrolled to the bottom
	  */
	def isAtBottom = contentPosition + contentLength <= length
	
	
	// IMPLEMENTED	----------------
	
	override def axes = Vector(axis)
	
	override def lengthLimits = HashMap(axis -> lengthLimit)
	
	
	// OTHER	----------------------
	
	/**
	  * Scrolls this scroll view to display content top
	  */
	def scrollToTop() = contentPosition = 0
	/**
	  * Scrolls this scroll view to display content bottom
	  */
	def scrollToBottom() = contentPosition = minContentPosition
	/**
	  * Scrolls to a specific percentage
	  * @param abovePercent The portion of the content that should be above the view [0, 1]
	  */
	def scrollTo(abovePercent: Double) = contentPosition = -contentLength * abovePercent
	/**
	  * Scrolls this view a certain amount
	  * @param amount The amount of pixels scrolled
	  */
	def scroll(amount: Double) = contentPosition += amount
}
