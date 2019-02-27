package utopia.reflection.shape

import utopia.flow.util.Equatable

object StackLength
{
    // ATTRIBUTES    ---------------
    
    val any = new StackLength(0, 0, None, true)
    
    
    // CONSTRUCTORS    -------------
    
    def apply(min: Int, optimal: Int, max: Option[Int] = None, 
            lowPriority: Boolean = false) = new StackLength(min, optimal, max, lowPriority);
    
    def apply(min: Int, optimal: Int, max: Int) = new StackLength(min, optimal, Some(max))
    
    def fixed(l: Int) = apply(l, l, l)
    
    def any(optimal: Int) = apply(0, optimal)
    
    def upscaling(min: Int, optimal: Int) = apply(min, optimal)
    
    def upscaling(optimal: Int): StackLength = upscaling(optimal, optimal)
    
    def downscaling(optimal: Int, max: Int) = apply(0, optimal, max)
    
    def downscaling(max: Int): StackLength = downscaling(max, max)
    
    
    // OTHER    --------------------
    
    def min(a: StackLength, b: StackLength) = StackLength(a.min min b.min, a.optimal min b.optimal, 
            Vector(a.max, b.max).flatten.reduceOption(_ min _), a.lowPriority || b.lowPriority);
    
    def max(a: StackLength, b: StackLength) = 
    {
        val max = if (a.max.isEmpty || b.max.isEmpty) None else Some(a.max.get max b.max.get)
        
        StackLength(a.min max b.min, a.optimal max b.optimal, max, a.lowPriority || b.lowPriority)
    }
}

/**
* This class represents a varying length with a minimum, optimal and a possible maximum
* @author Mikko Hilpinen
* @since 25.2.2019
**/
class StackLength(rawMin: Int, rawOptimal: Int, rawMax: Option[Int] = None, 
        val lowPriority: Boolean = false) extends Equatable
{
    // ATTRIBUTES    ------------------------
    
    val min = rawMin max 0
    
    // Optimal must be >= min
	val optimal = rawOptimal max min
	
	// Max must be >= optimal
	val max = rawMax.map(_ max optimal)
	
	
	// IMPLEMENTED    -----------------------
	
	def properties = Vector(min, optimal, max, lowPriority)
	
	override def toString() = 
	{
	    val s = new StringBuilder()
	    s append min
	    s append " < "
	    s append optimal
	    
	    max foreach {m => s append s" < $m"}
	    
	    if (lowPriority)
	        s append " (low prio)";
	    
	    s.toString()
	}
	
	
	// OPERATORS    ------------------------
	
	def +(length: Int) = StackLength(min + length, optimal + length, max.map(_ + length), lowPriority)
	
	def +(other: StackLength) = 
	{
	    val newMax = if (max.isDefined && other.max.isDefined) Some(max.get max other.max.get) else None
	    StackLength(min + other.min, optimal + other.optimal, newMax, lowPriority || other.lowPriority)
	}
	
	def -(length: Int) = +(-length)
	
	def *(multi: Double) = StackLength((min * multi).toInt, (optimal * multi).toInt, 
	        max.map(m => (m * multi).toInt), lowPriority);
	
	def /(div: Double) = *(1/div)
	
	
	// OTHER    ---------------------------
	
	def withMin(newMin: Int) = StackLength(newMin, optimal, max, lowPriority)
	
	def withOptimal(newOptimal: Int) = StackLength(min, newOptimal, max, lowPriority)
	
	def withMax(newMax: Option[Int]) = StackLength(min, optimal, newMax, lowPriority)
	
	def withMax(newMax: Int): StackLength = withMax(Some(newMax))
	
	def withPriority(isLowPriority: Boolean) = StackLength(min, optimal, max, isLowPriority)
	
	def withLowPriority = withPriority(false)
	
	def withNoLimits = StackLength(0, optimal, None, lowPriority)
	
	def upscaling = withMax(None)
	
	def downscaling = withMin(0)
	
	/**
	 * Combines two stack sizes to one which supports both limits
	 */
	def combine(other: StackLength) = 
	{
	    val newMin = min max other.min
	    val newMax = Vector(max, other.max).flatten.reduceOption(_ min _)
	    val newOptimal = optimal max other.optimal
	    val prio = lowPriority || other.lowPriority
	    
	    // Optimal is limited by maximum length
	    if (newMax exists { _ < newOptimal })
	        StackLength(newMin, newMax.get, newMax, prio)
	    else
	        StackLength(newMin, newOptimal, newMax, prio)
	}
	
	def mapMin(map: Int => Int) = withMin(map(min))
	
	def mapOptimal(map: Int => Int) = withOptimal(map(optimal))
	
	def mapMax(map: Option[Int] => Option[Int]) = withMax(map(max))
}