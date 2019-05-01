package utopia.reflection.text

import utopia.genesis.color.Color
import utopia.reflection.localization.LocalizedString

/**
  * Prompts are used for hinting input values
  * @author Mikko Hilpinen
  * @since 1.5.2019, v1+
  */
case class Prompt(text: LocalizedString, font: Font, color: Color = Color.textBlackDisabled)
