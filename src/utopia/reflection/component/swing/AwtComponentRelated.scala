package utopia.reflection.component.swing

/**
  * This trait is extended by classes that have a related awt component
  * @author Mikko Hilpinen
  * @since 28.4.2019, v1+
  */
trait AwtComponentRelated
{
	/**
	  * @return The awt component associated with this instance
	  */
	def component: java.awt.Component
}
