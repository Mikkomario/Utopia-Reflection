package utopia.reflection.component.swing

import java.awt.event.{ActionEvent, ActionListener, FocusEvent, FocusListener}

import utopia.reflection.shape.LengthExtensions._
import utopia.flow.generic.ValueConversions._
import javax.swing.JTextField
import javax.swing.event.{DocumentEvent, DocumentListener}
import javax.swing.text.{Document, PlainDocument}
import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.flow.event.{ChangeEvent, ChangeListener}
import utopia.genesis.color.Color
import utopia.genesis.shape.Axis.X
import utopia.reflection.component.{Alignable, Focusable}
import utopia.reflection.component.input.InteractionWithPointer
import utopia.reflection.component.stack.CachingStackable
import utopia.reflection.localization.LocalizedString
import utopia.reflection.shape.{Alignment, Border, Insets, StackLength, StackSize}
import utopia.reflection.text.{Font, Prompt, Regex}
import utopia.reflection.util.ComponentContext

object TextField
{
	/**
	  * Creates a new text field that is used for writing positive integers
	  * @param targetWidth Target width of the field
	  * @param insideMargins Margins placed on each side of text
	  * @param font Font used within this field
	  * @param initialValue The initial value displayed (default = None)
	  * @param prompt A prompt displayed when this field is empty (default = None)
	  * @param textColor The text color used (default = 88% opacity black)
	  * @return A new text field that formats values to positive integers
	  */
	def forPositiveInts(targetWidth: StackLength, insideMargins: StackSize, font: Font, initialValue: Option[Int] = None,
						prompt: Option[Prompt] = None, textColor: Color = Color.textBlack, alignment: Alignment = Alignment.Left) =
	{
		new TextField(targetWidth, insideMargins, font, FilterDocument(Regex.digit, 10),
			initialValue.map { _.toString } getOrElse "", prompt, textColor, Some(Regex.numericPositive), alignment)
	}
	
	/**
	  * Creates a new text field that is used for writing integers
	  * @param targetWidth Target width of the field
	  * @param insideMargins Margins placed on each side of text
	  * @param font Font used within this field
	  * @param initialValue The initial value displayed (default = None)
	  * @param prompt A prompt displayed when this field is empty (default = None)
	  * @param textColor The text color used (default = 88% opacity black)
	  * @return A new text field that formats values to integers
	  */
	def forInts(targetWidth: StackLength, insideMargins: StackSize, font: Font, initialValue: Option[Int] = None,
				prompt: Option[Prompt] = None, textColor: Color = Color.textBlack, alignment: Alignment = Alignment.Left) =
	{
		new TextField(targetWidth, insideMargins, font, FilterDocument(Regex.numericParts, 11),
			initialValue.map { _.toString } getOrElse "", prompt, textColor, Some(Regex.numeric), alignment)
	}
	
	/**
	  * Creates a new text field that is used for writing positive doubles
	  * @param targetWidth Target width of the field
	  * @param insideMargins Margins placed on each side of text
	  * @param font Font used within this field
	  * @param initialValue The initial value displayed (default = None)
	  * @param prompt A prompt displayed when this field is empty (default = None)
	  * @param textColor The text color used (default = 88% opacity black)
	  * @return A new text field that formats values to positive doubles
	  */
	def forPositiveDoubles(targetWidth: StackLength, insideMargins: StackSize, font: Font, initialValue: Option[Double] = None,
						   prompt: Option[Prompt] = None, textColor: Color = Color.textBlack, alignment: Alignment = Alignment.Left) =
	{
		new TextField(targetWidth, insideMargins, font, FilterDocument(Regex.decimalPositiveParts, 24),
			initialValue.map { _.toString } getOrElse "", prompt, textColor, Some(Regex.decimalPositive), alignment)
	}
	
