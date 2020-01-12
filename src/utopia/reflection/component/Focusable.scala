package utopia.reflection.component

/**
 * A common trait for components that an request and have focus
 * @author Mikko Hilpinen
 * @since 12.1.2020, v1
 */
trait Focusable
{
	/**
	 * @return Whether this component is currently in focus
	 */
	def isInFocus: Boolean
	
	/**
	 * Requests focus for this component
	 * @return Whether this component is able to gain focus (false means that this component will not receive focus
	 *         while true means that it may receive focus)
	 */
	def requestFocus(): Boolean
}
