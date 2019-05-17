package utopia.reflection.container.stack

import java.time.{Duration, Instant}

import utopia.flow.util.TimeExtensions._
import utopia.genesis.event._
import utopia.genesis.handling.mutable.ActorHandler
import utopia.genesis.handling.{Actor, MouseButtonStateListener, MouseMoveListener, MouseWheelListener}
import utopia.genesis.shape.Axis._
import utopia.genesis.shape.{Axis2D, VectorLike}
import utopia.genesis.shape.shape2D.{Bounds, Point, Size}
import utopia.inception.handling.immutable.Handleable
import utopia.reflection.component.drawing.CustomDrawer
import utopia.reflection.component.drawing.DrawLevel.Foreground
import utopia.reflection.component.stack.{CachingStackable, Stackable}
import utopia.reflection.shape.{StackLength, StackLengthLimit, StackSize}
import utopia.reflection.util.ScrollBarBounds

import scala.collection.immutable.HashMap

/**
  * Scroll areas are containers that allow horizontal and / or vertical content scrolling
  * @author Mikko Hilpinen
  * @since 15.5.2019, v1+
  */
trait ScrollAreaLike extends CachingStackable
{
	// ATTRIBUTES	----------------
	
	private var barBounds = HashMap[Axis2D, ScrollBarBounds]()
	
	
	// ABSTRACT	--------------------
	
	/**
	  * @return The content displayed in this scroll view
	  */
	def content: Stackable
	/**
	  * @return The scrolling axis / axes of this scroll view
	  */
	def axes: Seq[Axis2D]
	/**
	  * @return Limits applied to this area's stack lengths
	  */
	def lengthLimits: Map[Axis2D, StackLengthLimit]
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
	  * @return The size of this view's contents
	  */
	def contentSize = content.size
	/**
	  * @return The current position of this view's contents (negative)
	  */
	def contentOrigin = content.position
	def contentOrigin_=(pos: Point) =
	{
		content.position = minContentOrigin.bottomRight(pos).topLeft(Point.origin)
		updateScrollBarBounds()
	}
	
	/**
	  * @return The smallest possible content position (= position when scrolled at bottom right corner)
	  */
	def minContentOrigin = (size - contentSize).toPoint
	
	/**
	  * @return The current scroll modifier / percentage [0, 1]
	  */
	def scrollPercents = -contentOrigin / contentSize
	def scrollPercents_=(newPercents: VectorLike[_]) = scrollTo(newPercents)
	
	/**
	  * @return The currently visible area inside the content
	  */
	def visibleContentArea = Bounds(-content.position, size - scrollBarContentOverlap)
	
	private def scrollBarContentOverlap =
	{
		if (scrollBarIsInsideContent)
			axes.map { Size(0, scrollBarWidth, _) } reduceOption { _ + _ } getOrElse Size.zero
		else
			Size.zero
	}
	
	
	// IMPLEMENTED	----------------
	
	override protected def calculatedStackSize =
	{
		val contentSize = content.stackSize
		val lengths = Axis2D.values.map
		{
			axis =>
				// Handles scrollable & non-scrollable axes differently
				if (axes.contains(axis))
				{
					// Uses content size but may limit it in process
					val raw = contentSize.along(axis)
					val limit = lengthLimits.get(axis)
					val limited = limit.map(raw.within) getOrElse raw
					axis -> (if (limitsToContentSize) limited else if (limit.max.isDefined) limited else limited.noMax).withLowPriority
				}
				else
					axis -> contentSize.along(axis)
		}.toMap
		
		StackSize(lengths(X), lengths(Y))
	}
	
	// Updates content size & position
	override def updateLayout() =
	{
		// Non-scrollable content side is dependent from this component's side while scrollable side(s) are always set to optimal
		val contentSize = content.stackSize
		val lengths = Axis2D.values.map
		{
			axis =>
				if (axes.contains(axis))
					axis -> contentSize.along(axis).optimal
				else
					axis -> lengthAlong(axis)
		}.toMap
		
		content.size = Size(lengths(X), lengths(Y))
		
		// May scroll on content size change
		if (content.x + content.width < width)
			content.x = width - content.width
		if (content.y + content.height < height)
			content.y = height - content.height
		
		updateScrollBarBounds()
	}
	
	
	// OTHER	----------------------
	
	/**
	  * Scrolls to a specific percentage
	  * @param abovePercents The portion of the content that should be above the view [0, 1]
	  */
	def scrollTo(abovePercents: VectorLike[_]) = contentOrigin = -contentSize.toPoint * abovePercents
	/**
	  * Scrolls this view a certain amount
	  * @param amounts The scroll vector
	  */
	def scroll(amounts: VectorLike[_]) = contentOrigin += amounts
	
	/**
	  * Makes sure the specified area is (fully) visible in this scroll view
	  * @param area The target area
	  */
	def ensureAreaIsVisible(area: Bounds) =
	{
		val areaWithinContent = area + contentOrigin
		
		if (areaWithinContent.x < 0)
			content.x = -area.position.x
		else if (areaWithinContent.rightX > width)
			content.x = width - areaWithinContent.rightX
		
		if (areaWithinContent.y < 0)
			content.y = -area.position.y
		else if (areaWithinContent.bottomY > height)
			content.y = height - areaWithinContent.bottomY
	}
	
	/**
	  * Converts a scroll bar drawer to a custom drawer, which should then be added to this view
	  * @param barDrawer A scroll bar drawer
	  * @return A custom drawer based on the scroll bar drawer
	  */
	protected def scrollBarDrawerToCustomDrawer(barDrawer: ScrollBarDrawer) = CustomDrawer(Foreground,
		(d, _) => Axis2D.values.foreach
		{
			axis =>
				if (!scrollBarIsInsideContent || lengthAlong(axis) < contentSize.along(axis))
					barBounds.get(axis).foreach { barDrawer.draw(d, _, axis) }
		})
		
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
		override def mouseButtonStateEventFilter = Consumable.notConsumedFilter &&
			MouseButtonStateEvent.buttonFilter(MouseButton.Left)
		
		// Only listens to wheel events inside component bounds
		override def mouseWheelEventFilter = Consumable.notConsumedFilter && MouseEvent.isOverAreaFilter(bounds)
		
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
		override def onMouseWheelRotated(event: MouseWheelEvent) =
		{
			scroll(-event.wheelTurn * scrollPerWheelClick)
			true
		}
		
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
