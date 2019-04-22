package utopia.reflection.component

import javax.swing.SwingConstants
import utopia.genesis.shape.{Axis2D, X, Y}
import utopia.reflection.component.Alignment.Center

object Alignment
{
	/**
	  * In left alignment, content is leading
	  */
	case object Left extends Alignment
	{
		override val supportedAxes = Set(X)
		
		override def opposite = Right
		
		override def toSwingAlignment = SwingConstants.LEFT
	}
	
	/**
	  * In right alignment, content is trailing
	  */
	case object Right extends Alignment
	{
		override val supportedAxes = Set(X)
		
		override def opposite = Left
		
		override def toSwingAlignment = SwingConstants.RIGHT
	}
	
	/**
	  * In top alignment, content is positioned at the the top of a component
	  */
	case object Top extends Alignment
	{
		override val supportedAxes = Set(Y)
		
		override def opposite = Bottom
		
		override def toSwingAlignment = SwingConstants.TOP
	}
	
	/**
	  * In bottom alignment, content is positioned at the bottom of a component
	  */
	case object Bottom extends Alignment
	{
		override val supportedAxes = Set(Y)
		
		override def opposite = Top
		
		override def toSwingAlignment = SwingConstants.BOTTOM
	}
	
	/**
	  * In center alignment, content is positioned at the center of a component
	  */
	case object Center extends Alignment
	{
		override val supportedAxes = Set(X, Y)
		
		override def opposite = this
		
		override def toSwingAlignment = SwingConstants.CENTER
	}
	
	
	// ATTRIBUTES	--------------------
	
	/**
	  * All possible values for Alignment
	  */
	val values = Vector(Left, Right, Top, Bottom, Center)
	
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
	  * @param alignment Swing constant for alignment
	  * @return An alignment matchin the swing constant. None if no alignment matches the constant.
	  */
	def forSwingAlignment(alignment: Int) = values.find { _.toSwingAlignment == alignment }
}

/**
  * Alignments are used for specifying content position when there's additional room
  * @author Mikko Hilpinen
  * @since 22.4.2019, v1+
  */
sealed trait Alignment
{
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
	  * @return A swing constant that represents this alignment
	  */
	def toSwingAlignment: Int
	
	
	// COMPUTED	----------------
	
	/**
	  * @return Whether this alignment can be used for horizontal axis (X)
	  */
	def isHorizontal = supportedAxes.contains(X)
	
	/**
	  * @return Whether this alignment can be used for vertical axis (Y)
	  */
	def isVertical = supportedAxes.contains(Y)
	
	/**
	  * @return A version of this alignment that can be used for horizontal axis
	  */
	def horizontal = if (isHorizontal) this else Center
	
	/**
	  * @return A version of this alignment that can be used for vertical axis
	  */
	def vertical = if (isVertical) this else Center
	
	
	// OTHER	----------------
	
	/**
	  * @param axis Target axis
	  * @return A version of this alignment that can be used for the specified axis
	  */
	def along(axis: Axis2D) = if (supportedAxes.contains(axis)) this else Center
}
