package utopia.reflection.localization

/**
  * Localizers are used for localizing string content
  * @author Mikko Hilpinen
  * @since 22.4.2019, v1+
  */
trait Localizer[-Meta]
{
	// ABSTRACT	------------------
	
	/**
	  * Localizes a string
	  * @param string A string to be localized
	  * @param context The localization context, if available
	  * @return A localized version of the string
	  */
	def localize(string: LocalString, context: Option[Meta]): LocalizedString
	
	
	// OTHER	------------------
	
	/**
	  * Localizes a string without any contextual information
	  * @param string A string to be localized
	  * @return A localized version of the string
	  */
	def localizeWithoutContext(string: LocalString) = localize(string, None)
	
	/**
	  * Localizes a string
	  * @param string A string to be localized
	  * @param context The localization context
	  * @return A localized version of the string
	  */
	def localize(string: LocalString, context: Meta): LocalizedString = localize(string, Some(context))
	
	/**
	  * Localizes a string, using interpolation (segments marked with %s, %S, %i or %d will be replaced with provided
	  * arguments)
	  * @param string A string to be localized
	  * @param context The localization context, if available
	  * @param firstArg The first interpolation argument
	  * @param moreArgs More interpolation arguments
	  * @return A localized, interpolated version of the provided string
	  */
	def localizeWith(string: LocalString, context: Option[Meta])(firstArg: Any, moreArgs: Any*) =
		localize(string, context).interpolate(firstArg, moreArgs)
	
	/**
	  * Localizes a string, using interpolation (segments marked with %s, %S, %i or %d will be replaced with provided
	  * arguments)
	  * @param string A string to be localized
	  * @param context The localization context
	  * @param firstArg The first interpolation argument
	  * @param moreArgs More interpolation arguments
	  * @return A localized, interpolated version of the provided string
	  */
	def localizeWith(string: LocalString, context: Meta)(firstArg: Any, moreArgs: Any*): LocalizedString =
		localizeWith(string, Some(context))(firstArg, moreArgs)
	
	/**
	  * Localizes a string, using interpolation (segments marked with %s, %S, %i or %d will be replaced with provided
	  * arguments). Doesn't specify localization context.
	  * @param string A string to be localized
	  * @param firstArg The first interpolation argument
	  * @param moreArgs More interpolation arguments
	  * @return A localized, interpolated version of the provided string
	  */
	def localizeWithoutContextWith(string: LocalString)(firstArg: Any, moreArgs: Any*) =
		localizeWith(string, None)(firstArg, moreArgs)
}