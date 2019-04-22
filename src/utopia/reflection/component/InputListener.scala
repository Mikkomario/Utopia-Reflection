package utopia.reflection.component

import scala.language.implicitConversions

object InputListener
{
	/**
	  * Converts a function into an input listener (implicit)
	  * @param f A function
	  * @tparam A Accepted input type
	  * @return A new input listener that uses the specified function to handle input changes
	  */
	implicit def functionToListener[A](f: A => Unit): InputListener[A] = new FunctionalInputListener[A](f)
}

/**
  * These listeners are interested in changes in component input
  * @author Mikko Hilpinen
  * @since 22.4.2019, v1+
  */
trait InputListener[-A]
{
	/**
	  * This method will be called when component input is updated
	  * @param newInput The new component input
	  */
	def inputChanged(newInput: A): Unit
}

private class FunctionalInputListener[-A](f: A => Unit) extends InputListener[A]
{
	override def inputChanged(newInput: A) = f(newInput)
}