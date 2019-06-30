package utopia.reflection.controller.data

import java.awt.event.KeyEvent

import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.flow.event.{ChangeEvent, ChangeListener}
import utopia.flow.util.CollectionExtensions._
import utopia.reflection.component.input.SelectableWithPointers
import utopia.reflection.component.Refreshable

/**
  * This manager handles displayed content AND selection
  * @author Mikko Hilpinen
  * @since 22.5.2019, v1+
  */
trait SelectionManager[A, C <: Refreshable[A]] extends ContentManager[A, C] with SelectableWithPointers[Option[A], Vector[A]]
{
	// ATTRIBUTES	-------------------
	
	private var _selectedDisplay: Option[C] = None
	
	override val valuePointer = new PointerWithEvents[Option[A]](None)
	
	
	// ABSTRACT	-----------------------
	
	/**
	  * Updates how selection is displayed
	  * @param oldSelected The old selected item (None if no item was selected before)
	  * @param newSelected The new selected item (None if no item is selected anymore)
	  */
	protected def updateSelectionDisplay(oldSelected: Option[C], newSelected: Option[C]): Unit
	
	
	// COMPUTED	-----------------------
	
	/**
	  * @return The currently selected display. None if no item is currently selected
	  */
	def selectedDisplay = _selectedDisplay
	
	
	// OTHER	-------------------
	
	/**
	  * Handles mouse click event
	  * @param displayAtMousePosition The display closest to / under the mouse cursor
	  */
	protected def handleMouseClick(displayAtMousePosition: C) = selectDisplay(displayAtMousePosition)
	
	/**
	  * Handles key pressed event
	  * @param keyCode The key code that was pressed (only up and down arrow keys count)
	  */
	protected def handleKeyPress(keyCode: Int) =
	{
		val displays = this.displays
		
		if (displays.nonEmpty)
		{
			val transition = if (keyCode == KeyEvent.VK_UP) - 1 else if (keyCode == KeyEvent.VK_DOWN) 1 else 0
			
			if (transition != 0)
			{
				val oldIndex = _selectedDisplay.flatMap { displays.optionIndexOf(_) }
				
				if (oldIndex.isDefined)
				{
					// Moves the selection by one
					val newIndex = (oldIndex.get + transition) % displays.size
					
					if (newIndex < 0)
						selectDisplay(displays(newIndex + displays.size))
					else
						selectDisplay(displays(newIndex))
				}
				// If no item was selected previously, either selects the first or last item
				else if (transition > 0)
					selectDisplay(displays.head)
				else
					selectDisplay(displays.last)
			}
		}
	}
	
	private def updateSelection(newValue: Option[A]): Unit =
	{
		val oldSelected = _selectedDisplay
		_selectedDisplay = newValue.flatMap { v => displays.find { _.content == v } }
		
		if (oldSelected != _selectedDisplay)
			updateSelectionDisplay(oldSelected, _selectedDisplay)
	}
	
	private def selectDisplay(display: C) =
	{
		if (!_selectedDisplay.contains(display))
			value = Some(display.content)
	}
	
	
	// NESTED CLASSES	---------------------
	
	private class ContentUpdateSelectionHandler extends ChangeListener[Vector[A]]
	{
		override def onChangeEvent(event: ChangeEvent[Vector[A]]) =
		{
			// Tries to preserve selection after refresh
			if (value.isDefined)
			{
				if (event.newValue.contains(value.get))
					updateSelection(value)
				else
					value = None
			}
		}
	}
	
	private class ValueUpdateListener extends ChangeListener[Option[A]]
	{
		override def onChangeEvent(event: ChangeEvent[Option[A]]) = updateSelection(event.newValue)
	}
}
