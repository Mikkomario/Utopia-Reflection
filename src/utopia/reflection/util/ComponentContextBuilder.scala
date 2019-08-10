package utopia.reflection.util

import utopia.genesis.color.Color
import utopia.genesis.handling.mutable.ActorHandler
import utopia.genesis.shape.Axis2D
import utopia.genesis.util.Drawer
import utopia.reflection.component.Alignment
import utopia.reflection.container.stack.ScrollBarDrawer
import utopia.reflection.shape.{Border, ScrollBarBounds, StackLength, StackSize}
import utopia.reflection.text.Font

/**
  * Used for configuring component style at creation time
  * @author Mikko Hilpinen
  * @since 4.8.2019, v1+
  */
case class ComponentContextBuilder(actorHandler: ActorHandler, font: Font, highlightColor: Color, focusColor: Color,
								   normalWidth: Int, textColor: Color = Color.textBlack, promptFont: Option[Font] = None,
								   promptTextColor: Option[Color] = None, textHasMinWidth: Boolean = true,
								   textAlignment: Alignment = Alignment.Left, background: Option[Color] = None,
								   barBackground: Option[Color] = None, insideMargins: StackSize = StackSize.any,
								   border: Option[Border] = None, borderWidth: Option[Double] = None,
								   stackMargin: StackLength = StackLength.any, relatedItemsStackMargin: Option[StackLength] = None,
								   stackCap: StackLength = StackLength.fixed(0), dropDownWidthLimit: Option[Int] = None,
								   switchWidth: Option[StackLength] = None, textFieldWidth: Option[StackLength] = None,
								   scrollPerWheelClick: Double = 32, scrollBarWidth: Double = 24,
								   scrollBarDrawer: Option[ScrollBarDrawer] = None, scrollBarIsInsideContent: Boolean = false,
								   allowImageUpscaling: Boolean = false)
{
	// ATTRIBUTES	-------------------------
	
	lazy val result = ComponentContext(actorHandler, font, highlightColor, focusColor, normalWidth, textColor,
		promptFont.getOrElse(font), promptTextColor.getOrElse(textColor.timesAlpha(0.625)), textHasMinWidth,
		textAlignment, background, barBackground.orElse(background).getOrElse(Color.gray(0.5)), insideMargins,
		border, borderWidth.orElse(border.map { _.insets.average }).getOrElse(
			(insideMargins.optimal.width / 2 min insideMargins.optimal.height / 2) max 4),
		stackMargin, relatedItemsStackMargin.getOrElse(stackMargin), stackCap, dropDownWidthLimit,
		switchWidth.getOrElse(StackLength.any(normalWidth / 4)), textFieldWidth.getOrElse(StackLength.any(normalWidth)),
		scrollPerWheelClick, scrollBarWidth, scrollBarDrawer.getOrElse(new DefaultScrollBarDrawer),
		scrollBarIsInsideContent, allowImageUpscaling)
	
	
	// COMPUTED	-----------------------------
	
	def withTextMinWidth = copy(textHasMinWidth = true)
	
	def withNoTextMinWidth = copy(textHasMinWidth = false)
	
	def transparent = copy(background = None)
	
	def WithNoBorder = copy(border = None)
	
	def withScrollBarInsideContent = copy(scrollBarIsInsideContent = true)
	
	def withScrollBarOutsideContent = copy(scrollBarIsInsideContent = false)
	
	def withImageUpscaling = copy(allowImageUpscaling = true)
	
	def withNoImageUpscaling = copy(allowImageUpscaling = false)
	
	
	// OTHER	-----------------------------
	
	def withFont(font: Font) = copy(font = font)
	
	def withHighlightColor(color: Color) = copy(highlightColor = color)
	
	def withFocusColor(color: Color) = copy(focusColor = color)
	
	def withTextColor(color: Color) = copy(textColor = color)
	
	def withPromptFont(font: Font) = copy(promptFont = Some(font))
	
	def withPromptTextColor(color: Color) = copy(promptTextColor = Some(color))
	
	def withAlignment(alignment: Alignment) = copy(textAlignment = alignment)
	
	def withBackground(background: Color) = copy(background = Some(background))
	
	def withInnerMargins(margins: StackSize) = copy(insideMargins = margins)
	
	def withBorder(border: Border) = copy(border = Some(border))
	
	def withBorderWidth(borderWidth: Int) = copy(borderWidth = Some(borderWidth))
	
	def withStackMargin(margin: StackLength) = copy(stackMargin = margin)
	
	def withStackCap(cap: StackLength) = copy(stackCap = cap)
	
	def withDropDownWidthLimit(widthLimit: Int) = copy(dropDownWidthLimit = Some(widthLimit))
	
	def withSwitchWidth(switchWidth: StackLength) = copy(switchWidth = Some(switchWidth))
	
	def withTextFieldWidth(width: StackLength) = copy(textFieldWidth = Some(width))
	
	def withScrollPerWheelClick(scrollAmount: Double) = copy(scrollPerWheelClick = scrollAmount)
	
	def withScrollBarWidth(barWidth: Double) = copy(scrollBarWidth = barWidth)
	
	def withScrollBarDrawer(drawer: ScrollBarDrawer) = copy(scrollBarDrawer = Some(drawer))
	
	
	// NESTED	-----------------------------
	
	private class DefaultScrollBarDrawer extends ScrollBarDrawer
	{
		override def draw(drawer: Drawer, barBounds: ScrollBarBounds, barDirection: Axis2D) =
		{
			// If scroll bar is inside content, only draws the bar, otherwise draws background as well
			if (scrollBarIsInsideContent)
				drawer.onlyFill(Color.black.withAlpha(0.55)).draw(barBounds.bar.toRoundedRectangle(1))
			else
			{
				drawer.onlyFill(barBackground.getOrElse(Color.gray(0.5))).draw(barBounds.area)
				drawer.onlyFill(highlightColor).draw(barBounds.bar)
			}
		}
	}
}
