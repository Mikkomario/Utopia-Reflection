package utopia.reflection.component.swing.label

import javax.swing.ImageIcon
import utopia.genesis.shape.shape2D.Size
import utopia.reflection.component.stack.CachingStackable
import utopia.reflection.shape.StackSize

/**
  * This label displays an icon
  * @author Mikko Hilpinen
  * @since 8.5.2019, v1+
  * @param initialIcon An icon that will be placed incide this label (default = None)
  * @param margins The margins that will be placed around the icon, which also determine the maximum size of this label
  *                (default = fixed to 0)
  */
class IconLabel(initialIcon: Option[ImageIcon] = None, val margins: StackSize = StackSize.fixed(Size.zero)) extends Label with CachingStackable
{
	// ATTRIBUTES	------------------
	
	private var _icon = initialIcon
	
	
	// COMPUTED	----------------------
	
	/**
	  * @return The size of the current icon. 0x0 if this label doesn't have an icon
	  */
	def iconSize = _icon.map { i => Size(i.getIconWidth, i.getIconHeight) } getOrElse Size.zero
	
	/**
	  * @return The current icon displayed on this label
	  */
	def icon = _icon
	def icon_=(newIcon: Option[ImageIcon]) =
	{
		if (newIcon != _icon)
		{
			_icon = newIcon
			revalidate()
		}
	}
	def icon_=(newIcon: ImageIcon): Unit = icon = Some(newIcon)
	
	
	// IMPLEMENTED	------------------
	
	override protected def updateVisibility(visible: Boolean) = super[Label].isVisible_=(visible)
	
	override protected def calculatedStackSize = if (_icon.isDefined) margins + iconSize else StackSize.any
	
	override def updateLayout() = Unit
}
