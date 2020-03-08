package utopia.reflection.component.swing

import java.awt.event.KeyEvent

import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.flow.event.Changing
import utopia.genesis.color.Color
import utopia.genesis.handling.mutable.ActorHandler
import utopia.genesis.shape.shape2D.Point
import utopia.reflection.component.Refreshable
import utopia.reflection.component.drawing.{BackgroundDrawer, CustomDrawer}
import utopia.reflection.component.input.SelectableWithPointers
import utopia.reflection.container.stack.StackLayout
import utopia.reflection.container.stack.StackLayout.Fit
import utopia.reflection.container.swing.{Stack, SwitchPanel}
import utopia.reflection.container.swing.Stack.AwtStackable
import utopia.reflection.container.swing.window.{Popup, Window}
import utopia.reflection.controller.data.StackSelectionManager
import utopia.reflection.localization.{DisplayFunction, LocalizedString}
import utopia.reflection.shape.{StackLength, StackSize}
import utopia.reflection.text.{Font, Prompt}
import utopia.flow.util.StringExtensions._
import utopia.genesis.event.{ConsumeEvent, KeyStateEvent, MouseButtonStateEvent}
import utopia.genesis.handling.{KeyStateHandlerType, KeyStateListener, MouseButtonStateHandlerType, MouseButtonStateListener}
import utopia.inception.handling.HandlerType
import utopia.inception.handling.immutable.Handleable
import utopia.reflection.component.drawing.DrawLevel.Normal
import utopia.reflection.component.swing.label.{ItemLabel, TextLabel}
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
	(selectionPrompt: LocalizedString, noResultsView: AwtStackable, maxNumberOfDisplaysShown: Option[Int] = None,
	 displayStackLayout: StackLayout = Fit, contentPointer: PointerWithEvents[Vector[A]] = new PointerWithEvents[Vector[A]](Vector()),
	 searchFieldPointer: PointerWithEvents[Option[String]] = new PointerWithEvents(None),
	 checkEquals: (A, A) => Boolean = (a: A, b: A) => a == b)(makeDisplay: A => C)(itemToSearchString: A => String)
	(implicit context: ComponentContext, exc: ExecutionContext) =
	{
		val field = new SearchFromField[A, C](context.actorHandler, selectionPrompt, noResultsView, context.textFieldWidth,
			context.insideMargins, new BackgroundDrawer(context.highlightColor, Normal), context.font, context.textColor,
			context.promptTextColor, context.stackMargin, displayStackLayout, maxNumberOfDisplaysShown, contentPointer,
			searchFieldPointer, checkEquals)(makeDisplay)(itemToSearchString)
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
	def contextualWithTextOnly[A](selectionPrompt: LocalizedString, noResultsView: AwtStackable,
								  displayFunction: DisplayFunction[A] = DisplayFunction.raw,
								  maxNumberOfDisplaysShown: Option[Int] = None, displayStackLayout: StackLayout = Fit,
								  contentPointer: PointerWithEvents[Vector[A]] = new PointerWithEvents[Vector[A]](Vector()),
								  searchFieldPointer: PointerWithEvents[Option[String]] = new PointerWithEvents(None),
								  checkEquals: (A, A) => Boolean = (a: A, b: A) => a == b)
								 (implicit context: ComponentContext, exc: ExecutionContext) =
	{
		// TODO: Challenge: Pop-up doesn't have a margin on the left side
		def makeField(item: A) = ItemLabel.contextual(item, displayFunction)
		def itemToSearchString(item: A) = displayFunction(item).string
		
		contextual(selectionPrompt, noResultsView, maxNumberOfDisplaysShown, displayStackLayout,
			contentPointer, searchFieldPointer, checkEquals)(makeField)(itemToSearchString)
	}
	
	// TODO: Add one more constructor that displays text + icon
	
	/**
	  * Creates a label that can be used to indicate that no results were found. This label can be used in SearchFromFields.
	  * @param noResultsText Text displayed when no results are found. Should contain a placeholder (%s) for the search filter.
	  * @param searchStringPointer A pointer to current search filter
	  * @param context Component creation context (implicit)
	  * @return New label that adjusts itself based on changes in the search filter
	  */
	def noResultsLabel(noResultsText: LocalizedString, searchStringPointer: Changing[Option[String]])
					  (implicit context: ComponentContext) =
	{
		val label = TextLabel.contextual(noResultsText.interpolate(searchStringPointer.value.getOrElse("")))
		searchStringPointer.addListener { e => label.text = noResultsText.interpolate(e.newValue.getOrElse("")) }
		label
	}
}

/**
  * A custom drop down selection component with a search / filter function
  * @author Mikko Hilpinen
  * @since 26.2.2020, v1
  */
class SearchFromField[A, C <: AwtStackable with Refreshable[A]]
(actorHandler: ActorHandler, selectionPrompt: LocalizedString, noResultsView: AwtStackable, defaultWidth: StackLength,
 insideFieldMargins: StackSize, selectionDrawer: CustomDrawer, font: Font, textColor: Color = Color.textBlack,
 promptTextColor: Color = Color.textBlackDisabled, betweenDisplaysMargin: StackLength = StackLength.fixed(0),
 displayStackLayout: StackLayout = Fit, maxNumberOfDisplaysShown: Option[Int] = None,
 val contentPointer: PointerWithEvents[Vector[A]] = new PointerWithEvents(Vector()),
 searchFieldPointer: PointerWithEvents[Option[String]] = new PointerWithEvents(None),
 checkEquals: (A, A) => Boolean = (a: A, b: A) => a == b)(makeDisplay: A => C)(itemToSearchString: A => String)
