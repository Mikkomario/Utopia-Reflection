package utopia.reflection.component.input

import utopia.reflection.component.Pool

/**
  * Selection is an input that has a base pool of value(s), from which some are selected
  * @author Mikko Hilpinen
  * @since 23.4.2019, v1+
  * @tparam S the type of selection
  * @tparam P The type of selection pool
  */
trait Selection[S, +P] extends InputWithEvents[S] with Pool[P]
{
	// COMPUTED	-----------------
	
	/**
	  * @return The currently selected value (same as value)
	  */
	def selected = value
}
