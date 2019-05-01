package utopia.reflection.text

object TextFilter
{
	val upperCase = TextFilter(None, true)
	
	def apply(regex: Regex): TextFilter = TextFilter(Some(regex), false)
	
	def upperCase(regex: Regex) = TextFilter(Some(regex), true)
}

/**
  * These filters alter text characters to fit it into some format
  * @author Mikko Hilpinen
  * @since 1.5.2019, v1+
  */
case class TextFilter(regex: Option[Regex], isOnlyUpperCase: Boolean)
{
	// ATTRIBUTES	--------------------
	
	private val negative = regex.map { -_ }
	
	
	// OPERATORS	--------------------
	
	// Null checks because you never know about swing components
	/**
	  * Checks whether this filter accepts the provided character
	  * @param character A character
	  * @return Whether this filter accepts the provided character
	  */
	def apply(character: String) = if (character == null) false else regex.forall { r => character.matches(r.string) }
	
	/**
	  * Formats a string that it only includes accepted values
	  * @param s A source string
	  * @return Result string
	  */
	def format(s: String) =
	{
		if (s == null || s.isEmpty)
			""
		else
		{
			// Deletes all sequences NOT included in the regex
			val replaced = negative.map { r => s.replaceAll(r.string, "") } getOrElse s
			// May convert all chars to upper case
			if (isOnlyUpperCase) replaced.toUpperCase else replaced
		}
	}
}
