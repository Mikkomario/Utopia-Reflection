package utopia.reflection.component.swing

import java.awt.event.{ActionEvent, ActionListener}

import javax.swing.plaf.basic.ComboPopup
import utopia.reflection.shape.LengthExtensions._
import utopia.flow.util.CollectionExtensions._
import javax.swing.{JComboBox, JList, ListCellRenderer}
import utopia.genesis.color.Color
import utopia.reflection.component.Refreshable
import utopia.reflection.component.input.Selectable
import utopia.reflection.component.stack.CachingStackable
import utopia.reflection.component.swing.label.Label
import utopia.reflection.localization.{DisplayFunction, LocalizedString}
import utopia.reflection.shape.{Border, Insets, StackSize}
import utopia.reflection.text.Font

/**
  * Dropdowns are used for selecting a single value from multiple alternatives
  * @author Mikko Hilpinen
  * @since 1.5.2019, v1+
  * @param margins The margins placed around the text (affects stack size)
  * @param selectText The text displayed when no value is selected
  * @param font The font used in this drop down
  * @param backgroundColor The default label background color
  * @param selectedBackground The background color of currently selected item
  * @param textColor The text color used (default = 88% opacity black)
  * @param displayFunction A function used for transforming values to displayable strings
  *                        (default = toString with no localization)
  * @param initialContent The initially available selections
  * @param maximumOptimalWidth The maximum optimal widht for this drop down (default = maximum based on text length & margin)
  */
class DropDown[A](val margins: StackSize, val selectText: LocalizedString, font: Font, backgroundColor: Color,
				  selectedBackground: Color, textColor: Color = Color.textBlack,
				  val displayFunction: DisplayFunction[A] = DisplayFunction.raw, initialContent: Vector[A] = Vector(),
				  val maximumOptimalWidth: Option[Int] = None) extends Selectable[Option[A], Vector[A]]
	with Refreshable[Vector[A]] with JWrapper with CachingStackable
{
	// ATTRIBUTES	-------------------
	
	private val field = new JComboBox[String]()
	private var _content = initialContent
	private var _displayValues = Vector[LocalizedString]()
	private var isShowingSelectOption = true
	
	
	// INITIAL CODE	-------------------
	
	field.setFont(font.toAwt)
	field.setEditable(false)
	field.setMaximumRowCount(10)
	field.setForeground(textColor.toAwt)
	
	// Modifies the renderer
	field.setRenderer(new CellRenrerer(margins.width.optimal, backgroundColor, selectedBackground, textColor))
	
	{
		val popup = field.getUI.getAccessibleChild(field, 0)
		popup match
		{
			case p: ComboPopup =>
				val jlist = p.getList
				// jlist.setFixedCellHeight(h + margins.height.optimal * 2)
				// jlist.setVisibleRowCount(10)
				jlist.setSelectionBackground(selectedBackground.toAwt)
				jlist.setForeground(textColor.toAwt)
				jlist.setBackground(backgroundColor.toAwt)
				jlist.setSelectionForeground(textColor.toAwt)
			case _ =>
		}
	}
	
	content = initialContent
	field.addActionListener(new UserSelectionListener())
	
	
	// COMPUTED	-----------------------
	
	/**
	  * @return The currently selected index where 0 is the first item
	  */
	def selectedIndex =
	{
		val index = field.getSelectedIndex
		// Index 0 in field is a plaeholder text (sometimes)
		if (index < indexMod) None else Some(index - indexMod)
	}
	def selectedIndex_=(newIndex: Option[Int]): Unit = selectedIndex_=(newIndex getOrElse -1)
	def selectedIndex_=(newIndex: Int) =
	{
		// Index 0 in field sometimes represents the placeholder value (not selected)
		val trueIndex = -1 max ((-1 max newIndex) + indexMod) min (this.count - 1)
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
	
	private def indexMod = if (isShowingSelectOption) 1 else 0
	
	
	// IMPLEMENTED	-------------------
	
	// Only accepts values that are within selection pool
	// override def value_=(newValue: Option[A]) = super.value_=(newValue.filter(content.contains))
	
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
				
				// May limit text width optimal (also adds width for drop down icon)
				val finalW = (maximumOptimalWidth.map { max => if (textW.optimal > max) textW.withOptimal(max) else
					textW } getOrElse textW) + 24
				
				StackSize(finalW, textH)
				
		} getOrElse (220.any x 32.any)
	}
	
	override def updateLayout() = component.revalidate()
	
	override def setValueNoEvents(newValue: Option[A]) = selectedIndex = newValue.flatMap(content.optionIndexOf)
	
	override def value = selectedIndex.flatMap(content.getOption)
	
	override def content = _content
	
	override def content_=(newContent: Vector[A]) =
	{
		// Preserves selection
		val oldSelected = selected
		
		_content = newContent
		_displayValues = newContent.map(displayFunction.apply)
		
		// If there is only 1 item available or if previously selected item is still available, auto-selects it afterwards
		val newSelectedIndex =
		{
			if (_content.size == 1)
				Some(0)
			else
				oldSelected.flatMap(_content.optionIndexOf)
		}
		
		// Updates the field (leaves out "select") if there is an item selected or if there are no values available
		isShowingSelectOption = newSelectedIndex.isEmpty && _content.nonEmpty
		val finalDisplayOptions = if (isShowingSelectOption) selectText +: _displayValues else _displayValues
		
		field.removeAllItems()
		finalDisplayOptions.foreach { s => field.addItem(s.string) }
		
		// If there is only 1 item available, auto-selects it afterwards
		selectedIndex = newSelectedIndex
		
		// Informs listeners if selection changes
		val newSelected = selected
		if (oldSelected.isDefined && newSelected.isEmpty)
			informListeners(newSelected)
		
		revalidate()
	}
	
	
	// NESTED CLASSES	---------------
	
	private class UserSelectionListener extends ActionListener
	{
		// ATTRIBUTES	---------------
		
		private var lastSelected = selected
		
		
		// IMPLEMENTED	---------------
		
		override def actionPerformed(e: ActionEvent) =
		{
			val newSelected = selected
			if (lastSelected != newSelected)
			{
				lastSelected = newSelected
				informListeners(newSelected)
				
				// Once an item has been selected, makes sure that "select" option is no longer shown
				if (isShowingSelectOption && newSelected.isDefined)
					content = _content
			}
		}
	}
	
	private class CellRenrerer(hmargin: Int, val defaultBackground: Color, val selectedBackground: Color,
								  val textColor: Color) extends Label with ListCellRenderer[String]
	{
		// INITIAL CODE	---------------------
		
		setBorder(Border(Insets.symmetric(hmargin, 0), None))
		
		
		// IMPLEMENTED	---------------------
		
		override def getListCellRendererComponent(list: JList[_ <: String], value: String, index: Int, isSelected: Boolean, cellHasFocus: Boolean) =
		{
			if (value != null)
				label.setText(value)
			
			// check if this cell is selected
			if (isSelected)
				background = selectedBackground
			// unselected, and not the DnD drop location
			else
				background = defaultBackground
			
			label.setForeground((if (isShowingSelectOption && index == 0) textColor.timesAlpha(0.625) else textColor).toAwt)
			label
		}
	}
}