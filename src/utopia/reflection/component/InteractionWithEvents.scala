package utopia.reflection.component

/**
  * This component allows two-way interaction with the users while generating events for value changes
  * @author Mikko Hilpinen
  * @since 22.4.2019, v1+
  */
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
			informListeners(newValue)
		}
	}
}
