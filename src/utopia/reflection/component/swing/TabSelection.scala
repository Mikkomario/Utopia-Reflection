package utopia.reflection.component.swing

import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.flow.event.{ChangeEvent, ChangeListener}
import utopia.genesis.color.Color
import utopia.genesis.event.{MouseButtonStateEvent, MouseEvent}
import utopia.genesis.handling.MouseButtonStateListener
import utopia.inception.handling.immutable.Handleable
import utopia.reflection.component.drawing.{BorderDrawer, CustomDrawableWrapper}
import utopia.reflection.component.input.SelectableWithPointers
import utopia.reflection.component.swing.label.TextLabel
import utopia.reflection.container.swing.{AwtContainerRelated, Stack}
import utopia.reflection.localization.{DisplayFunction, LocalizedString}
import utopia.reflection.shape.LengthExtensions._
import utopia.reflection.shape.{Border, StackLength, StackSize}
import utopia.reflection.text.Font

import scala.collection.immutable.HashMap

/**
  * This class offers a selection from multiple choices using a horizontal set of tabs
  * @author Mikko Hilpinen
  * @since 4.5.2019, v1+
  */
class TabSelection[A](val font: Font, val color: Color, val optimalHMargin: Int, val vMargin: StackLength,
					  val borderWidth: Double = 2.0, val displayFunction: DisplayFunction[A] = DisplayFunction.raw,
					  initialChoices: Seq[A] = Vector())
	extends StackableAwtComponentWrapperWrapper with SwingComponentRelated
	with AwtContainerRelated with SelectableWithPointers[Option[A], Seq[A]] with CustomDrawableWrapper
{
	// ATTRIBUTES	-------------------
	
	private val stack = Stack.row[TextLabel](0.fixed)
	private val textMargin = StackSize(StackLength(0, optimalHMargin), vMargin)
	
	private var labels: Map[A, TextLabel] = HashMap()
	
	override val valuePointer = new PointerWithEvents[Option[A]](None)
	override val contentPointer = new PointerWithEvents[Seq[A]](Vector())
	
	
	// INITIAL CODE	-------------------
	
	valuePointer.addListener(new ValueUpdateListener)
	contentPointer.addListener(new ContentUpdateListener)
	
	content = initialChoices
	addCustomDrawer(new BorderDrawer(Border.symmetric(borderWidth, borderWidth, color)))
	
	
	// COMPUTED	-----------------------
	
	private def selectedLabel = selected.flatMap(labels.get)
	
	
	// IMPLEMENTED	-------------------
	
	override def drawable = stack
	
	override def component = stack.component
	
	override protected def wrapped = stack
	
	override def background_=(color: Color) = super[StackableAwtComponentWrapperWrapper].background_=(color)
	
	
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
	
	
	// NESTED CLASSES	----------
	
	private class ValueUpdateListener extends ChangeListener[Option[A]]
	{
		private var lastSelectedLabel: Option[TextLabel] = None
		
		override def onChangeEvent(event: ChangeEvent[Option[A]]) =
		{
			// Styles the labels based on selection
			lastSelectedLabel.foreach(styleNotSelected)
			
			val newSelected = selectedLabel
			newSelected.foreach(styleSelected)
			lastSelectedLabel = newSelected
		}
	}
	
	private class ContentUpdateListener extends ChangeListener[Seq[A]]
	{
		override def onChangeEvent(event: ChangeEvent[Seq[A]]) =
		{
			// Makes sure there is a right amount of labels
			val newContent = event.newValue
			val oldValue = value
			val oldLabels = event.oldValue.map { v => labels(v) }
			val newLabels =
			{
				if (oldLabels.size > newContent.size)
				{
					val labelsToRemove = oldLabels.dropRight(oldLabels.size - newContent.size)
					stack --= labelsToRemove
					labelsToRemove.foreach { _.mouseButtonHandler.clear() }
					oldLabels.take(newContent.size)
				}
				else if (oldLabels.size < newContent.size)
				{
					val moreLabels = Vector.fill(newContent.size - oldLabels.size)
					{
						val label = new TextLabel(LocalizedString.empty, font, textMargin, false)
						styleNotSelected(label)
						label.alignCenter()
						label.setBorder(Border.horizontal(borderWidth / 2, color))
						label.addMouseButtonListener(new LabelMouseListener(label))
						label
					}
					
					stack ++= moreLabels
					oldLabels ++ moreLabels
				}
				else
					oldLabels
			}
			
			// Sets old selected label to normal style
			selectedLabel.foreach (styleNotSelected)
			
			// Assigns new values to labels
			labels = newContent.zip(newLabels).toMap
			newContent.foreach { v => labels(v).text = displayFunction(v) }
			
			// Preserves selection, if possible
			if (oldValue.exists(newContent.contains))
				value = oldValue
			else
				value = None
		}
	}
	
	private class LabelMouseListener(val label: TextLabel) extends MouseButtonStateListener with Handleable
	{
		// ATTRIBUTES	----------
		
		override val mouseButtonStateEventFilter = MouseButtonStateEvent.leftPressedFilter && MouseEvent.isOverAreaFilter(label.bounds)
		
		
		// IMPLEMENTED	---------
		
		// When pressed, selects the label
		override def onMouseButtonState(event: MouseButtonStateEvent) =
		{
			val newValue = labels.find { _._2 == label }.map { _._1 }
			if (newValue.isDefined)
			{
				value = newValue
				true
			}
			else
				false
		}
	}
}
