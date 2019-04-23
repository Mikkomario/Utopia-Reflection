package utopia.reflection.component

/**
  * Pools are used for presenting one or more items
  * @author Mikko Hilpinen
  * @since 23.4.2019, v1+
  * @tparam A The type of content in this pool
  */
trait Pool[+A]
{
	/**
	  * @return The contents of this pool
	  */
	def content: A
}