	/**
	  * Creates a new text field that is used for writing doubles
	  * @param targetWidth Target width of the field
	  * @param insideMargins Margins placed on each side of text
	  * @param font Font used within this field
	  * @param initialValue The initial value displayed (default = None)
	  * @param prompt A prompt displayed when this field is empty (default = None)
	  * @param textColor The text color used (default = 88% opacity black)
	  * @return A new text field that formats values to doubles
	  */
	def forDoubles(targetWidth: StackLength, insideMargins: StackSize, font: Font, initialValue: Option[Double] = None,
				   prompt: Option[Prompt] = None, textColor: Color = Color.textBlack, alignment: Alignment = Alignment.Left) =
	{
		new TextField(targetWidth, insideMargins, font, FilterDocument(Regex.decimalParts, 24),
			initialValue.map { _.toString } getOrElse "", prompt, textColor, Some(Regex.decimal), alignment)
	}
	
	/**
	  * Creates a new text field using contextual information
	  * @param document Document used for this field (default = plain document)
	  * @param initialText Initially displayed text (default = no text)
	  * @param prompt Prompt text displayed (optional)
	  * @param resultFilter A regex used for transforming field content (default = None)
	  * @param context Component creation context
	  * @return A new text field
	  */
	def contextual(document: Document = new PlainDocument(), initialText: String = "",
				   prompt: Option[LocalizedString] = None, resultFilter: Option[Regex] = None)
				  (implicit context: ComponentContext) =
	{
		val field = new TextField(context.textFieldWidth, context.insideMargins, context.font, document,
			initialText, prompt.map { Prompt(_, context.promptFont, context.promptTextColor) }, context.textColor,
			resultFilter)
		context.setBorderAndBackground(field)
		field.addFocusHighlight(context.focusColor)
		
		field
	}
	
	/**
	  * Creates a field that is used for writing positive integers. Uses component creation context.
	  * @param initialValue Initially displayed value (Default = None)
	  * @param prompt Prompt text displayed, if any (Default = None)
	  * @param context Component creation context (implicit)
	  * @return A new text field
	  */
	def contextualForPositiveInts(initialValue: Option[Int] = None, prompt: Option[LocalizedString] = None)
								 (implicit context: ComponentContext) = contextual(
		FilterDocument(Regex.digit, 10), initialValue.map { _.toString } getOrElse "", prompt, Some(Regex.numericPositive))
	
	/**
	  * Creates a field that is used for writing positive or negative integers. Uses component creation context.
	  * @param initialValue Initially displayed value (Default = None)
	  * @param prompt Prompt text displayed, if any (Default = None)
	  * @param context Component creation context (implicit)
	  * @return A new text field
	  */
	def contextualForInts(initialValue: Option[Int] = None, prompt: Option[LocalizedString] = None)
								 (implicit context: ComponentContext) = contextual(
		FilterDocument(Regex.numericParts, 11), initialValue.map { _.toString } getOrElse "", prompt, Some(Regex.numeric))
	
	/**
	  * Creates a field that is used for writing positive doubles. Uses component creation context.
	  * @param initialValue Initially displayed value (Default = None)
	  * @param prompt Prompt text displayed, if any (Default = None)
	  * @param context Component creation context (implicit)
	  * @return A new text field
	  */
	def contextualForPositiveDoubles(initialValue: Option[Double] = None, prompt: Option[LocalizedString] = None)
								 (implicit context: ComponentContext) = contextual(
		FilterDocument(Regex.decimalPositiveParts, 24), initialValue.map { _.toString } getOrElse "", prompt, Some(Regex.decimalPositive))
	
	/**
	  * Creates a field that is used for writing positive or negative doubles. Uses component creation context.
	  * @param initialValue Initially displayed value (Default = None)
	  * @param prompt Prompt text displayed, if any (Default = None)
	  * @param context Component creation context (implicit)
	  * @return A new text field
	  */
	def contextualForDoubles(initialValue: Option[Double] = None, prompt: Option[LocalizedString] = None)
						 (implicit context: ComponentContext) = contextual(
		FilterDocument(Regex.decimalParts, 24), initialValue.map { _.toString } getOrElse "", prompt, Some(Regex.decimal))
}

/**
  * Text fields are used for collecting text input from user
  * @author Mikko Hilpinen
  * @since 1.5.2019, v1+
  * @param initialTargetWidth The target width of this field
  * @param insideMargins The target margins around text in this field
  * @param font The font used in this field
  * @param document The document used in this field (default = plain document)
  * @param initialText The initially displayed text (default = "")
  * @param prompt The prompt for this field (default = None)
  * @param textColor The text color in this field (default = 88% opacity black)
  */
