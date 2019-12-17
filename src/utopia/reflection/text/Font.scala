package utopia.reflection.text

import scala.language.implicitConversions
import java.awt

import utopia.reflection.text.FontStyle.Plain

object Font
{
	// Implicitly converts an awt font to Reflection font
	implicit def awtFontToFont(awtFont: java.awt.Font): Font = Font(awtFont.getName, awtFont.getSize,
		FontStyle.fromAwt(awtFont.getStyle).getOrElse(Plain))
}

/**
  * This is a wrapper for awt font, but also supports scaling
  * @author Mikko Hilpinen
  * @since 24.4.2019, v1+
  * @param name The name of this font
  * @param baseSize The base size for this font (adjusted by scaling factors)
  * @param style The style of this font
  * @param scaling The instance level scaling of this font (affects final font size)
  */
case class Font(name: String, baseSize: Int, style: FontStyle = FontStyle.Plain, scaling: Double = 1.0)
{
	// ATTRIBUTES	-------------------
	
	/**
	  * The awt representation of this font
	  */
	lazy val toAwt = new awt.Font(name, style.toAwt, (baseSize * scaling).toInt)
	
	
	// OPERATORS	-------------------
	
	/**
	  * @param scalingMod A scaling modifier
	  * @return A scaled version of this font
	  */
	def *(scalingMod: Double) = copy(scaling = scaling * scalingMod)
	
	/**
	  * @param div A division modifier
	  * @return A divided (downscaled) version of this font
	  */
	def /(div: Double) = copy(scaling = scaling / div)
}
