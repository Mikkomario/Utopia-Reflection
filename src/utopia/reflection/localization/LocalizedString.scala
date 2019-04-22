package utopia.reflection.localization

import scala.language.implicitConversions

object LocalizedString
{
	/**
	  * Creates a new localized string
	  * @param original The local string
	  * @param localized The localized version
	  * @return A new localized string
	  */
	def apply(original: LocalString, localized: LocalString) = new LocalizedString(original, Some(localized))
	
	/**
	  * Automatically converts a local string to localized string using an implicit localizer
	  * @param local A local string
	  * @param localizer A localizer (implicit)
	  * @return A localized version of the string
	  */
	implicit def autoLocalize(local: LocalString)(implicit localizer: Localizer[_]): LocalizedString =
		localizer.localizeWithoutContext(local)
}

/**
  * A localized string is a string that has been localized for user context
  * @author Mikko Hilpinen
  * @since 22.4.2019, v1+
  * @param original The non-localized version of this string
  * @param localized the localized version of this string (if available, None otherwise)
  */
case class LocalizedString(original: LocalString, localized: Option[LocalString]) extends LocalStringLike[LocalizedString]
{
	// COMPUTED	------------------------
	
	/**
	  * @return A string representation of this localized string
	  */
	def string = localized.getOrElse(original).string
	
	/**
	  * @return The ISO code for the source language
	  */
	def sourceLanguageCode = original.languageCode
	
	/**
	  * @return The ISO code for the target language
	  */
	def targetLanguageCode = localized.map { _.languageCode }
	
	/**
	  * @return Whether localized data is available
	  */
	def isLocalized = localized.isDefined
	
	
	// IMPLEMENTED	------------------------
	
	override def languageCode = targetLanguageCode getOrElse sourceLanguageCode
	
	override def split(regex: String) =
	{
		val originalSplits = original.split(regex)
		
		if (isLocalized)
		{
			val localizedSplits = localized.get.split(regex)
			
			if (originalSplits.size == localizedSplits.size)
				originalSplits.zip(localizedSplits).map { case (orig, loc) => LocalizedString(orig, Some(loc)) }
			else
				localizedSplits.map { LocalizedString(_, None) }
		}
		else
			originalSplits.map { LocalizedString(_, None) }
	}
	
	override def interpolate(firstArg: Any, moreArgs: Any*) = LocalizedString(original.interpolate(firstArg, moreArgs),
		localized.map { _.interpolate(firstArg, moreArgs) })
	
	
	// OPERATORS	--------------------------
	
	/**
	  * Appends this localized string with another localization
	  * @param str A string
	  * @param localizer A localizer that will loalize the string
	  * @return a combined localized string
	  */
	def +(str: String)(implicit localizer: Localizer[_]) = LocalizedString(original + str,
		localized.getOrElse(original) + localizer.localizeWithoutContext(LocalString(str, sourceLanguageCode)).string)
}
