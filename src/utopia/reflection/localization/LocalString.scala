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
}

/**
  * LocalStrings are simple strings that know the language of their contents
  * @author Mikko Hilpinen
  * @since 22.4.2019, v1+
  * @param string A source string
  * @param languageCode The 2-character ISO code for the language of the string
  */
case class LocalString(override val string: String, override val languageCode: String) extends LocalStringLike[LocalString]
{
	// COMPUTED	--------------------------
	
	/**
	  * @return A version of this string where localization has been skipped
	  */
	def localizationSkipped = LocalizedString(this, None)
	
	
	// OPERATORS	----------------------
	
	/**
	  * Appends a string at the end of this string
	  * @param str A string to append
	  * @return An appended local string
	  */
	def +(str: String) = LocalString(string + str, languageCode)
	
	
	// IMPLEMENTED	----------------------
	
	override def split(regex: String) = string.split(regex).toVector.map { LocalString(_, languageCode) }
}
