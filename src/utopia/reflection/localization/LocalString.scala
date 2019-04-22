package utopia.reflection.localization

import scala.language.implicitConversions

object LocalString
{
	/**
	  * Converts a string into a local string using the default language code (implicit)
	  * @param string A string
	  * @param defaultLanguageCode An implicit default language code
	  * @return A string as a local string in default language
	  */
	implicit def stringToLocal(string: String)(implicit defaultLanguageCode: String): LocalString =
		LocalString(string, defaultLanguageCode)
	
	def apply(string: String, languageCode: String) = new LocalString(string, Some(languageCode))
}

/**
  * LocalStrings are simple strings that know the language of their contents
  * @author Mikko Hilpinen
  * @since 22.4.2019, v1+
  * @param string A source string
  * @param languageCode The 2-character ISO code for the language of the string
  */
case class LocalString(override val string: String, override val languageCode: Option[String] = None) extends LocalStringLike[LocalString]
{
	// COMPUTED	--------------------------
	
	/**
	  * @return A version of this string where localization has been skipped
	  */
	def localizationSkipped = LocalizedString(this, None)
	
	
	// IMPLEMENTED	----------------------
	
	override def +(other: LocalString) =
	{
		val newCode =
		{
			if (languageCode.isDefined)
			{
				if (other.languageCode.forall { _ == languageCode.get }) languageCode else None
			}
			else
				other.languageCode
		}
		
		LocalString(string + other.string, newCode)
	}
	
	override def split(regex: String) = string.split(regex).toVector.map { LocalString(_, languageCode) }
	
	override def interpolate(firstArg: Any, moreArgs: Any*) = LocalString(parseArguments(string, firstArg +: moreArgs),
		languageCode)
	
	// OPERATORS	----------------------
	
	/**
	  * Appends a string at the end of this string
	  * @param str A string to append
	  * @return An appended local string
	  */
	def +(str: String) = LocalString(string + str, languageCode)
	
	
	// OTHER	--------------------------
	
	private def parseArguments(field: String, args: Seq[Any]) =
	{
		val str = new StringBuilder()
		
		var cursor = 0
		var nextArgIndex = 0
		
		while (cursor < field.length)
		{
			// Finds the next argument position
			val nextArgumentPosition = cursor + field.substring(cursor).indexOf('%')
			
			// After all arguments have been parsed, adds the remaining part of the string
			if (nextArgumentPosition < cursor)
			{
				str.append(field.substring(cursor))
				cursor = field.length
			}
			else
			{
				// The part between the arguments is kept as is
				str.append(field.substring(cursor, nextArgumentPosition))
				
				// The field may end in '%', in which case the following checks cannot be made
				if (field.length <= nextArgumentPosition + 1)
				{
					str.append('%')
					cursor = field.length
				}
				else
				{
					// Checks the argument type
					val argType = StringArgumentType(field.charAt(nextArgumentPosition + 1))
					
					// Sometimes '%' is used without argument type, in which case it is copied as is
					// This also happens when there aren't enough arguments provided
					if (argType.isEmpty || nextArgIndex >= args.size)
					{
						str.append('%')
						cursor = nextArgumentPosition + 1
					}
					else
					{
						// Parses argument and inserts it to string
						str.append(argType.get.parse(args(nextArgIndex)))
						nextArgIndex += 1
						cursor = nextArgumentPosition + 2
					}
				}
			}
		}
		
		str.toString()
	}
}
