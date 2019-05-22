package utopia.reflection.component

/**
  * Selection displays specifically display a selected state
  * @author Mikko Hilpinen
  * @since 22.5.2019, v1+
  */
trait SelectionDisplay
{
	/**
	  * @return The current selection state of this display
	  */
	def isSelected: Boolean
	/**
	  * Updates the selection state of this display
	  * @param newStatus The new selection state
	  */
	def isSelected_=(newStatus: Boolean): Unit
}
