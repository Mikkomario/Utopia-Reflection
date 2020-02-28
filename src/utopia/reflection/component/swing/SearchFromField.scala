package utopia.reflection.component.swing

import java.awt.event.KeyEvent

import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.genesis.color.Color
import utopia.genesis.handling.mutable.ActorHandler
import utopia.genesis.shape.shape2D.Point
import utopia.reflection.component.Refreshable
import utopia.reflection.component.drawing.{BackgroundDrawer, CustomDrawer}
import utopia.reflection.component.input.SelectableWithPointers
import utopia.reflection.container.stack.StackLayout
import utopia.reflection.container.stack.StackLayout.Fit
import utopia.reflection.container.swing.Stack
import utopia.reflection.container.swing.Stack.AwtStackable
import utopia.reflection.container.swing.window.{Popup, Window}
import utopia.reflection.controller.data.StackSelectionManager
import utopia.reflection.localization.{DisplayFunction, LocalizedString}
import utopia.reflection.shape.StackLength
import utopia.reflection.text.{Font, Prompt}
import utopia.flow.util.StringExtensions._
import utopia.genesis.event.KeyStateEvent
import utopia.genesis.handling.KeyStateListener
import utopia.inception.handling.immutable.Handleable
import utopia.reflection.component.swing.label.ItemLabel
import utopia.reflection.util.ComponentContext

import scala.concurrent.ExecutionContext

object SearchFromField
{
	/**
	  * Creates a new search from field with component creation context
	  * @param selectionPrompt Prompt that is displayed
	  * @param maxNumberOfDisplaysShown Maximum number of selectable items shown at once (None if not limited, default)
	  * @param displayStackLayout Stack layout used in selection items display (default = Fit)
	  * @param contentPointer Content pointer used (default = new pointer)
	  * @param checkEquals Function for checking item equality (default = use standard equals)
	  * @param makeDisplay Function for creating display components
	  * @param itemToSearchString Function for converting selectable items to search / display strings
	  * @param context Component creation context (implicit)
	  * @param exc Execution context (implicit)
	  * @tparam A Type of selected item
	  * @tparam C Type of display used
	  * @return A new search from field
	  */
	def contextual[A, C <: AwtStackable with Refreshable[A]]
	(selectionPrompt: LocalizedString, maxNumberOfDisplaysShown: Option[Int] = None, displayStackLayout: StackLayout = Fit,
	 contentPointer: PointerWithEvents[Vector[A]] = new PointerWithEvents[Vector[A]](Vector()),
	 checkEquals: (A, A) => Boolean = (a: A, b: A) => a == b)(makeDisplay: A => C)(itemToSearchString: A => String)
	(implicit context: ComponentContext, exc: ExecutionContext) =
	{
		val field = new SearchFromField[A, C](context.actorHandler, selectionPrompt, context.textFieldWidth,
			context.insideMargins.height, new BackgroundDrawer(context.highlightColor), context.font, context.textColor,
			context.promptTextColor, context.stackMargin, displayStackLayout, maxNumberOfDisplaysShown, contentPointer,
			checkEquals)(makeDisplay)(itemToSearchString)
		context.setBorderAndBackground(field)
		field
	}
	
	/**
	  * Creates a new search from field that displays items as text
	  * @param displayFunction Display function used for transforming items to text
	  * @param selectionPrompt Prompt that is displayed
	  * @param maxNumberOfDisplaysShown Maximum number of selectable items shown at once (None if not limited, default)
	  * @param displayStackLayout Stack layout used in selection items display (default = Fit)
	  * @param contentPointer Content pointer used (default = new pointer)
	  * @param checkEquals Function for checking item equality (default = use standard equals)
	  * @param context Component creation context (implicit)
	  * @param exc Execution context (implicit)
	  * @tparam A Type of selected item
	  * @return A new search from field
	  */
	def contextualWithTextOnly[A](displayFunction: DisplayFunction[A], selectionPrompt: LocalizedString,
								  maxNumberOfDisplaysShown: Option[Int] = None, displayStackLayout: StackLayout = Fit,
								  contentPointer: PointerWithEvents[Vector[A]] = new PointerWithEvents[Vector[A]](Vector()),
								  checkEquals: (A, A) => Boolean = (a: A, b: A) => a == b)
								 (implicit context: ComponentContext, exc: ExecutionContext) =
	{
		def makeField(item: A) = ItemLabel.contextual(item, displayFunction)
		def itemToSearchString(item: A) = displayFunction(item).string
		
		contextual(selectionPrompt, maxNumberOfDisplaysShown, displayStackLayout, contentPointer, checkEquals)(
			makeField)(itemToSearchString)
	}
	