(implicit exc: ExecutionContext)
	extends StackableAwtComponentWrapperWrapper with SelectableWithPointers[Option[A], Vector[A]] with SwingComponentRelated
{
	// ATTRIBUTES	----------------------------
	
	private val searchField = new TextField(defaultWidth, insideFieldMargins, font,
		prompt = Some(Prompt(selectionPrompt, font, promptTextColor)), textColor = textColor, valuePointer = searchFieldPointer)
	private val searchStack = Stack.column[C](margin = betweenDisplaysMargin, layout = displayStackLayout)
	private val displaysManager = new StackSelectionManager[A, C](searchStack, selectionDrawer, checkEquals)(makeDisplay)
	private val popupContentView = SwitchPanel[AwtStackable](searchStack)
	
	private var focusGainSkips = 0
	// TODO: Consider making this a volatile option
	private var visiblePopup: Option[Window[_]] = None
	private var currentSearchString = ""
	private var currentOptions = content.map
	{ a => itemToSearchString(a) -> a }.toMap
	
	
	// INITIAL CODE	-----------------------------
	
	searchStack.background = background
	
	// When the field gains focus, displays the pop-up window (if not yet displayed)
	searchField.addFocusChangedListener {
		if (focusGainSkips > 0)
			focusGainSkips -= 1
		else
			displayPopup()
	} {
		// Also, when focus is lost and an item is selected, updates the text as well (unless field is empty,
		// in which case selection is removed)
		if (visiblePopup.isEmpty)
		{
			if (searchField.text.nonEmpty)
				value match
				{
					case Some(selected) => searchField.text = itemToSearchString(selected)
					case None => searchField.clear()
				}
			else
				value = None
		}
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
	
	// When text field updates (while no value is selected)
	searchField.addValueListener
	{
		_.newValue match
		{
			case Some(newFilter) =>
				if (currentSearchString != newFilter)
				{
					currentSearchString = newFilter
					updateDisplayedOptions()
				}
			case None =>
				currentSearchString = ""
				updateDisplayedOptions()
		}
	}
	
	displaysManager.enableMouseHandling()
	displaysManager.enableKeyHandling(actorHandler, listenEnabledCondition = Some(() => searchField.isInFocus ||
		visiblePopup.exists { _.isVisible }))
	
	addKeyStateListener(ShowPopupKeyListener)
	addMouseButtonListener(ShowPopupKeyListener)
	
	//addMouseMoveListener(MouseMoveListener.onEnter(bounds, _ => println("Entered area")))
	
	
	// IMPLEMENTED	----------------------------
	
	override def component = searchField.component
	
	override def valuePointer = displaysManager.valuePointer
	
	override protected def wrapped = searchField
	
	override def background_=(color: Color) = searchField.background = color
	
	
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
		// If only a single item is available, auto-selects that one
		if (availableItems.size == 1)
		{
			displaysManager.content = availableItems
			// Selection is delayed because text field doesn't allow change from within event listener
			value = Some(availableItems.head)
			popupContentView.set(searchStack)
		}
		// If no selection is available, shows no-result view
		else if (availableItems.isEmpty)
		{
			popupContentView.set(noResultsView)
			displaysManager.content = Vector()
		}
		else
		{
			// Applies maximum limit, if specified
			displaysManager.content = maxNumberOfDisplaysShown.map(availableItems.take).getOrElse(availableItems)
			popupContentView.set(searchStack)
		}
	}
	
	private def displayPopup() =
	{
		if (visiblePopup.isEmpty && content.nonEmpty)
		{
			// Creates and displays the popup
			searchStack.revalidate()
			val popup = Popup(searchField, popupContentView, actorHandler) { (fieldSize, _) => Point(0, fieldSize.height) }
			visiblePopup = Some(popup)
			// Relays key events to the search field
			popup.relayAwtKeyEventsTo(searchField)
			popup.addKeyStateListener(KeyStateListener(_ => if (popup.isFocusedWindow) popup.close(),
				KeyStateEvent.keysFilter(KeyEvent.VK_TAB, KeyEvent.VK_ENTER, KeyEvent.VK_ESCAPE) && KeyStateEvent.wasPressedFilter))
			popup.addMouseButtonListener(MouseButtonStateListener(_ =>
			{
				if (popup.isFocusedWindow)
					popup.close()
				None
			}, MouseButtonStateEvent.wasReleasedFilter))
			popup.display()
			popup.closeFuture.foreach { _ =>
				focusGainSkips += 1
				visiblePopup = None
				value match
				{
					case Some(selected) =>
						val selectedAsString = itemToSearchString(selected)
						currentSearchString = selectedAsString
						searchField.text = selectedAsString
					case None => searchField.clear()
				}
			}
		}
	}
	
	
	// NESTED	-------------------------------
	
	private object ShowPopupKeyListener extends KeyStateListener with Handleable with MouseButtonStateListener
	{
		private def isReceivingEvents = visiblePopup.isEmpty
		
		override def allowsHandlingFrom(handlerType: HandlerType) = handlerType match
		{
			case KeyStateHandlerType => isReceivingEvents && searchField.isInFocus
			case MouseButtonStateHandlerType => isReceivingEvents
			case _ => super.allowsHandlingFrom(handlerType)
		}
		
		override val keyStateEventFilter = KeyStateEvent.wasPressedFilter &&
			KeyStateEvent.notKeysFilter(Vector(KeyEvent.VK_ESCAPE, KeyEvent.VK_TAB))
		
		override def onKeyState(event: KeyStateEvent) = displayPopup()
		
		override val mouseButtonStateEventFilter = MouseButtonStateEvent.leftPressedFilter && (e => e.isOverArea(searchField.bounds))
		
		override def onMouseButtonState(event: MouseButtonStateEvent) =
		{
			displayPopup()
			Some(ConsumeEvent("Search From Field Clicked"))
		}
	}
}
