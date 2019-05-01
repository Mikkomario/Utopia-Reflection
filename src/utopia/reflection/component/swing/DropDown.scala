package utopia.reflection.component.swing

import java.awt.event.{ItemEvent, ItemListener}

import utopia.reflection.shape.LengthExtensions._
import utopia.flow.util.CollectionExtensions._
import javax.swing.{JComboBox, JComponent}
import utopia.genesis.color.Color
import utopia.reflection.component.{CachingStackable, Refreshable}
import utopia.reflection.component.input.Selectable
import utopia.reflection.localization.{DisplayFunction, LocalizedString}
import utopia.reflection.shape.StackSize

/**
  * Dropdowns are used for selecting a single value from multiple alternatives
  * @author Mikko Hilpinen
  * @since 1.5.2019, v1+
  */
class DropDown[A](val margins: StackSize, val selectText: LocalizedString,
				  val displayFunction: DisplayFunction[A] = DisplayFunction.raw, initialContent: Vector[A] = Vector(),
				  val maximumOptimalWidth: Option[Int] = None,
				  modifyLabel: JWrapper => Unit = _ => Unit) extends Selectable[Option[A], Vector[A]]
	with Refreshable[Vector[A]] with JWrapper with CachingStackable
{
	// ATTRIBUTES	-------------------
	
	private val field = new JComboBox[String]()
	private var _content = initialContent
	private var _displayValues = Vector[LocalizedString]()
	private var updatingContent = false
	
	
	// INITIAL CODE	-------------------
	
	field.setEditable(false)
	field.setMaximumRowCount(10)
	field.setForeground(Color.textBlack.toAwt)
	
	// Modifies the renderer
	field.getRenderer match
	{
		case c: JComponent => modifyLabel(JWrapper(c))
		case _ => Unit
	}
	
	content = initialContent
	field.addItemListener(new UserSelectionListener())
	
	
	// COMPUTED	-----------------------
	
	/**
	  * @return The currently selected index where 0 is the first item
	  */
	def selectedIndex =
	{
		val index = field.getSelectedIndex
		// Index 0 in field is a plaeholder text
		if (index < 1) None else Some(index - 1)
	}
	def selectedIndex_=(newIndex: Option[Int]): Unit = selectedIndex_=(newIndex getOrElse -1)
	def selectedIndex_=(newIndex: Int) =
	{
		// Index 0 in field represents the placeholder value (not selected)
		val trueIndex =
		{
			if (newIndex < 0)
				0
			else if (newIndex >= this.count)
				this.count
			else
				newIndex + 1
		}
		
		field.setSelectedIndex(trueIndex)
	}
	
	/**
	  * @return The display of the currently selected value
	  */
	def selectedDisplay = selectedIndex.map(displayValues.apply)
	/**
	  * @return The currently displayed values (not including the placeholder text)
	  */
	def displayValues = _displayValues
	
	
	// IMPLEMENTED	-------------------
	
	override protected def updateVisibility(visible: Boolean) = super[JWrapper].isVisible_=(visible)
	
	override def component = field
	
	override protected def calculatedStackSize =
	{
		// If this drop down contains fields, they may affect the width
		fontMetrics.map
		{
			metrics =>
				val maxTextWidth = (selectText +: displayValues).map { s => metrics.stringWidth(s.string) }.max
				val textW = margins.width * 2 + maxTextWidth
				val textH = margins.height * 2 + metrics.getHeight
				
				// May limit text width optimal
				val finalW = maximumOptimalWidth.map { max => if (textW.optimal > max) textW.withOptimal(max) else
					textW } getOrElse textW
				
				StackSize(finalW, textH)
				
		} getOrElse (220.any x 32.any)
	}
	
	override def updateLayout() = Unit
	
	override def setValueNoEvents(newValue: Option[A]) = selectedIndex = newValue.flatMap(content.optionIndexOf)
	
	override def value = selectedIndex.flatMap(content.getOption)
	
	override def content = _content
	
	override def content_=(newContent: Vector[A]) =
	{
		// Preserves selection
		val oldSelected = selected
		
		updatingContent = true
		_content = newContent
		_displayValues = newContent.map(displayFunction.apply)
		field.removeAllItems()
		(selectText +: _displayValues).foreach { s => field.addItem(s.string) }
		updatingContent = false
		
		// If there is only 1 item available, auto-selects it
		if (_content.size == 1)
			this.selectFirst()
		else
			selected = oldSelected
		revalidate()
	}
	
	
	// NESTED CLASSES	---------------
	
	private class UserSelectionListener extends ItemListener
	{
		// ATTRIBUTES	---------------
		
		private var lastSelected = selected
		
		
		// IMPLEMENTED	---------------
		
		override def itemStateChanged(e: ItemEvent) =
		{
			if (!updatingContent)
			{
				val newSelected = selected
				if (lastSelected != newSelected)
				{
					lastSelected = newSelected
					informListeners(newSelected)
				}
			}
		}
	}
}