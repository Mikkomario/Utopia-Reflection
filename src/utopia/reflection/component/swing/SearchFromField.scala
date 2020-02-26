package utopia.reflection.component.swing

import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.genesis.color.Color
import utopia.genesis.handling.mutable.ActorHandler
import utopia.genesis.shape.shape2D.Point
import utopia.reflection.component.Refreshable
import utopia.reflection.component.drawing.CustomDrawer
import utopia.reflection.component.input.SelectableWithPointers
import utopia.reflection.container.stack.StackLayout
import utopia.reflection.container.stack.StackLayout.Fit
import utopia.reflection.container.swing.Stack
import utopia.reflection.container.swing.Stack.AwtStackable
import utopia.reflection.container.swing.window.{Popup, Window}
import utopia.reflection.controller.data.StackSelectionManager
import utopia.reflection.localization.LocalizedString
import utopia.reflection.shape.StackLength
import utopia.reflection.text.{Font, Prompt}

import scala.concurrent.ExecutionContext

/**
  * A custom drop down selection component with a search / filter function
  * @author Mikko Hilpinen
  * @since 26.2.2020, v1
  */
class SearchFromField[A, C <: AwtStackable with Refreshable[A]]
(actorHandler: ActorHandler, selectionPrompt: LocalizedString, defaultWidth: StackLength, vMargin: StackLength,
 selectionDrawer: CustomDrawer, font: Font, textColor: Color = Color.textBlack,
 promptTextColor: Color = Color.textBlackDisabled, betweenDisplaysMargin: StackLength = StackLength.fixed(0),
 displayStackLayout: StackLayout = Fit, maxNumberOfDisplaysShown: Option[Int] = None,
 val contentPointer: PointerWithEvents[Vector[A]] = new PointerWithEvents(Vector()),
 checkEquals: (A, A) => Boolean = (a: A, b: A) => a == b)(makeDisplay: A => C)(implicit exc: ExecutionContext)
	extends StackableAwtComponentWrapperWrapper with SelectableWithPointers[Option[A], Vector[A]]
{
	// ATTRIBUTES	----------------------------
	
	private val searchField = new TextField(defaultWidth, vMargin, font,
		prompt = Some(Prompt(selectionPrompt, font, promptTextColor)), textColor = textColor)
	private val searchStack = Stack.column[C](margin = betweenDisplaysMargin, layout = displayStackLayout)
	private val displaysManager = new StackSelectionManager[A, C](searchStack, selectionDrawer, checkEquals)(makeDisplay)
	
	// TODO: Consider making this a volatile option
	private var visiblePopup: Option[Window[_]] = None
	private var isSelected = false
	
	
	// INITIAL CODE	-----------------------------
	
	// When the field gains focus, displays the pop-up window (if not yet displayed)
	searchField.addFocusGainedListener { displayPopup() }
	
	// When content updates, changes selection options and updates field size
	addContentListener { e =>
		val displayedItems = maxNumberOfDisplaysShown.map(e.newValue.take).getOrElse(e.newValue)
		displaysManager.content = displayedItems
		searchField.targetWidth = if (content.isEmpty) defaultWidth else searchStack.stackSize.width
	}
	
	// When selection changes in pop-up, updates text field contents
	addValueListener { e =>
		e.newValue match
		{
			case Some(newSelected) => ??? // TODO: Change text field content & status
			case None => ??? // TODO: Change status & re-enable filtering
		}
	}
	
	
	// IMPLEMENTED	----------------------------
	
	override def valuePointer = displaysManager.valuePointer
	
	override protected def wrapped = searchField
	
	
	// OTHER	--------------------------------
	
	private def displayPopup() =
	{
		if (visiblePopup.isEmpty && content.nonEmpty)
		{
			// Creates and displays the popup
			val popup = Popup(searchField, searchStack, actorHandler, (fieldSize, _) => Point(0, fieldSize.height))
			visiblePopup = Some(popup)
			popup.closeFuture.foreach { _ => visiblePopup = None }
		}
	}
}
