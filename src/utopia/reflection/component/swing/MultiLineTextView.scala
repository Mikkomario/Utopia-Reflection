package utopia.reflection.component.swing

import utopia.reflection.shape.LengthExtensions._
import utopia.flow.util.StringExtensions._
import utopia.flow.util.CollectionExtensions._
import utopia.genesis.color.Color
import utopia.reflection.component.swing.label.TextLabel
import utopia.reflection.component.TextComponent
import utopia.reflection.component.drawing.CustomDrawableWrapper
import utopia.reflection.container.stack.StackLayout.{Center, Leading, Trailing}
import utopia.reflection.container.swing.{AlignFrame, Stack, SwitchPanel}
import utopia.reflection.localization.{LocalString, LocalizedString}
import utopia.reflection.shape.{Alignment, StackLength, StackSize}
import utopia.reflection.text.Font

/**
  * Presents text using multiple lines
  * @author Mikko Hilpinen
  * @since 10.12.2019, v1+
  * @param initialText Initially displayed text
  * @param initialFont Initially used font
  * @param initialLineSplitThreshold Maximum line length before splitting text to another line (single word lines
  *                                  may still exceed threshold)
  * @param margins Margins placed around the text in this view (default = any, preferring 0)
  * @param betweenLinesMargin Margin placed between each line of text (default = fixed to 0)
  * @param useLowPriorityForScalingSides Whether low stack length priority should be used for sides which are affected
  *                                      by alignment (for example right side with left alignment) (default = false)
  * @param initialAlignment Initially used text and content alignment (default = left)
  * @param initialTextColor Initially used text color (default = slightly opaque black)
  */
class MultiLineTextView(initialText: LocalizedString, initialFont: Font, initialLineSplitThreshold: Double,
						override val margins: StackSize = StackSize.any,
						val betweenLinesMargin: StackLength = StackLength.fixed(0),
						val useLowPriorityForScalingSides: Boolean = false, initialAlignment: Alignment = Alignment.Left,
						initialTextColor: Color = Color.textBlack)
	extends StackableAwtComponentWrapperWrapper with TextComponent with CustomDrawableWrapper
{
	// ATTRIBUTES	------------------------
	
	private var _alignment = initialAlignment
	private var _text = initialText
	private var _font = initialFont
	private var _textColor = initialTextColor
	private var _lineSplitThreshold = initialLineSplitThreshold
	
	private val panel = new SwitchPanel[AlignFrame[Stack[TextLabel]]](makeNewContent())
	
	
	// COMPUTED	----------------------------
	
	/**
	  * @return Maximum length of a multiple word line (in pixels)
	  */
	def lineSplitThreshold = _lineSplitThreshold
	def lineSplitThreshold_=(newThreshold: Double) =
	{
		_lineSplitThreshold = newThreshold
		resetContent()
	}
	
	
	// IMPLEMENTED	------------------------
	
	override def drawable = panel
	
	override protected def wrapped = panel
	
	override def alignment = _alignment
	def alignment_=(newAlignment: Alignment) =
	{
		if (_alignment != newAlignment)
		{
			_alignment = newAlignment
			resetContent()
		}
	}
	
	override def text = _text
	def text_=(newText: LocalizedString) =
	{
		if (_text != newText)
		{
			if (_text.string != newText.string)
			{
				_text = newText
				resetContent()
			}
			else
				_text = newText
		}
	}
	
	override def font = _font
	def font_=(newFont: Font) =
	{
		if (_font != newFont)
		{
			_font = newFont
			resetContent()
		}
	}
	
	override def textColor = _textColor
	override def textColor_=(newColor: Color) =
	{
		_textColor = newColor
		// Updates the colors of each line label
		panel.content.foreach { _.content.foreach { _.components.foreach { _.textColor = newColor } } }
	}
	
	
	// OTHER	----------------------------
	
	private def resetContent() = panel.set(makeNewContent())
	
	private def makeNewContent() =
	{
		val stack =
		{
			if (text.string.isEmpty)
				Stack.column[TextLabel](cap = margins.height)
			else
			{
				// Splits the text whenever target width is exeeded. The resulting lines determine the size constraints
				val lines = text.lines.flatMap { s => split(s.string) }
				// val maxLineWidth = lines.flatMap { textWidth(_) }.max
				
				// Creates new line components
				val language = text.languageCode
				val lineComponents = lines.map { line =>
					new TextLabel(LocalizedString(LocalString(line, language), None), font, margins.width x 0.fixed,
						initialAlignment = alignment.horizontal, initialTextColor = textColor) }
				
				// Places the lines in a stack
				// Stack layout depends from current alignment (horizontal)
				val stackLayout = alignment.horizontal match
				{
					case Alignment.Left => Leading
					case Alignment.Right => Trailing
					case _ => Center
				}
				
				Stack.columnWithItems(lineComponents, betweenLinesMargin, margins.height, stackLayout)
			}
		}
		stack.aligned(alignment, useLowPriorityForScalingSides)
	}
	
	private def split(text: String) =
	{
		val threshold = _lineSplitThreshold
		
		var lineSplitIndices = Vector[Int]()
		var currentLineStartIndex = 0
		var lastCursorIndex = 0
		
		// Finds line split indices (NB: Splits are positioned in front of white space characters)
		text.indexOfIterator(" ").foreach { cursorIndex =>
			// Checks whether threshold was exeeded
			// (Cannot split twice at the same point, however)
			if (lastCursorIndex != currentLineStartIndex &&
				textWidth(text.substring(currentLineStartIndex, cursorIndex)).exists { _ > threshold })
			{
				lineSplitIndices :+= lastCursorIndex
				currentLineStartIndex = lastCursorIndex
			}
			lastCursorIndex = cursorIndex
		}
		
		// Splits the string
		if (lineSplitIndices.isEmpty)
			Vector(text)
		else if (lineSplitIndices.size == 1)
		{
			val (first, second) = text.splitAt(lineSplitIndices.head)
			Vector(first, second.trim)
		}
		else
			(-1 +: lineSplitIndices :+ text.length).paired.map { case (start, end) => text.substring(start + 1, end) }
	}
}
