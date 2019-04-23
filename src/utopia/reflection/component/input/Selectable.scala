package utopia.reflection.component.input

/**
  * Selectable components are selections that can be interacted with from the program side
  * @author Mikko Hilpinen
  * @since 23.4.2019, v1+
  */
trait Selectable[S, P] extends Selection[S, P] with InteractionWithEvents[S]
{
	// COMPUTED	------------------
	
	/**
	  * Updates the currently selected value, also generating events (same as calling value = ...)
	  * @param newValue The new value for this selection
	  */
	def selected_=(newValue: S) = value = newValue
	
	
	// OTHER	------------------
	
	/**
	  * Updates the currently selected value without generating events (same as calling setValueNoEvents(...))
	  * @param newValue The new value for this selection
	  */
	def selectNoEvents(newValue: S) = setValueNoEvents(newValue)
}
