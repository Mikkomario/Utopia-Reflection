package utopia.reflection.util

import utopia.genesis.color.RGB

/**
  * Defines program default colors
  * @author Mikko Hilpinen
  * @since 17.11.2019, v1
  * @param primary The primary color's used
  * @param secondary The secondary color's used
  * @param gray suplementary grayscale colors used
  */
case class ColorScheme(primary: ColorSet, secondary: ColorSet, gray: ColorSet = ColorSet(RGB.grayWithValue(225),
	RGB.grayWithValue(245), RGB.grayWithValue(164)))
