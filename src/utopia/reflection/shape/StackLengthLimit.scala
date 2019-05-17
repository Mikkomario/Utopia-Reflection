package utopia.reflection.shape

object StackLengthLimit
{
	/**
	  * A stack length limit that doesn't actually limit stack lengths
	  */
	val noLimit = StackLengthLimit()
}

/**
  * These classes are used for limiting stack lengths to certain bounds
  * @author Mikko Hilpinen
  * @since 15.5.2019, v1+
  * @param min The absolute minimum length (default = 0)
  * @param minOptimal The minimum value allowed for optimal length. None if not limited. (default = None)
  * @param maxOptimal The maximum value allowed for optimal length. None if not limited. (default = None)
  * @param max The absolute maximum length. None if not limited. (default = None)
  */
case class StackLengthLimit(min: Int = 0, minOptimal: Option[Int] = None, maxOptimal: Option[Int] = None,
							max: Option[Int] = None)
