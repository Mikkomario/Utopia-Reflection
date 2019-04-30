package utopia.reflection.container.stack

import java.time.{Duration, Instant}

import utopia.flow.util.TimeExtensions._
import utopia.genesis.event.{MouseButton, MouseButtonStateEvent, MouseEvent, MouseMoveEvent, MouseWheelEvent}
import utopia.genesis.handling.mutable.ActorHandler
import utopia.genesis.handling.{Actor, MouseButtonStateListener, MouseMoveListener, MouseWheelListener}
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
	
	/**
	  * @return The content displayed in this scroll view
	  */
	def content: Stackable
	/**
	  * @return The scrolling axis of this scroll view
	  */
	def axis: Axis2D
	
	/**
	  * @return The minimum length of this scroll view
	  */
	def minLength: Int
	/**
	  * @return The smallest optimum length for this scroll view. None if optimal length doesn't have a minimum
	  */
	def minOptimalLength: Option[Int]
	/**
	  * @return The largest optimum length for this scroll view. None if optimal length doesn't have a maximum
	  */
	def maxOptimalLength: Option[Int]
	/**
	  * @return Maximum length of this scroll view
	  */
	def maxLength: Option[Int]
	/**
	  * @return Whether this scroll view's maximum length should be limited to content length
	  */
	def limitsToContentSize: Boolean
	
	/**
	  * @return Whether the scroll bar should be placed over content (true) or besides it (false)
	  */
	def scrollBarIsInsideContent: Boolean
	/**
	  * @return The width of the scroll bar
	  */
	def scrollBarWidth: Int
	
	/**
	  * Repaints this scroll view
	  * @param bounds The area that needs repainting
	  */
	def repaint(bounds: Bounds): Unit
	
	
	// COMPUTED	--------------------
	
	/**
	  * @return The length of this scroll view
	  */
	def length = lengthAlong(axis)
	def length_=(newLength: Double) = setLength(newLength, axis)
	/**
	  * @return The breadth of this scroll view
	  */
	def breadth = lengthAlong(axis.perpendicular)
	def breadth_=(newBreadth: Double) = setLength(newBreadth, axis.perpendicular)
	
	/**
	  * @return The size of this view's contents
	  */
	def contentSize = content.size
	/**
	  * @return The length of this view's contents
	  */
	def contentLength = contentSize.along(axis)
	/**
	  * @return The breadth of this view's contents
	  */
	def contentBreadth = contentSize.perpendicularTo(axis)
	
	/**
	  * @return The current position of this view's contents (negative)
	  */
	def contentPosition = content.position.along(axis)
	def contentPosition_=(pos: Double) =
	{
		content.setCoordinate(minContentPosition max pos min 0, axis)
		updateScrollBarBounds()
	}
	
	/**
	  * @return The smallest possible content position (= position when scrolled at bottom)
	  */
	def minContentPosition = length - contentLength
	
	/**
	  * @return The current scroll modifier / percentage [0, 1]
	  */
	def scrollPercent = -contentPosition / contentLength
	def scrollPercent_=(newPercent: Double) = scrollTo(newPercent)
	
	/**
	  * @return Whether the content is currently scrolled to the top
	  */
	def isAtTop = contentPosition >= 0
	/**
	  * @return Whether the content is currently scrolled to the bottom
	  */
	def isAtBottom = contentPosition + contentLength <= length
	
	/**
	  * @return The currently visible area inside the content
	  */
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
		val b = if (scrollBarIsInsideContent) breadth else breadth + scrollBarWidth
		
		StackSize(l, b, axis)
	}
	
	// Updates content size & position
	override def updateLayout() =
	{
		// Content breadth is dependent from this component's breadth while length is always set to optimal
		val contentSize = content.stackSize
		val b = if (scrollBarIsInsideContent) breadth else breadth - scrollBarWidth
		val l = contentSize.along(axis).optimal
		
		content.size = Size(l, b, axis)
		
		// May scroll on content size change
		if (isAtBottom)
			scrollToBottom()
		else if (isAtTop)
			scrollToTop()
		else
			updateScrollBarBounds()
	}
	
	
	// OTHER	----------------------
	
	/**
	  * Scrolls this scroll view to display content top
	  */
	def scrollToTop() = contentPosition = 0
	/**
	  * Scrolls this scroll view to display content bottom
	  */
	def scrollToBottom() = contentPosition = minContentPosition
	/**
	  * Scrolls to a specific percentage
	  * @param abovePercent The portion of the content that should be above the view [0, 1]
	  */
	def scrollTo(abovePercent: Double) = contentPosition = -contentLength * abovePercent
	/**
	  * Scrolls this view a certain amount
	  * @param amount The amount of pixels scrolled
	  */
	def scroll(amount: Double) = contentPosition += amount
	
	/**
	  * Makes sure the specified area is (fully) visible in this scroll view
	  * @param area The target area
	  */
	def ensureAreaIsVisible(area: Bounds) =
	{
		if (contentPosition + area.position.along(axis) < 0)
			contentPosition = -area.position.along(axis)
		else if (contentPosition + area.position.along(axis) + area.size.along(axis) > length)
			contentPosition = length - area.position.along(axis) - area.size.along(axis)
	}
	
	/**
	  * Converts a scroll bar drawer to a custom drawer, which should then be added to this view
	  * @param barDrawer A scroll bar drawer
	  * @return A custom drawer based on the scroll bar drawer
	  */
	protected def scrollBarDrawerToCustomDrawer(barDrawer: ScrollBarDrawer) = CustomDrawer(Foreground,
		(d, _) => if (scrollBarAreaBounds != Bounds.zero) barDrawer.draw(d, scrollBarAreaBounds, scrollBarBounds, axis))
	
	/**
	  * Sets up mouse handling for this view
	  * @param actorHandler Actor handler that will allow velocity handling
	  * @param scrollPerWheelClick How many pixels should be scrolled at each wheel "click"
	  * @param dragDuration The maximum drag duration when concerning velocity tracking (default = 0.5 seconds)
	  * @param friction Friction applied to velocity (pixels / millisecond, default = 0.1)
	  * @param velocityMod A modifier applied to velocity (default = 1.0)
	  */
	protected def setupMouseHandling(actorHandler: ActorHandler, scrollPerWheelClick: Double,
									 dragDuration: Duration = Duration.ofMillis(300), friction: Double = 0.1,
									 velocityMod: Double = 1.0) =
	{
		val listener = new MouseListener(scrollPerWheelClick, dragDuration, friction, velocityMod)
		
		addMouseButtonListener(listener)
		addMouseMoveListener(listener)
		addMouseWheelListener(listener)
		actorHandler += listener
	}
	
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
	
	private class MouseListener(val scrollPerWheelClick: Double, val dragDuration: Duration, val friction: Double,
								val velocityMod: Double) extends MouseButtonStateListener with MouseMoveListener
		with MouseWheelListener with Handleable with Actor
	{
		// ATTRIBUTES	-----------------------
		
		private var isDraggingBar = false
		private var barDragPosition = Point.origin
		
		private var isDraggingContent = false
		private var contentDragPosition = Point.origin
		
		private var velocities = Vector[(Instant, Double, Duration)]()
		private var currentVelocity = 0.0
		
		
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
					barDragPosition = event.positionOverArea(barBounds)
					isDraggingBar = true
					currentVelocity = 0
				}
				// if outside, starts drag scrolling
				else if (event.isOverArea(bounds))
				{
					contentDragPosition = event.mousePosition
					isDraggingContent = true
					currentVelocity = 0
				}
			}
			else
			{
				// When mouse is released, stops dragging. May apply scrolling velocity
				isDraggingBar = false
				if (isDraggingContent)
				{
					isDraggingContent = false
					
					// Calculates the scrolling velocity
					val now = Instant.now
					val velocityData = velocities.dropWhile { _._1 < now - dragDuration }
					velocities = Vector()
					
					if (velocityData.nonEmpty)
					{
						val actualDragDutationMillis = (now - velocityData.head._1).toPreciseMillis
						val averageVelocity = velocityData.map { v => v._2 * v._3.toPreciseMillis }.sum / actualDragDutationMillis
						currentVelocity += averageVelocity * velocityMod
					}
				}
			}
			false
		}
		
		override def onMouseMove(event: MouseMoveEvent) =
		{
			// If dragging scroll bar, scrolls the content
			if (isDraggingBar)
			{
				val newBarOrigin = event.positionOverArea(bounds) - barDragPosition
				scrollTo(newBarOrigin.along(axis) / length)
			}
			// If dragging content, updates scrolling and remembers velocity
			else if (isDraggingContent)
			{
				scroll(event.transition.along(axis))
				val now = Instant.now
				velocities = velocities.dropWhile { _._1 < now - dragDuration } :+ (now, event.velocity.along(axis),
					event.duration)
			}
		}
		
		// When wheel is rotated inside component bounds, scrolls
		override def onMouseWheelRotated(event: MouseWheelEvent) = scroll(-event.wheelTurn * scrollPerWheelClick)
		
		override def act(duration: Duration) =
		{
			if (currentVelocity != 0)
			{
				// Applies velocity
				scroll(currentVelocity * duration.toPreciseMillis)
				
				// Applies friction to velocity
				if (currentVelocity.abs <= friction)
					currentVelocity = 0
				else if (currentVelocity > 0)
					currentVelocity -= friction
				else
					currentVelocity += friction
			}
		}
	}
}
