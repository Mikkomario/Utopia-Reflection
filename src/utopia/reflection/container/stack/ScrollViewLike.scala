package utopia.reflection.container.stack

import utopia.genesis.event.{MouseButton, MouseButtonStateEvent, MouseEvent, MouseMoveEvent, MouseWheelEvent}
import utopia.genesis.handling.{MouseButtonStateListener, MouseMoveListener, MouseWheelListener}
import utopia.genesis.shape.{Axis2D, X, Y}
import utopia.genesis.shape.shape2D.{Bounds, Point, Size}
import utopia.inception.handling.immutable.Handleable
import utopia.reflection.component.DrawLevel.Foreground
import utopia.reflection.component.{CachingStackable, CustomDrawer, Stackable}
import utopia.reflection.shape.{StackLength, StackSize}

/**
  * Scroll views are containers that allow horizontal or vertical content scrolling
  * @author Mikko Hilpinen
  * @since 30.4.2019, v1+
  */
trait ScrollViewLike extends CachingStackable
{
	// ATTRIBUTES	----------------
	
	private var scrollBarAreaBounds = Bounds.zero
	private var scrollBarBounds = Bounds.zero
	
	
	// ABSTRACT	--------------------
	
	def content: Stackable
	def axis: Axis2D
	
	def minLength: Int
	def minOptimalLength: Option[Int]
	def maxOptimalLength: Option[Int]
	def maxLength: Option[Int]
	def limitsToContentSize: Boolean
	
	def scrollBarIsInsideContent: Boolean
	def scrollBarWidth: Int
	
	// TODO: Actual implementation needs a resize listener
	
	def repaint(bounds: Bounds): Unit
	
	// TODO: Add drag scroll
	
	
	// COMPUTED	--------------------
	
	def length = lengthAlong(axis)
	
	def breadth = lengthAlong(axis.perpendicular)
	
	def contentSize = content.size
	
	def contentLength = contentSize.along(axis)
	
	def contentBreadth = contentSize.perpendicularTo(axis)
	
	def contentPosition = content.position.along(axis)
	def contentPosition_=(pos: Double) =
	{
		content.setCoordinate(minContentPosition max pos min 0, axis)
		updateScrollBarBounds()
	}
	
	def minContentPosition = length - contentLength
	
	def scrollPercent = -contentPosition / contentLength
	def scrollPercent_=(newPercent: Double) = scrollTo(newPercent)
	
	def isAtTop = contentPosition >= 0
	def isAtBottom = contentPosition + contentLength <= length
	
	def visibleContentArea = Bounds(-content.position, size - scrollBarContentOverlap)
	
	private def scrollBarContentOverlap = if (scrollBarIsInsideContent) Size(scrollBarWidth, 0, axis.perpendicular) else Size.zero
	
	
	// IMPLEMENTED	----------------
	
	override protected def calculatedStackSize =
	{
		def contentSize = content.stackSize
		def breadth = contentSize.perpendicularTo(axis)
		def length = contentSize.along(axis)
		
		// Max length may be limited by a) specified value or b) content length
		val max = maxLength.orElse { if (limitsToContentSize) length.max else None }
		
		// Optimal length is based on content optimal, but may be altered by specified limits
		val optimal =
		{
			if (minOptimalLength.exists { length.optimal < _ })
				minOptimalLength.get
			else if (maxOptimalLength.exists { length.optimal > _ })
				maxOptimalLength.get
			else
				length.optimal
		}
		
		// Length is always low priority
		val l = StackLength(minLength, optimal, max, true)
		
		// Scroll bar may affect breadth
		val b = if (scrollBarIsInsideContent) breadth + scrollBarWidth else breadth
		
		StackSize(l, b, axis)
	}
	
	// Updates content size & position
	override def updateLayout() =
	{
		// Content breadth is dependent from this component's breadth while length is always set to optimal
		val contentSize = content.stackSize
		val b = if (scrollBarIsInsideContent) breadth - scrollBarWidth else breadth
		val l = contentSize.along(axis).optimal
		
		content.size = Size(l, b, axis)
		
		// May scroll on content size change
		if (isAtBottom)
			scrollToBottom()
		else if (isAtTop)
			scrollToTop()
		
		updateScrollBarBounds()
	}
	
	
	// OTHER	----------------------
	
	def scrollToTop() = contentPosition = 0
	
