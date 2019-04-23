package utopia.reflection.component

/**
  * Refreshable components are pools that can be refreshed from the program side
  * @author Mikko Hilpinen
  * @since 23.4.2019, v1+
  * @tparam A The type of content in this pool
  */
trait Refreshable[A] extends Pool[A]
{
	/**
	  * Updates the contents of this pool
	  * @param newContent New contents
	  */
	def content_=(newContent: A): Unit
}