	// TODO: Add one more constructor that displays text + icon
}

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
 checkEquals: (A, A) => Boolean = (a: A, b: A) => a == b)(makeDisplay: A => C)(itemToSearchString: A => String)
(implicit exc: ExecutionContext)
	extends StackableAwtComponentWrapperWrapper with SelectableWithPointers[Option[A], Vector[A]] with SwingComponentRelated
{
	// ATTRIBUTES	----------------------------
	
	private val searchField = new TextField(defaultWidth, vMargin, font,
		prompt = Some(Prompt(selectionPrompt, font, promptTextColor)), textColor = textColor)
	private val searchStack = Stack.column[C](margin = betweenDisplaysMargin, layout = displayStackLayout)
	private val displaysManager = new StackSelectionManager[A, C](searchStack, selectionDrawer, checkEquals)(makeDisplay)
	
	// TODO: Consider making this a volatile option
	private var visiblePopup: Option[Window[_]] = None
	private var isSelected = false
	private var currentSearchString = ""
	private var currentOptions = content.map
	{ a => itemToSearchString(a) -> a }.toMap
	
	
	// INITIAL CODE	-----------------------------
	
	// When the field gains focus, displays the pop-up window (if not yet displayed)
	searchField.addFocusChangedListener
	{displayPopup()}
	{
		visiblePopup.foreach
		{_.close()}
		visiblePopup = None
	}
	
	// When content updates, changes selection options and updates field size
	addContentListener
	{ e =>
		currentOptions = e.newValue.map
		{ a => itemToSearchString(a) -> a }.toMap
		updateDisplayedOptions()
		searchField.targetWidth = if (content.isEmpty) defaultWidth
		else searchStack.stackSize.width
	}
	
	// When selection changes in pop-up, updates text field contents
	addValueListener
	{ e =>
		e.newValue match
		{
			case Some(newSelected) =>
				if (!isSelected)
					isSelected = true
				val newItemAsString = itemToSearchString(newSelected)
				currentSearchString = newItemAsString
				searchField.text = newItemAsString
			case None =>
				isSelected = false
		}
	}
	
	// When text field updates (while no value is selected)
	searchField.addValueListener
	{
		_.newValue match
		{
			case Some(newFilter) =>
				if (currentSearchString != newFilter)
				{
					currentSearchString = newFilter
				}
			case None =>
				currentSearchString = ""
		}
	}
	
	displaysManager.enableMouseHandling()
	
	addKeyStateListener(SelectionKeyListener)
	
	
	// IMPLEMENTED	----------------------------
	
	override def component = searchField.component
	
	override def valuePointer = displaysManager.valuePointer
	
	override protected def wrapped = searchField
	
	
	// OTHER	--------------------------------
	
	private def updateDisplayedOptions() =
	{
		// Applies filter, if necessary
		val availableItems =
		{
			if (currentSearchString.isEmpty)
				content
			else
			{
				val searchWords = currentSearchString.words.map
				{_.toLowerCase}
				currentOptions.filterKeys
				{ k =>
					val lower = k.toLowerCase
					searchWords.forall(lower.contains)
				}.values.toVector
			}
		}
		// Applies maximum limit, if specified
		displaysManager.content = maxNumberOfDisplaysShown.map(availableItems.take).getOrElse(availableItems)
	}
	
	private def displayPopup() =
	{
		if (visiblePopup.isEmpty && content.nonEmpty)
		{
			// Creates and displays the popup
			val popup = Popup(searchField, searchStack, actorHandler, (fieldSize, _) => Point(0, fieldSize.height))
			visiblePopup = Some(popup)
			popup.display(gainFocus = false)
			popup.closeFuture.foreach
			{ _ => visiblePopup = None }
		}
	}
	
	
	// NESTED	-------------------------------
	
	private object SelectionKeyListener extends KeyStateListener with Handleable
	{
		override def isReceivingKeyStateEvents = visiblePopup.forall { _.isVisible }
		
		// Listens for up and down presses
		override val keyStateEventFilter = KeyStateEvent.keysFilter(Vector(KeyEvent.VK_UP, KeyEvent.VK_DOWN)) &&
			KeyStateEvent.wasPressedFilter
		
		override def onKeyState(event: KeyStateEvent) = if (event.index == KeyEvent.VK_UP)
			displaysManager.selectPrevious() else displaysManager.selectNext()
	}
}
