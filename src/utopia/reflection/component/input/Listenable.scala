package utopia.reflection.component.input

import scala.concurrent.{ExecutionContext, Future}

/**
  * Listenable items can have changes in input state, which can be listened
  * @author Mikko Hilpinen
  * @since 22.4.2019, v1+
  */
@deprecated("Replaced with a new approach that uses pointers", "v1+")
trait Listenable[A]
{
	// ATTRIBUTES	------------------
	
	private var _listeners = Vector[InputListener[A]]()
	
	
	// COMPUTED	----------------------
	
	/**
	  * @return The currently registered listeners for this component
	  */
	def listeners = _listeners
	
	
	// OTHER	----------------------
	
	/**
	  * Registers a new listener
	  * @param l A new listener
	  */
	def addListener(l: InputListener[A]) = _listeners :+= l
	
	/**
	  * Unregisters a listener
	  * @param l A listener
	  */
	def removeListener(l: Any) = _listeners = _listeners.filterNot { _ == l }
	
	/**
	  * Unregisters all listeners from this component
	  */
	def clearListeners() = _listeners = Vector()
	
	/**
	  * Informs all listeners of a value
	  * @param value A value that will be shown to all listeners
	  */
	def informListeners(value: A) = listeners.foreach { _.inputChanged(value) }
	
	/**
	  * Informs all listeners of a value. The informing will be done asynchronously.
	  * @param value A value that will be shown to all listeners
	  * @param context An asynchronous execution context (implicit)
	  * @return A future of the completion of this operation
	  */
	def informListenersAsync(value: A)(implicit context: ExecutionContext) = Future { informListeners(value) }
}
