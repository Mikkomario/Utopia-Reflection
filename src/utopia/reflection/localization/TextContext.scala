package utopia.reflection.localization

object TextContext
{
	/**
	  * This context is used when a (simple) value is displayed
	  */
	case object Value extends TextContext
	/**
	  * This context is used when presenting descriptive text
	  */
	case object Description extends TextContext
	/**
	  * This context is used for field names / labels
	  */
	case object FieldName extends TextContext
	/**
	  * This context is used for interactive elements like buttons etc.
	  */
	case object Button extends TextContext
	/**
	  * This context is used for multiple selection tools like drop downs etc.
	  */
	case object Selection extends TextContext
	/**
	  * This context is used for headers and titles
	  */
	case object Header extends TextContext
}

/**
  * Text contexts represent different situations and purposes when text is displayed
  * @author Mikko Hilpinen
  * @since 22.4.2019, v1+
  */
trait TextContext extends Equals
