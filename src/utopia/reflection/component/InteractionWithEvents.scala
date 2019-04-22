package utopia.reflection.component

import scala.concurrent.{ExecutionContext, Future}

/**
  * This component allows two-way interaction with the users while generating events for value changes
  * @author Mikko Hilpinen
  * @since 22.4.2019, v1+
  */
trait InteractionWithEvents[A] extends Interaction[A] with Listenable[A]
{
	// ABSTRACT	----------------
	
	/**
	  * Specifies the input value in this interaction. Will not generate any events.
	  * @param newValue A new input value for this interaction.
	  */
	def setInputNoEvents(newValue: A): Unit
	
	
	// IMPLEMENTED	------------
	
	/**
	  * Updates the value of this interaction element
	  * @param newValue New (input) value for this interaction
	  */
	override def input_=(newValue: A) =
	{
		if (newValue != input)
		{
			setInputNoEvents(newValue)
			informListeners(newValue)
		}
	}
	
	
	// OTHERS	----------------
	
	/**
	  * Informs the listeners registered on this component about this input's current state
	  */
	def informListeners(): Unit = informListeners(input)
	
	/**
	  * Informs the listeners registered on this component about this input's current state. This is done asynchronously.
	  * @param context Asynchronous execution context (implicit)
	  * @return A future of the completion of this operation
	  */
	def informListenersAsync()(implicit context: ExecutionContext): Future[Unit] = informListenersAsync(input)
}
