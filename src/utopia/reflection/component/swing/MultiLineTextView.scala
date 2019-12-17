package utopia.reflection.component.swing

import utopia.reflection.shape.LengthExtensions._
import utopia.flow.util.StringExtensions._
import utopia.flow.util.CollectionExtensions._
import utopia.genesis.shape.shape2D.Size
import utopia.reflection.component.stack.StackSizeCalculating
import utopia.reflection.component.swing.label.TextLabel
import utopia.reflection.component.{SingleLineTextComponent, TextComponent}
import utopia.reflection.container.stack.StackLayout.{Center, Leading, Trailing}
import utopia.reflection.container.stack.StackLike
import utopia.reflection.container.swing.Stack
import utopia.reflection.localization.{LocalString, LocalizedString}
import utopia.reflection.shape.{Alignment, StackLength, StackSize}
import utopia.reflection.text.Font

/**
  * Presents text using multiple lines
  * @author Mikko Hilpinen
  * @since 10.12.2019, v1+
  */
trait MultiLineTextView extends TextComponent with StackSizeCalculating
{
	// ATTRIBUTES	------------------------
	
	private var preparedStack: Option[Stack[TextLabel]] = None
	
	
	// ABSTRACT	----------------------------
	
	/**
	  * @return Vertical margin placed between each line
	  */
	def betweenLinesMargin: StackLength
	
	/**
	  * @return The desirable (maximum) width for this view
	  */
	protected def targetWidth: Double
	
	
	// IMPLEMENTED	------------------------
	
	override protected def calculatedStackSize =
	{
		if (text.string.isEmpty)
			StackSize.any
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
			val stack = Stack.columnWithItems(lineComponents, betweenLinesMargin, margins.height, stackLayout)
			
			// Caches stack until changes are applied
			preparedStack = Some(stack)
			
			// Stack handles size calculations
			stack.stackSize
		}
	}
	
	override def updateLayout() =
	{
		// Applies possible component changes and repositions content according to current vertical alignment
	}
	
	
	// OTHER	----------------------------
	
	private def split(text: String) =
	{
		val threshold = targetWidth
		
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
