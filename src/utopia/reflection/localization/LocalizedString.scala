package utopia.reflection.localization

object LocalizedString
{

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
	
	/**
	  * @return The ISO code for this string representation
	  */
	override def languageCode = targetLanguageCode getOrElse sourceLanguageCode
	
	/**
	  * @param regex A splitting regex
	  * @return A split version of this localized string
	  */
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
	
	
	// OPERATORS	--------------------------
	
	// TODO: Add string append with implicit localizer
}
