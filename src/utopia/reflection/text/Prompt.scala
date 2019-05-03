package utopia.reflection.text

import utopia.genesis.color.Color
import utopia.reflection.localization.LocalizedString

/**
  * Prompts are used for hinting input values
  * @author Mikko Hilpinen
  * @since 1.5.2019, v1+
  * @param text The text displayed in the prompt
  * @param font The font used
  * @param color the font color (default = 55% opaque black)
  */
case class Prompt(text: LocalizedString, font: Font, color: Color = Color.textBlackDisabled)