class TextField(initialTargetWidth: StackLength, val insideMargins: StackSize, font: Font,
				val document: Document = new PlainDocument(), initialText: String = "",
				val prompt: Option[Prompt] = None, val textColor: Color = Color.textBlack,
				resultFilter: Option[Regex] = None, initialAlignment: Alignment = Alignment.Left)
	extends JWrapper with CachingStackable with InteractionWithPointer[Option[String]] with Alignable with Focusable
{
	// ATTRIBUTES	----------------------
	
	private val field = new JTextField()
	private val defaultBorder = Border.square(1, textColor.timesAlpha(0.625))
	
	private lazy val promptDocument = new PlainDocument()
	private var isDisplayingPrompt = false
	private var isUpdatingText = false
	private var enterListeners = Vector[Option[String] => Unit]()
	private var resultListeners = Vector[Option[String] => Unit]()
	
	override val valuePointer = new PointerWithEvents[Option[String]](None)
	
	private var _targetWidth = initialTargetWidth
	
	
	// INITIAL CODE	----------------------
	
	field.setFont(font.toAwt)
	field.setForeground(textColor.toAwt)
	field.setDocument(document)
	
	setBorder(defaultBorder)
	text = initialText
	
	document.addDocumentListener(new InputListener)
	field.addActionListener(new EnterListener())
	if (prompt.isDefined)
		field.addFocusListener(new PromptFocusListener())
	valuePointer.addListener(new ValueChangeListener)
	
	{
		// TODO: Handle alignment better (take into account bottom & top alignments)
		val alignment = initialAlignment.horizontal
		if (alignment == Alignment.Left)
			alignLeft(insideMargins.width.optimal)
		else
			align(alignment)
	}
	
	
	// COMPUTED	--------------------------
	
	/**
	  * @return The width (stack length) of this field
	  */
	def targetWidth = _targetWidth
	def targetWidth_=(newWidth: StackLength) =
	{
		if (_targetWidth != newWidth)
		{
			_targetWidth = newWidth
			revalidate()
		}
	}
	
	/**
	  * @return Current text in this field
	  */
	def text =
	{
		if (isDisplayingPrompt)
			""
		else
		{
			val t = field.getText
			if (t == null) "" else t
		}
	}
	def text_=(newText: String): Unit =
	{
		if (!isUpdatingText)
		{
			// Updates text field value
			val raw = newText.trim
			value = resultFilter.map { _.findFirstFrom(raw) }.getOrElse { if (raw.isEmpty) None else Some(raw) }
			
			isUpdatingText = true
			if (prompt.isEmpty)
				field.setText(newText)
			else
			{
				// On empty text, may display prompt instead
				if (newText.isEmpty)
				{
					field.setText(newText)
					showPrompt()
				}
				else
				{
					// May disable a prompt if one is shown
					hidePrompt()
					field.setText(newText)
				}
			}
			// TODO: Trigger value update event
			isUpdatingText = false
		}
	}
	def text_=(newText: Option[String]): Unit = text_=(newText getOrElse "")
	
	/**
	  * @return Current integer value in this field. None if the text couldn't be read as an integer
	  */
	def intValue = value.int
	
	/**
	  * @return Current double value in this field. None if the text couldn't be read as a double
	  */
	def doubleValue = value.double
	
	
	// IMPLEMENTED	--------------------
	
	override protected def updateVisibility(visible: Boolean) = super[JWrapper].isVisible_=(visible)
	
	override def updateLayout() = Unit
	
	// Empty strings and strings with only whitespaces are treated as None
	override def value =
	{
		val raw = text.trim
		resultFilter.map { _.findFirstFrom(raw) }.getOrElse { if (raw.isEmpty) None else Some(raw) }
	}
	
	override def component = field
	
	override protected def calculatedStackSize =
	{
		val h = textHeight.map { insideMargins.height * 2 + _ } getOrElse 32.any
		StackSize(targetWidth, h)
	}
	
	override def align(alignment: Alignment) = alignment.horizontal.swingComponents.get(X).foreach(field.setHorizontalAlignment)
	
	override def isInFocus = field.hasFocus
	
	override def requestFocusInWindow() = field.requestFocusInWindow()
	
	
	// OTHER	------------------------------
	
	/**
	  * Clears all text from this field
	  */
	def clear() = text = ""
	
	/**
	  * Aligns this field to the left and adds margin
	  * @param margin The amount of margin
	  */
	def alignLeft(margin: Double): Unit =
	{
		alignLeft()
		if (margin > 0)
			setBorder(defaultBorder + Border(Insets.left(margin), None))
	}
	
	/**
	  * Adds a listener that will be informed when user presses enter inside this text field. Informs listener
	  * of the processed value of this text field
	  * @param listener A listener
	  */
	def addEnterListener(listener: Option[String] => Unit) = enterListeners :+= listener
	/**
	  * Adds a listener that will be informed when this field loses focus or the user presses enter inside this field.
	  * Informs the listener of the processed value of this field.
	  * @param listener A listener
	  */
	def addResultListener(listener: Option[String] => Unit) = resultListeners :+= listener
	
	/**
	  * Adds focus highlighting to this text field. The highlighting will change text field background color when
	  * it gains focus
	  * @param color The background color used when focused
	  */
	def addFocusHighlight(color: Color) = component.addFocusListener(new FocusHighlighter(background, color))
	
	private def filter() =
	{
		val original = text
		val filtered = resultFilter.map { _.findFirstFrom(original.trim) } getOrElse Some(original.trim)
		val changed = filtered.filter { _ != original }
		
		changed.foreach(text_=)
	}
	
	/**
	  * Hides the prompt if one is currently being displayed
	  */
		// TODO: Create a new prompt system that uses custom drawing instead of field text
	def hidePrompt() =
	{
		if (isDisplayingPrompt)
		{
			isDisplayingPrompt = false
			field.setDocument(document)
			field.setForeground(textColor.toAwt)
			field.setFont(font.toAwt)
		}
	}
	
	private def showPrompt() =
	{
		if (!isDisplayingPrompt && prompt.isDefined)
		{
			isDisplayingPrompt = true
			field.setDocument(promptDocument)
			field.setText(prompt.get.text.string)
			field.setForeground(prompt.get.color.toAwt)
			field.setFont(prompt.get.font.toAwt)
		}
	}
	
	
	// NESTED CLASSES	----------------------
	
	private class EnterListener extends ActionListener
	{
		// When enter is pressed, filters field value and informs listeners
		override def actionPerformed(e: ActionEvent) =
		{
			filter()
			if (enterListeners.nonEmpty || resultListeners.nonEmpty)
			{
				val result = value
				enterListeners.foreach { _(result) }
				resultListeners.foreach { _(result) }
			}
		}
	}
	
	private class ValueChangeListener extends ChangeListener[Option[String]]
	{
		override def onChangeEvent(event: ChangeEvent[Option[String]]) =
		{
			if (!isUpdatingText)
				text = event.newValue getOrElse ""
		}
	}
	
	private class InputListener extends DocumentListener
	{
		override def insertUpdate(e: DocumentEvent) = handleInputChange()
		
		override def removeUpdate(e: DocumentEvent) = handleInputChange()
		
		override def changedUpdate(e: DocumentEvent) = handleInputChange()
		
		private def handleInputChange() =
		{
			if (!isUpdatingText)
			{
				isUpdatingText = true
				val raw = text.trim
				value = resultFilter.map { _.findFirstFrom(raw) }.getOrElse { if (raw.isEmpty) None else Some(raw) }
				isUpdatingText = false
			}
		}
	}
	
	private class PromptFocusListener extends FocusListener
	{
		override def focusGained(e: FocusEvent) = hidePrompt()
		
		override def focusLost(e: FocusEvent) =
		{
			filter()
			// May show prompt when focus is lost
			if (text.isEmpty) showPrompt()
			// Informs results listeners
			if (resultListeners.nonEmpty)
			{
				val result = value
				resultListeners.foreach { _(result) }
			}
		}
	}
	
	private class FocusHighlighter(val defaultBackground: Color, val highlightBackground: Color) extends FocusListener
	{
		override def focusGained(e: FocusEvent) = background = highlightBackground
		
		override def focusLost(e: FocusEvent) = background = defaultBackground
	}
}
