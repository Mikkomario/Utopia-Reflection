package utopia.reflection.localization

/**
  * This trait handles commonalities between different localization strings
  * @author Mikko Hilpinen
  * @since 22.4.2019, v1+
  */
trait LocalStringLike[Repr <: LocalStringLike[Repr]]
{
	// ABSTRACT	--------------
	
	/**
	  * @return A string representation of this string-like instance
	  */
	def string: String
	
	/**
	  * @return The ISO code of this string representation's language
	  */
	def languageCode: String
	
	/**
	  * Splits this string based on provided regex
	  * @param regex The part which splits this string
	  * @return Splitted parts
	  */
	def split(regex: String): Vector[Repr]
	
	
	// COMPUTED	--------------
	
	/**
	  * @return This string split on newline characters
	  */
	def lines = split("\r?\n|\r")
	
	
	// IMPLEMENTED	----------
	
	override def toString = string
}
