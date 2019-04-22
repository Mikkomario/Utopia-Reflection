package utopia.reflection.localization

import scala.language.implicitConversions

object DisplayFunction
{
	// TYPES	----------------------
	
	type Transform[A] = A => LocalString
	type Localize[Meta] = (LocalString, Option[Meta]) => LocalizedString
	
	
	// ATTRIBUTES	------------------
	
	/**
	  * A DisplayFunction that displays the item as is, no transformation or localization applied
	  */
	val raw = noLocalization[Any] { a => LocalString(a.toString) }
	
	
	// IMPLICIT	----------------------
	
	/**
	  * Converts a function to displayfunction
	  * @param f A function that converts an item to a localized string
	  * @tparam A The source item type
	  * @tparam Meta Localization context type
	  * @return A displayfunction that uses the specified function
	  */
	implicit def functionToDisplayFunction[A, Meta](f: (A, Option[Meta]) => LocalizedString): DisplayFunction[A, Meta] =
		new DisplayFunction(f)
	
	
	// OTHER	----------------------
	
	/**
	  * Creates a new display function from two separate functions
	  * @param transform A transform function that transforms an item to string
	  * @param localize A localization function that localizes the produced string
	  * @tparam A The source item type
	  * @tparam Meta Localization context type
	  * @return A new display function
	  */
	def apply[A, Meta](transform: Transform[A])(localize: Localize[Meta]) =
		new DisplayFunction[A, Meta]((item, meta) => localize(transform(item), meta))
	
	/**
	  * This displayfunction takes in local strings and localizes them with specified function
	  * @param localize A localization function
	  * @tparam Meta Localization context type
	  * @return A new display function
	  */
	def stringLocalized[Meta](localize: Localize[Meta]) =
		DisplayFunction[LocalString, Meta]{ s => s }(localize)
	
	/**
	  * This displayFunction takes in local strings and localizes them using the specified localizer (implicit)
	  * @param localizer A localizer that does the localization (implicit)
	  * @tparam Meta Localization context type
	  * @return A new display function
	  */
	def stringLocalized[Meta]()(implicit localizer: Localizer[Meta]): DisplayFunction[LocalString, Meta] =
		stringLocalized(localizer.localize)
	
	/**
	  * This display function uses an item's toString function to transform it to string and then a localization
	  * function to localize the produced string
	  * @param localize A localization function
	  * @tparam Meta Localization context type
	  * @return A new display function
	  */
	def localizeOnly[Meta](localize: Localize[Meta]) =
		DisplayFunction[Any, Meta]{ a => LocalString(a.toString) }(localize)
	
	/**
	  * This display function only transforms an item into string form but doesn't then perform a localization on the
	  * string
	  * @param transform A transform function for transforming the item to local string format
	  * @tparam A Source item type
	  * @return A new display function
	  */
	def noLocalization[A](transform: Transform[A]) =
		DisplayFunction[A, Any](transform){ (s, _) => s.localizationSkipped }
	
	/**
	  * This display function transforms an item into string format and then localizes it using a localizer (implicit)
	  * @param transform A transform function for transforming the item to string format
	  * @param localizer A localizer for localization (implicit)
	  * @tparam A Source item type
	  * @tparam Meta Localization context type
	  * @return A new display function
	  */
	def localized[A, Meta](transform: Transform[A])(implicit localizer: Localizer[Meta]) =
		DisplayFunction[A, Meta](transform)(localizer.localize)
	
	/**
	  * This display function uses toString to convert an item to string and then localizes that string using a
	  * localizer (implicit)
	  * @param localizer A localizer that handles localization (implicit)
	  * @tparam Meta Localization context type
	  * @return A new display function
	  */
	def localized[Meta]()(implicit localizer: Localizer[Meta]) = localizeOnly[Meta](localizer.localize)
}

/**
  * These functions are used for converting data into localized text format
  * @author Mikko Hilpinen
  * @since 22.4.2019, v1+
  */
class DisplayFunction[-A, -Meta](f: (A, Option[Meta]) => LocalizedString)
{
	// OPERATORS	-------------------
	
	/**
	  * Displays an item
	  * @param item The source item
	  * @param context Localization context, None if no contextual information is available
	  * @return A localized display version for the item
	  */
	def apply(item: A, context: Option[Meta] = None) = f(item, context)
	
	/**
	  * Displays an item
	  * @param item The source item
	  * @param context Localization context
	  * @return A localized display version for the item
	  */
	def apply(item: A, context: Meta): LocalizedString = apply(item, Some(context))
}
