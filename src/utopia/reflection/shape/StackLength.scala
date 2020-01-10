package utopia.reflection.shape

import utopia.flow.util.Equatable

object StackLength
{
    // ATTRIBUTES    ---------------
	
	/**
	  * A stack length that allows any value (from 0 and up, preferring 0)
	  */
	val any = new StackLength(0, 0, None)
    
    
    // CONSTRUCTORS    -------------
	
	/**
	  * @param min Minimum length
	  * @param optimal Optimal length
	  * @param max Maximum length. None if not limited. Default = None
	  * @param lowPriority Whether this length should be treated as a low priority constraint. Default = false.
	  * @return A new stack length
	  */
    def apply(min: Double, optimal: Double, max: Option[Double] = None,
            lowPriority: Boolean = false) = new StackLength(min, optimal, max, lowPriority)
	
	/**
	  * @param min Minimum length
	  * @param optimal Optimal length
	  * @param max Maximum length
	  * @return A new stack length
	  */
    def apply(min: Double, optimal: Double, max: Double) = new StackLength(min, optimal, Some(max))
	
	/**
	  * @param l Fixed length
	  * @return A new stack length with min, optimal and max set to specified single value (no variance)
	  */
    def fixed(l: Double) = apply(l, l, l)
	
	/**
	  * @param optimal Optimal length
	  * @return A stack length with no minimum or maximum, preferring specified value
	  */
    def any(optimal: Double) = apply(0, optimal)
	
	/**
	  * @param min Minimum length
	  * @param optimal Optimal length
	  * @return A stack length with no maximum
	  */
    def upscaling(min: Double, optimal: Double) = apply(min, optimal)
	
	/**
	  * @param optimal Minimum & Optimal length
	  * @return A stack length with no maximum, preferring the minimum value
	  */
    def upscaling(optimal: Double): StackLength = upscaling(optimal, optimal)
	
	/**
	  * @param optimal Optimal length
	  * @param max Maximum length
	  * @return A stack length with no minimum
	  */
    def downscaling(optimal: Double, max: Double) = apply(0, optimal, max)
	
	/**
	  * @param max Maximum length
	  * @return A stack length with no miminum, preferring the maximum length
	  */
    def downscaling(max: Double): StackLength = downscaling(max, max)
}

/**
* This class represents a varying length with a minimum, optimal and a possible maximum
* @author Mikko Hilpinen
* @since 25.2.2019
  * @param rawMin Minimum length
  * @param rawOptimal Optimum length
  * @param rawMax Maximum length. None if not limited. Defaults to None.
  * @param isLowPriority Whether this length should be treated as a low priority constraint. Default = false.
**/
class StackLength(rawMin: Double, rawOptimal: Double, rawMax: Option[Double] = None, val isLowPriority: Boolean = false) extends Equatable
{
    // ATTRIBUTES    ------------------------
	
	/**
	  * Minimum length
	  */
	val min: Double = rawMin max 0
	
	/**
	  * Optimal / preferred length
	  */
	// Optimal must be >= min
	val optimal: Double = rawOptimal max min
	
	/**
	  * Maximum length. None if not limited.
	  */
	// Max must be >= optimal
	val max: Option[Double] = rawMax.map(_ max optimal)
	
	
	// COMPUTED	-----------------------------
	
	/**
	 * @return Whether this length has a maximum limit
	 */
	def hasMax = max.isDefined
	
	/**
	  * @return A version of this stack length that has low priority
	  */
	def withLowPriority = withPriority(true)
	
	/**
	  * @return A version of this stack length that has no mimimum (set to 0)
	  */
	def noMin = withMin(0)
	
	/**
	  * @return A version of this stack length that has no maximum
	  */
	def noMax = withMax(None)
	
	/**
	  * @return A version of this stack length with no mimum or maximum
	  */
	def noLimits = StackLength(0, optimal, None, isLowPriority)
	
	/**
	  * @return A version of this stack length with no maximum value (same as noMax)
	  */
	def upscaling = noMax
	
	/**
	  * @return A version of this stack length with no mimimum value (same as noMin)
	  */
	def downscaling = noMin
	
	/**
	  * @return A stacksize that uses this length as both widht and height
	  */
	def square = StackSize(this, this)
	
	
	// IMPLEMENTED    -----------------------
	
	def properties = Vector(min, optimal, max, isLowPriority)
	
	override def toString =
	{
	    val s = new StringBuilder()
	    s append min.toInt
	    s append "-"
	    s append optimal.toInt
		s append "-"
	    
	    max foreach { m => s.append(m.toInt) }
	    
	    if (isLowPriority)
	        s append " (low prio)"
	    
	    s.toString()
	}
	
	
	// OPERATORS    ------------------------
	
	/**
	  * @param length Increase in length
	  * @return An increased version of this stack length (min, optimal and max adjusted, if present)
	  */
	def +(length: Double) = StackLength(min + length, optimal + length, max.map(_ + length), isLowPriority)
	
	/**
	  * @param other Another stack length
	  * @return A combination of these stack sizes where minimum, optimal and maximum (if present) values are increased
	  */
	def +(other: StackLength) =
	{
	    val newMax = if (max.isDefined && other.max.isDefined) Some(max.get + other.max.get) else None
	    StackLength(min + other.min, optimal + other.optimal, newMax, isLowPriority || other.isLowPriority)
	}
	
	/**
	  * @param length A decrease in length
	  * @return A decreased version of this stack length (min, optimal and max adjusted, if present). Minimum won't go below 0
	  */
	def -(length: Double) = +(-length)
	
