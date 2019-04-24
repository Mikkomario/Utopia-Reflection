package utopia.reflection.text

object FontStyle
{
	/**
	  * The plain (default) font style
	  */
	case object Plain extends FontStyle { def toAwt = java.awt.Font.PLAIN }
	
	/**
	  * The bold font style
	  */
	case object Bold extends FontStyle { def toAwt = java.awt.Font.BOLD }
	
	/**
	  * The italic font style
	  */
	case object Italic extends FontStyle { def toAwt = java.awt.Font.ITALIC }
}

/**
  * FontStyle trait represents a font presentation style (bold, italiziced etc.)
  * @author Mikko
  * @since 24.4.2019, v
  */
trait FontStyle
{
	/**
	  * @return The java.awt.Font integer representation of this font style
	  */
	def toAwt: Int
}
