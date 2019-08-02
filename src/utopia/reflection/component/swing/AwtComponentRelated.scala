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
	
	
	// COMPUTED	--------------------
	
	/**
	  * @return The lowest window parent of this component. None if this component isn't hosted in any window.
	  */
	def parentWindow =
	{
		var nextParent = component.getParent
		var window: Option[java.awt.Window] = None
		
		// Checks parents until a window is found
		while (window.isEmpty && nextParent != null)
		{
			nextParent match
			{
				case w: java.awt.Window => window = Some(w)
				case _ => nextParent = nextParent.getParent
			}
		}
		
		window
	}
	
	
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