	/**
	  * @param multi A multiplier
	  * @return A multiplied version of this length where min, optimal and max lengths are all affected, if present
	  */
	def *(multi: Double) = StackLength(min * multi, optimal * multi, max.map { _ * multi }, isLowPriority)
	
	/**
	  * @param div A divider
	  * @return A divided version of this length where min, optimal and max lengths are all affected, if present
	  */
	def /(div: Double) = *(1/div)
	
	/**
	  * Combines this stack length with another in order to create a stack size
	  * @param vertical The vertical stack length component
	  * @return A stack size with this length as width and 'vertical' as height
	  */
	def x(vertical: StackLength) = StackSize(this, vertical)
	
	
	// OTHER    ---------------------------
	
	/**
	  * @param newMin A new minimum value
	  * @return An updated version of this length with specified minimum value (optimal and max may also be adjusted if necessary)
	  */
	def withMin(newMin: Double) = StackLength(newMin, optimal, max, isLowPriority)
	
	/**
	  * @param newOptimal A new optimal value
	  * @return An updated version of this length with specified optimum value (maximum may also be adjusted if necessary)
	  */
	def withOptimal(newOptimal: Double) = StackLength(min, newOptimal, max, isLowPriority)
	
	/**
	  * @param newMax A new maximum value (None if no maximum)
	  * @return An updated version of this length with specified maximum length
	  */
	def withMax(newMax: Option[Double]) = StackLength(min, optimal, newMax, isLowPriority)
	
	/**
	  * @param newMax A new maximum value
	  * @return An updated version of this length with specified maximum length
	  */
	def withMax(newMax: Double): StackLength = withMax(Some(newMax))
	
	/**
	  * @param isLowPriority Whether the new length should be considered a low priority constraint
	  * @return A copy of this stack length with specified priority status
	  */
	def withPriority(isLowPriority: Boolean) = if (this.isLowPriority == isLowPriority) this else
		StackLength(min, optimal, max, isLowPriority)
	
	/**
	  * @param other Another stack length
	  * @return A minimum between this length and the other (min, optimal and max will be picked from the minimum value)
	  */
	def min(other: StackLength): StackLength = StackLength(min min other.min, optimal min other.optimal,
		Vector(max, other.max).flatten.reduceOption(_ min _), isLowPriority || other.isLowPriority)
	
	/**
	  * @param other Another stack length
	  * @return A maximum between this length and the other (min, optimal and max will be picked from the maximum value)
	  */
	def max(other: StackLength): StackLength =
	{
		val newMax = if (max.isEmpty || other.max.isEmpty) None else Some(max.get max other.max.get)
		StackLength(min max other.min, optimal max other.optimal, newMax, isLowPriority || other.isLowPriority)
	}
	
	/**
	 * Combines two stack sizes to one which supports both limits
	 */
	def combineWith(other: StackLength) =
	{
	    val newMin = min max other.min
	    val newMax = Vector(max, other.max).flatten.reduceOption(_ min _)
	    val newOptimal = optimal max other.optimal
	    val prio = isLowPriority || other.isLowPriority
	    
	    // Optimal is limited by maximum length
	    if (newMax exists { _ < newOptimal })
	        StackLength(newMin, newMax.get, newMax, prio)
	    else
	        StackLength(newMin, newOptimal, newMax, prio)
	}
	
	/**
	  * Creates a new stack length that is within the specified limits
	  * @param minimum Minimum limit
	  * @param maximum Maximum limit. None if not limited.
	  * @return A stack length that has at least 'minimum' minimum width and at most 'maximum' maximum width
	  */
	def within(minimum: Double, maximum: Option[Double]) =
	{
		if (maximum.isDefined)
		{
			val newMin = minimum max min min maximum.get
			val newMax = max.map { m => minimum max m min maximum.get } getOrElse maximum.get
			val newOptimal = newMin max optimal min newMax
			
			StackLength(newMin, newOptimal, Some(newMax), isLowPriority)
		}
		else
		{
			val newMin = minimum max min
			val newMax = max.map { minimum max _ }
			val newOptimal = newMin max optimal
			
			StackLength(newMin, newOptimal, newMax, isLowPriority)
		}
	}
	
	/**
	  * Creates a new stack length that is within the specified limits
	  * @param limits The limits applied to this length
	  * @return A stack length with limited min, max and optimal value
	  */
	def within(limits: StackLengthLimit) =
	{
		// Minimum size is limited both from minimum and maximum side (cannot be larger than specified max or max optimal)
		val newMin = min max limits.min
		val minUnderMax = limits.maxOptimal.orElse(limits.max).map { newMin min _ } getOrElse newMin
		
		val newMax =
		{
			if (max.isEmpty)
				limits.max
			else if (limits.max.isEmpty)
				max
			else
				Some(max.get min limits.max.get)
		}
		val newOptimal =
		{
			val minLimited = optimal max (limits.minOptimal getOrElse minUnderMax)
			(limits.maxOptimal orElse limits.max).map { minLimited min _ } getOrElse minLimited
		}
		
		StackLength(minUnderMax, newOptimal, newMax, isLowPriority)
	}
	
	/**
	  * @param map A mapping function
	  * @return A new length with mapped min
	  */
	def mapMin(map: Double => Double) = withMin(map(min))
	
	/**
	  * @param map A mapping function
	  * @return A new length with mapped optimal
	  */
	def mapOptimal(map: Double => Double) = withOptimal(map(optimal))
	
	/**
	  * @param map A mapping function
	  * @return A new length with mapped maximum value
	  */
	def mapMax(map: Option[Double] => Option[Double]) = withMax(map(max))
}