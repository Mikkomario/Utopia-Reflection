package utopia.reflection.component.swing

import java.awt.event.{ActionEvent, ActionListener, FocusEvent, FocusListener}

import utopia.reflection.shape.LengthExtensions._
import utopia.flow.generic.ValueConversions._
import javax.swing.JTextField
import javax.swing.event.{DocumentEvent, DocumentListener}
import javax.swing.text.{Document, PlainDocument}
import utopia.genesis.color.Color
import utopia.reflection.component.{Alignable, Alignment}
import utopia.reflection.component.input.InteractionWithEvents
import utopia.reflection.component.stack.CachingStackable
import utopia.reflection.shape.{Border, Insets, StackLength, StackSize}
import utopia.reflection.text.{Font, Prompt, Regex}

object TextField
{
	/**
	  * Creates a new text field that is used for writing positive integers
	  * @param targetWidth Target width of the field
	  * @param vMargin Vertical margin placed on each side of text
	  * @param font Font used within this field
	  * @param initialValue The initial value displayed (default = None)
	  * @param prompt A prompt displayed when this field is empty (default = None)
	  * @param textColor The text color used (default = 88% opacity black)
	  * @return A new text field that formats values to positive integers
	  */
	def forPositiveInts(targetWidth: StackLength, vMargin: StackLength, font: Font, initialValue: Option[Int] = None,
						prompt: Option[Prompt] = None, textColor: Color = Color.textBlack) =
	{
		new TextField(targetWidth, vMargin, font, FilterDocument(Regex.digit, 10),
			initialValue.map { _.toString } getOrElse "", prompt, textColor, Some(Regex.numericPositive))
	}
	
	/**
	  * Creates a new text field that is used for writing integers
	  * @param targetWidth Target width of the field
	  * @param vMargin Vertical margin placed on each side of text
	  * @param font Font used within this field
	  * @param initialValue The initial value displayed (default = None)
	  * @param prompt A prompt displayed when this field is empty (default = None)
	  * @param textColor The text color used (default = 88% opacity black)
	  * @return A new text field that formats values to integers
	  */
	def forInts(targetWidth: StackLength, vMargin: StackLength, font: Font, initialValue: Option[Int] = None,
				prompt: Option[Prompt] = None, textColor: Color = Color.textBlack) =
	{
		new TextField(targetWidth, vMargin, font, FilterDocument(Regex.numericParts, 11),
			initialValue.map { _.toString } getOrElse "", prompt, textColor, Some(Regex.numeric))
	}
	
	/**
	  * Creates a new text field that is used for writing positive doubles
	  * @param targetWidth Target width of the field
	  * @param vMargin Vertical margin placed on each side of text
	  * @param font Font used within this field
	  * @param initialValue The initial value displayed (default = None)
	  * @param prompt A prompt displayed when this field is empty (default = None)
	  * @param textColor The text color used (default = 88% opacity black)
	  * @return A new text field that formats values to positive doubles
	  */
	def forPositiveDoubles(targetWidth: StackLength, vMargin: StackLength, font: Font, initialValue: Option[Double] = None,
						   prompt: Option[Prompt] = None, textColor: Color = Color.textBlack) =
	{
		new TextField(targetWidth, vMargin, font, FilterDocument(Regex.decimalPositiveParts, 24),
			initialValue.map { _.toString } getOrElse "", prompt, textColor, Some(Regex.decimalPositive))
	}
	
	/**
	  * Creates a new text field that is used for writing doubles
	  * @param targetWidth Target width of the field
	  * @param vMargin Vertical margin placed on each side of text
	  * @param font Font used within this field
	  * @param initialValue The initial value displayed (default = None)
	  * @param prompt A prompt displayed when this field is empty (default = None)
	  * @param textColor The text color used (default = 88% opacity black)
	  * @return A new text field that formats values to doubles
	  */
	def forDoubles(targetWidth: StackLength, vMargin: StackLength, font: Font, initialValue: Option[Double] = None,
				   prompt: Option[Prompt] = None, textColor: Color = Color.textBlack) =
	{
		new TextField(targetWidth, vMargin, font, FilterDocument(Regex.decimalParts, 24),
			initialValue.map { _.toString } getOrElse "", prompt, textColor, Some(Regex.decimal))
	}
}

/**
  * Text fields are used for collecting text input from user
  * @author Mikko Hilpinen
  * @since 1.5.2019, v1+
  * @param targetWidth The target width of this field
  * @param vMargin The target vertical margin around text in this field
  * @param font The font used in this field
  * @param document The document used in this field (default = plain document)
  * @param initialText The initially displayed text (default = "")
  * @param prompt The prompt for this field (default = None)
  * @param textColor The text color in this field (default = 88% opacity black)
  */
class TextField(val targetWidth: StackLength, val vMargin: StackLength, font: Font,
				val document: Document = new PlainDocument(), initialText: String = "",
				val prompt: Option[Prompt] = None, val textColor: Color = Color.textBlack,
				resultFilter: Option[Regex] = None)
	extends JWrapper with CachingStackable with InteractionWithEvents[Option[String]] with Alignable
{
	// ATTRIBUTES	----------------------
	
	private val field = new JTextField()
	private val listener = new InputListener()
	private val defaultBorder = Border.square(1, textColor.timesAlpha(0.625))
	
	private lazy val promptDocument = new PlainDocument()
	private var isDisplayingPrompt = false
	private var enterListeners = Vector[Option[String] => Unit]()
	private var resultListeners = Vector[Option[String] => Unit]()
	
	
	// INITIAL CODE	----------------------
	
	field.setFont(font.toAwt)
	field.setForeground(textColor.toAwt)
	field.setDocument(document)
	alignCenter()
	
	setBorder(defaultBorder)
	text = initialText
	
	field.getDocument.addDocumentListener(listener)
	field.addActionListener(new EnterListener())
	if (prompt.isDefined)
		field.addFocusListener(new PromptFocusListener())
	
	
	// COMPUTED	--------------------------
	
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
	
	override def setValueNoEvents(newValue: Option[String]) =
	{
		field.getDocument.removeDocumentListener(listener)
		text = newValue getOrElse ""
		field.getDocument.addDocumentListener(listener)
	}
	
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
		val h = textHeight.map { vMargin * 2 + _ } getOrElse 32.any
		StackSize(targetWidth, h)
	}
	
	override def align(alignment: Alignment) = field.setHorizontalAlignment(alignment.horizontal.toSwingAlignment)
	
	
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
		setBorder(defaultBorder + Border(Insets.left(margin), None))
	}
	
	/**
	  * Requests focus within this window
	  * @return Whether this field is likely to gain focus
	  */
	def requestFocus() = field.requestFocusInWindow()
	
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
	
	private def filter() =
	{
		val original = text
		val filtered = resultFilter.map { _.findFirstFrom(original.trim) } getOrElse Some(original.trim)
		val changed = filtered.filter { _ != original }
		
		changed.foreach
		{
			c =>
				field.setText(c)
				informListeners()
		}
	}
	
	private def hidePrompt() =
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
	
	private class InputListener extends DocumentListener
	{
		override def insertUpdate(e: DocumentEvent) = informListeners()
		
		override def removeUpdate(e: DocumentEvent) = informListeners()
		
		override def changedUpdate(e: DocumentEvent) = informListeners()
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
}
