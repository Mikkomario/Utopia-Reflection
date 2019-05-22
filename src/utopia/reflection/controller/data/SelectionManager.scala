package utopia.reflection.controller.data

import java.awt.event.KeyEvent

import utopia.flow.util.CollectionExtensions._
import utopia.reflection.component.input.Selectable
import utopia.reflection.component.{Refreshable, SelectionDisplay}

/**
  * This manager handles displayed content AND selection
  * @author Mikko Hilpinen
  * @since 22.5.2019, v1+
  */
trait SelectionManager[A, C <: Refreshable[A] with SelectionDisplay] extends ContentManager[A, C]
	with Selectable[Option[A], Vector[A]]
{
	// ATTRIBUTES	-------------------
	
	private var selectedDisplay: Option[C] = None
	
	
	// IMPLEMENTED	-------------------
	
	override def content_=(newContent: Vector[A]) =
	{
		// Tries to preserve selection during refresh
		val oldSelected = selected
		super.content_=(newContent)
		
		if (oldSelected.isDefined)
		{
			setValueNoEvents(oldSelected)
			
			// Informs listeners if selection was lost
			val newSelected = selected
			if (newSelected.isEmpty)
				informListeners(newSelected)
		}
	}
	
	override def setValueNoEvents(newValue: Option[A]) =
	{
		// Will not select the item again
		if (selectedDisplay.forall { d => !newValue.contains(d.content)} )
		{
			selectedDisplay.foreach { _.isSelected = false }
			if (newValue.isDefined)
			{
				selectedDisplay = displays.find { _.content == newValue.get }
				selectedDisplay.foreach { _.isSelected = true }
			}
			else
				selectedDisplay = None
		}
	}
	
	override def value = selectedDisplay.map { _.content }
	
	
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
				val oldIndex = selectedDisplay.flatMap { displays.optionIndexOf(_) }
				
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
	
	private def selectDisplay(display: C) =
	{
		if (!selectedDisplay.contains(display))
		{
			selectedDisplay.foreach { _.isSelected = false }
			selectedDisplay = Some(display)
			display.isSelected = true
		}
	}
}
