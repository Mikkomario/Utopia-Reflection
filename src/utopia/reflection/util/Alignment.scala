package utopia.reflection.util

import javax.swing.SwingConstants
import utopia.genesis.shape.Axis._
import utopia.genesis.shape.Axis2D

import scala.collection.immutable.HashMap

object Alignment
{
	/**
	  * In left alignment, content is leading
	  */
	case object Left extends Alignment
	{
		override val supportedAxes = Set(X)
		
		override def opposite = Right
		
		override def swingComponents = HashMap(X -> SwingConstants.LEADING)
		
		override def horizontal = this
		
		override def vertical = Center
	}
	
	/**
	  * In right alignment, content is trailing
	  */
	case object Right extends Alignment
	{
		override val supportedAxes = Set(X)
		
		override def opposite = Left
		
		override def swingComponents = HashMap(X -> SwingConstants.TRAILING)
		
		override def horizontal = this
		
		override def vertical = Center
	}
	
	/**
	  * In top alignment, content is positioned at the the top of a component
	  */
	case object Top extends Alignment
	{
		override val supportedAxes = Set(Y)
		
		override def opposite = Bottom
		
		override def swingComponents = HashMap(Y -> SwingConstants.TOP)
		
		override def horizontal = Center
		
		override def vertical = this
	}
	
	/**
	  * In bottom alignment, content is positioned at the bottom of a component
	  */
	case object Bottom extends Alignment
	{
		override val supportedAxes = Set(Y)
		
		override def opposite = Top
		
		override def swingComponents = HashMap(Y -> SwingConstants.BOTTOM)
		
		override def horizontal = Center
		
		override def vertical = this
	}
	
	/**
	  * In center alignment, content is positioned at the center of a component
	  */
	case object Center extends Alignment
	{
		override val supportedAxes = Set(X, Y)
		
		override def opposite = this
		
		override def swingComponents = HashMap(X -> SwingConstants.CENTER, Y -> SwingConstants.CENTER)
		
		override def horizontal = this
		
		override def vertical = this
	}
	
	case object TopLeft extends Alignment
	{
		override def supportedAxes = Set(X, Y)
		
		override def opposite = BottomRight
		
		override def swingComponents = HashMap(X -> SwingConstants.CENTER, Y -> SwingConstants.TOP)
		
		override def horizontal = Left
		
		override def vertical = Top
	}
	
	case object TopRight extends Alignment
	{
		override def supportedAxes = Set(X, Y)
		
		override def opposite = BottomLeft
		
		override def swingComponents = HashMap(X -> SwingConstants.TRAILING, Y -> SwingConstants.TOP)
		
		override def horizontal = Right
		
		override def vertical = Top
	}
	
	case object BottomLeft extends Alignment
	{
		override def supportedAxes = Set(X, Y)
		
		override def opposite = TopRight
		
		override def swingComponents = HashMap(X -> SwingConstants.LEADING, Y -> SwingConstants.BOTTOM)
		
		override def horizontal = Left
		
		override def vertical = Bottom
	}
	
	case object BottomRight extends Alignment
	{
		override def supportedAxes = Set(X, Y)
		
		override def opposite = TopLeft
		
		override def swingComponents = HashMap(X -> SwingConstants.TRAILING, Y -> SwingConstants.BOTTOM)
		
		override def horizontal = Right
		
		override def vertical = Bottom
	}
	
	
	// ATTRIBUTES	--------------------
	
	/**
	  * All possible values for Alignment
	  */
	val values = Vector(Left, Right, Top, Bottom, Center, TopLeft, TopRight, BottomLeft, BottomRight)
	
	/**
	  * All horizontally supported values
	  */
	val horizontal = Vector(Left, Center, Right)
	
	/**
	  * All vertically supported values
	  */
	val vertical = Vector(Top, Center, Bottom)
	
	
	// OTHER	-----------------------
	
	/**
	  * @param alignment Searched swing alignment
	  * @return A horizontal alignment matching the specified swing alignment. None if no alignment matched.
	  */
	def forHorizontalSwingAlignment(alignment: Int): Option[Alignment] = horizontal.find { _.swingComponents.get(X).contains(alignment) }
	
	/**
	  * @param alignment Searched swing alignment
	  * @return A vertical alignment matching the specified swing alignment. None if no alignment matched.
	  */
	def forVerticalSwingAlignment(alignment: Int): Option[Alignment] = vertical.find { _.swingComponents.get(Y).contains(alignment) }
	
	/**
	  * @param alignment Swing constant for alignment
	  * @return An alignment matchin the swing constant. None if no alignment matches the constant.
	  */
	def forSwingAlignment(alignment: Int): Option[Alignment] = forHorizontalSwingAlignment(alignment) orElse
		forVerticalSwingAlignment(alignment)
	
	/**
	  * Finds an alignment that matches the specified swing alignment combo
	  * @param horizontal Horizontal swing alignment component
	  * @param vertical Vertical swing alignment component
	  * @return A matching alignment
	  */
	def forSwingAlignments(horizontal: Int, vertical: Int): Alignment =
	{
		val hMatch = forHorizontalSwingAlignment(horizontal).getOrElse(Center)
		val vMatch = forVerticalSwingAlignment(vertical).getOrElse(Center)
		
		vMatch match
		{
			case Top =>
				hMatch match
				{
					case Left => TopLeft
					case Right => TopRight
					case _ => Top
				}
			case Bottom =>
				hMatch match
				{
					case Left => BottomLeft
					case Right => BottomRight
					case _ => Bottom
				}
			case _ => hMatch
		}
	}
}

/**
  * Alignments are used for specifying content position when there's additional room
  * @author Mikko Hilpinen
  * @since 22.4.2019, v1+
  */
sealed trait Alignment
{
	import Alignment._
	
	// ABSTRACT	----------------
	
	/**
	  * @return The axes supported for this alignment
	  */
	def supportedAxes: Set[Axis2D]
	
	/**
	  * @return The opposite for this alignment, if there is one
	  */
	def opposite: Alignment
	
	/**
	  * @return Swing representation(s) of this alignment, may be horizontal and/or vertical
	  */
	def swingComponents: Map[Axis2D, Int]
	
	/**
	  * @return A version of this alignment that only affects the horizontal axis
	  */
	def horizontal: Alignment
	
	/**
	  * @return A version of this alignment that only affects the vertical axis
	  */
	def vertical: Alignment
	
	
	// COMPUTED	----------------
	
	/**
	  * @return Whether this alignment can be used for horizontal axis (X)
	  */
	def isHorizontal = supportedAxes.contains(X)
	
	/**
	  * @return Whether this alignment can be used for vertical axis (Y)
	  */
	def isVertical = supportedAxes.contains(Y)
	
	
	// OTHER	----------------
	
	/**
	  * @param axis Target axis
	  * @return A version of this alignment that can be used for the specified axis
	  */
	def along(axis: Axis2D) = if (supportedAxes.contains(axis)) this else Center
}