	def scrollToBottom() = contentPosition = minContentPosition
	
	def scrollTo(abovePercent: Double) = contentPosition = -contentLength * abovePercent
	
	def scroll(amount: Double) = contentPosition += amount
	
	def ensureAreaIsVisible(area: Bounds) =
	{
		if (contentPosition + area.position.along(axis) < 0)
			contentPosition = -area.position.along(axis)
		else if (contentPosition + area.position.along(axis) + area.size.along(axis) > length)
			contentPosition = length - area.position.along(axis) - area.size.along(axis)
	}
	
	protected def scrollBarDrawerToCustomDrawer(barDrawer: ScrollBarDrawer) = CustomDrawer(Foreground,
		(d, _) => if (scrollBarAreaBounds != Bounds.zero) barDrawer.draw(d, scrollBarAreaBounds, scrollBarBounds, axis))
	
	private def updateScrollBarBounds() =
	{
		if (contentSize.area == 0)
		{
			scrollBarAreaBounds = Bounds.zero
			scrollBarBounds = Bounds.zero
		}
		else
		{
			// Calculates the size of the scroll area
			val barAreaSize = axis match
			{
				case X => Size(width, scrollBarWidth)
				case Y => Size(scrollBarWidth, height)
			}
			
			// Calculates scroll bar size
			val barLengthMod = (length / contentLength) min 1.0
			val barSize = barAreaSize * (barLengthMod, axis)
			
			// Calculates the positions of scroll bar area + bar itself
			val barAreaPosition = Point(if (scrollBarIsInsideContent) contentBreadth - scrollBarWidth else
				contentBreadth, 0, axis.perpendicular)
			
			scrollBarAreaBounds = Bounds(barAreaPosition, barAreaSize)
			scrollBarBounds = Bounds(barAreaPosition + (barAreaSize.along(axis) * scrollPercent, axis), barSize)
			
			repaint(scrollBarAreaBounds)
		}
	}
	
	
	// NESTED CLASSES	-----------------------
	
	private class MouseListener(val scrollPerWheelClick: Double) extends MouseButtonStateListener
		with MouseMoveListener with MouseWheelListener with Handleable
	{
		// ATTRIBUTES	-----------------------
		
		private var isDraggingBar = false
		private var barDragPosition = Point.origin
		
		private var isDraggingContent = false
		private var contentDragPosition = Point.origin
		
		
		// IMPLEMENTED	-----------------------
		
		// Listens to left mouse presses & releases
		override def mouseButtonStateEventFilter = MouseButtonStateEvent.buttonFilter(MouseButton.Left)
		
		// Only listens to wheel events inside component bounds
		override def mouseWheelEventFilter = MouseEvent.isOverAreaFilter(bounds)
		
		override def onMouseButtonState(event: MouseButtonStateEvent) =
		{
			if (event.wasPressed)
			{
				// If mouse was pressed inside inside scroll bar, starts dragging the bar
				val barBounds = scrollBarBounds + position
				if (event.isOverArea(barBounds))
				{
					isDraggingBar = true
					barDragPosition = event.positionOverArea(barBounds)
				}
				// if outside, starts drag scrolling
				else if (event.isOverArea(bounds))
				{
					isDraggingContent = true
					contentDragPosition = event.mousePosition
				}
			}
			else
			{
				// When mouse is released, stops dragging. May apply scrolling velocity
				isDraggingBar = false
				if (isDraggingContent)
				{
					isDraggingContent = false
					// TODO: Handle scrolling velocity
				}
			}
			false
		}
		
		// TODO: Apply scrolling velocity & friction
		
		override def onMouseMove(event: MouseMoveEvent) =
		{
			// If dragging scroll bar, scrolls the content
			if (isDraggingBar)
			{
				val newBarOrigin = event.positionOverArea(scrollBarBounds + position) - barDragPosition
				scrollTo(newBarOrigin.along(axis) / length)
			}
			// If dragging content, updates scrolling and remembers velocity
			else if (isDraggingContent)
			{
				scroll(event.transition.along(axis))
				// TODO: Handle velocity tracking
			}
		}
		
		// When wheel is rotated inside component bounds, scrolls
		override def onMouseWheelRotated(event: MouseWheelEvent) = scroll(event.wheelTurn * scrollPerWheelClick)
	}
}
