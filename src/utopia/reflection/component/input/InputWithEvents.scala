package utopia.reflection.component.input

import scala.concurrent.{ExecutionContext, Future}

/**
  * This input source generates events whenever the input changes
  * @author Mikko Hilpinen
  * @since 23.4.2019, v1+
  * @tparam A The type of input
  */
@deprecated("Replaced with a new approach that uses pointers", "v1+")
trait InputWithEvents[A] extends Input[A] with Listenable[A]
{
	/**
	  * Informs the listeners registered on this component about this input's current state
	  */
	def informListeners(): Unit = informListeners(value)
	
	/**
	  * Informs the listeners registered on this component about this input's current state. This is done asynchronously.
	  * @param context Asynchronous execution context (implicit)
	  * @return A future of the completion of this operation
	  */
	def informListenersAsync()(implicit context: ExecutionContext): Future[Unit] = informListenersAsync(value)
}
