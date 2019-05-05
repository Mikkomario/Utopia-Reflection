package utopia.reflection.component.swing

import utopia.genesis.color.Color
import utopia.genesis.event.{MouseButtonStateEvent, MouseEvent}
import utopia.genesis.handling.MouseButtonStateListener
import utopia.genesis.shape.X
import utopia.inception.handling.immutable.Handleable
import utopia.reflection.component.Refreshable
import utopia.reflection.component.input.Selectable
import utopia.reflection.component.swing.label.TextLabel
import utopia.reflection.container.stack.StackLayout.Fit
import utopia.reflection.container.swing.{AwtContainerRelated, Stack}
import utopia.reflection.localization.{DisplayFunction, LocalizedString}
import utopia.reflection.shape.LengthExtensions._
import utopia.reflection.shape.{Border, StackLength, StackSize}
import utopia.reflection.text.Font

/**
  * This class offers a selection from multiple choices using a horizontal set of tabs
  * @author Mikko Hilpinen
  * @since 4.5.2019, v1+
  */
class TabSelection[A](val font: Font, val color: Color, val optimalHMargin: Int, val vMargin: StackLength,
					  val borderWidth: Double = 4.0, val displayFunction: DisplayFunction[A] = DisplayFunction.raw,
					  initialChoices: Seq[A] = Vector())
	extends StackableAwtComponentWrapperWrapper with SwingComponentRelated
	with AwtContainerRelated with Selectable[Option[A], Seq[A]] with Refreshable[Seq[A]]
{
	// ATTRIBUTES	-------------------
	
	private val stack = new Stack[TextLabel](X, Fit, 0.fixed, 0.fixed)
	private val textMargin = StackSize(StackLength(0, optimalHMargin), vMargin)
	
	private var options: Seq[(A, TextLabel)] = Vector()
	private var _selected: Option[(A, TextLabel)] = None
	
	
	// INITIAL CODE	-------------------
	
	content = initialChoices
	
	
	// IMPLEMENTED	-------------------
	
	override def component = stack.component
	
	override protected def wrapped = stack
	
	override def background_=(color: Color) = super[StackableAwtComponentWrapperWrapper].background_=(color)
	
	// Finds the matching label and selects it, or just removes any selection
	override def setValueNoEvents(newValue: Option[A]) = updateSelection(newValue.flatMap { v => options.find { _._1 == v } })
	
	override def content = options.map { _._1 }
	
	override def value = _selected.map { _._1 }
	
	override def content_=(newContent: Seq[A]) =
	{
		// Makes sure there is a right amount of labels
		val oldValue = value
		val oldLabels = options.map { _._2 }
		val labels =
		{
			if (oldLabels.size > newContent.size)
			{
				val labelsToRemove = oldLabels.dropRight(options.size - newContent.size)
				stack --= labelsToRemove
				labelsToRemove.foreach { _.mouseButtonHandler.clear() }
				oldLabels.take(newContent.size)
			}
			else if (oldLabels.size < newContent.size)
			{
				val newLabels = Vector.fill(newContent.size - oldLabels.size)
				{
					val label = new TextLabel(LocalizedString.empty, font, textMargin, false)
					styleNotSelected(label)
					label.alignCenter()
					// TODO: Border should be different at the edges
					label.setBorder(Border.symmetric(borderWidth / 2, borderWidth, color))
					label.addMouseButtonListener(new LabelMouseListener(label))
					label
				}
				
				stack ++= newLabels
				oldLabels ++ newLabels
			}
			else
				oldLabels
		}
		
		// Sets old selected label to normal style
		_selected.map { _._2 }.foreach (styleNotSelected)
		
		// Assigns new values to labels
		options = newContent.zip(labels)
		options.foreach { case(v, label) => label.text = displayFunction(v) }
		
		// Handles selection
		if (oldValue.isDefined)
		{
			_selected = options.find { _._1 == oldValue.get }
			_selected.map { _._2 }.foreach(styleSelected)
			// Informs listeners if selection is lost
			if (_selected.isEmpty)
				informListeners()
		}
	}
	
	
	// OTHER	-------------------
	
	private def styleSelected(label: TextLabel) =
	{
		label.background = color
		label.textColor = Color.white
		label.setArrowCursor()
	}
	
	private def styleNotSelected(label: TextLabel) =
	{
		label.background = Color.white
		label.textColor = color
		label.setHandCursor()
	}
	
	private def updateSelection(newSelected: Option[(A, TextLabel)]) =
	{
		// Deselects the old component & selects the new
		if (_selected != newSelected)
		{
			_selected.map { _._2 }.foreach(styleNotSelected)
			newSelected.map { _._2 }.foreach(styleSelected)
			
			_selected = newSelected
			true
		}
		else
			false
	}
	
	
	// NESTED CLASSES	----------
	
	private class LabelMouseListener(val label: TextLabel) extends MouseButtonStateListener with Handleable
	{
		// ATTRIBUTES	----------
		
		override val mouseButtonStateEventFilter = MouseButtonStateEvent.leftPressedFilter && MouseEvent.isOverAreaFilter(label.bounds)
		
		
		// IMPLEMENTED	---------
		
		// When pressed, selects the label
		override def onMouseButtonState(event: MouseButtonStateEvent) =
		{
			if (updateSelection(options.find { _._2 == label }))
			{
				informListeners()
				true
			}
			else
				false
		}
	}
}
