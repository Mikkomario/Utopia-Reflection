package utopia.reflection.component.input

/**
  * This component allows two-way interaction with the users while generating events for value changes
  * @author Mikko Hilpinen
  * @since 22.4.2019, v1+
  */
@deprecated("Replaced with a new approach that uses pointers", "v1+")
trait InteractionWithEvents[A] extends Interaction[A] with InputWithEvents[A]
{
	// ABSTRACT	----------------
	
	/**
	  * Specifies the input value in this interaction. Will not generate any events.
	  * @param newValue A new input value for this interaction.
	  */
	def setValueNoEvents(newValue: A): Unit
	
	
	// IMPLEMENTED	------------
	
	/**
	  * Updates the value of this interaction element
	  * @param newValue New (input) value for this interaction
	  */
	override def value_=(newValue: A) =
	{
		if (newValue != value)
		{
			setValueNoEvents(newValue)
			informListeners(value)
		}
	}
	
	
	// OTHER	----------------
	
	/**
	  * Specifies input value for this interaction
	  * @param newValue The new value
	  * @param generateEvents Whether input events should be generated (default = true)
	  */
	def setValue(newValue: A, generateEvents: Boolean = true) = if (generateEvents) value = newValue else setValueNoEvents(newValue)
}
