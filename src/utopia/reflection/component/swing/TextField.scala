package utopia.reflection.component.swing

import java.awt.event.{ActionEvent, FocusEvent, FocusListener}

import utopia.reflection.shape.LengthExtensions._
import utopia.flow.generic.ValueConversions._
import javax.swing.{AbstractAction, JTextField}
import javax.swing.event.{DocumentEvent, DocumentListener}
import javax.swing.text.{Document, PlainDocument}
import utopia.genesis.color.Color
import utopia.reflection.component.{Alignable, Alignment}
import utopia.reflection.component.input.InteractionWithEvents
import utopia.reflection.component.stack.CachingStackable
import utopia.reflection.shape.{Border, Insets, StackLength, StackSize}
import utopia.reflection.text.{Font, Prompt}

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
				val prompt: Option[Prompt] = None, val textColor: Color = Color.textBlack) extends JWrapper
	with CachingStackable with InteractionWithEvents[Option[String]] with Alignable
{
	// ATTRIBUTES	----------------------
	
	private val field = new JTextField()
	private val listener = new InputListener()
	
	private lazy val promptDocument = new PlainDocument()
	private var isDisplayingPrompt = false
	
	
	// INITIAL CODE	----------------------
	
	field.setFont(font.toAwt)
	field.setForeground(textColor.toAwt)
	field.setDocument(document)
	alignCenter()
	
	text = initialText
	
	field.getDocument.addDocumentListener(listener)
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
	def text_=(newText: String) =
	{
		if (prompt.isEmpty)
			field.setText(newText)
		else
		{
			// On empty text, may display prompt instead
			if (newText.isEmpty)
				showPrompt()
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
		if (raw.isEmpty)
			None
		else
			Some(raw)
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
	  * Adds a new ation that will be called when user presses enter inside this field
	  * @param action The action that will be performed. Takes current input value.
	  */
	def addActionForEnter(action: Option[String] => Unit) =
	{
		val a = new AbstractAction()
		{
			override def actionPerformed(e: ActionEvent) = action(value)
		}
		field.addActionListener(a)
	}
	
	/**
	  * Aligns this field to the left and adds margin
	  * @param margin The amount of margin
	  */
	def alignLeft(margin: Double): Unit =
	{
		alignLeft()
		setBorder(Border(Insets.left(margin), None))
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
	
	private class InputListener extends DocumentListener
	{
		override def insertUpdate(e: DocumentEvent) = informListeners()
		
		override def removeUpdate(e: DocumentEvent) = informListeners()
		
		override def changedUpdate(e: DocumentEvent) = informListeners()
	}
	
	private class PromptFocusListener extends FocusListener
	{
		override def focusGained(e: FocusEvent) = hidePrompt()
		
		override def focusLost(e: FocusEvent) = if (value.isEmpty) showPrompt()
	}
}
