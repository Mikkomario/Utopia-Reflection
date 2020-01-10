package utopia.reflection.util

import scala.language.implicitConversions

import utopia.genesis.color.Color

object ColorSet
{
	/**
	  * @param set A color set
	  * @return Default color for the set
	  */
	implicit def setToColor(set: ColorSet): Color = set.default
	
	/**
	  * Converts a set of hexes into a color set
	  * @param defaultHex A hex for the standard color
	  * @param lightHex A hex for the light version
	  * @param darkHex A hex for the dark version
	  * @return A color set based on the hexes. Fails if some values couldn't be parsed
	  */
	def fromHexes(defaultHex: String, lightHex: String, darkHex: String) = Color.fromHex(defaultHex).flatMap { normal =>
		Color.fromHex(lightHex).flatMap { light => Color.fromHex(darkHex).map { dark => ColorSet(normal, light, dark) } }
	}
}

/**
  * A set of related colors
  * @author Mikko Hilpinen
  * @since 17.11.2019, v1
  * @param default The default color
  * @param light A lighter version of color
  * @param dark a darker version of color
  */
case class ColorSet(default: Color, light: Color, dark: Color)
