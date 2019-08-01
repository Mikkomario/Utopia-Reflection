package utopia.reflection.component.swing

import java.awt.Cursor

/**
  * This trait is extended by classes that have a related awt component
  * @author Mikko Hilpinen
  * @since 28.4.2019, v1+
  */
trait AwtComponentRelated
{
	// ABSTRACT	--------------------
	
	/**
	  * @return The awt component associated with this instance
	  */
	def component: java.awt.Component
	
	
	// OTHER	--------------------
	
	/**
	  * Specifies that the mouse should have a hand cursor when hovering over this component
	  */
	def setHandCursor() = component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
	
	/**
	  * Specifies that the mouse should have the default cursor when hovering over this component
	  */
	def setArrowCursor() = component.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))
}
