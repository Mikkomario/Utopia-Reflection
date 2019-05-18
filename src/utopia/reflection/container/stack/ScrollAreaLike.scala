package utopia.reflection.container.stack

import java.awt.event.KeyEvent
import java.time.{Duration, Instant}

import utopia.flow.util.CollectionExtensions._
import utopia.flow.util.TimeExtensions._
import utopia.genesis.event._
import utopia.genesis.handling.mutable.ActorHandler
import utopia.genesis.handling.{Actor, KeyStateListener, MouseButtonStateListener, MouseMoveListener, MouseWheelListener}
import utopia.genesis.shape.Axis._
import utopia.genesis.shape.{Axis2D, Vector3D, VectorLike}
import utopia.genesis.shape.shape2D.{Bounds, Point, Size}
import utopia.inception.handling.immutable.Handleable
import utopia.reflection.component.drawing.CustomDrawer
import utopia.reflection.component.drawing.DrawLevel.Foreground
import utopia.reflection.component.stack.{CachingStackable, Stackable}
import utopia.reflection.shape.{StackLengthLimit, StackSize}
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
	
	private var barBounds: Map[Axis2D, ScrollBarBounds] = HashMap()
	
	
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
	
	/**
	  * @return Whether this scroll view allows 2-dimensional scrolling
	  */
	def allows2DScrolling = Axis2D.values.forall(axes.contains)
	
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
					axis -> (if (limitsToContentSize) limited else if (limited.max.isDefined) limited else limited.noMax).withLowPriority.noMin
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
		val lengths: Map[Axis2D, Double] = Axis2D.values.map
		{
			axis =>
				if (axes.contains(axis))
					axis -> contentSize.along(axis).optimal.toDouble
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
	  * @param abovePercents The portion of the content that should be above this view [0, 1]
	  */
	def scrollTo(abovePercents: VectorLike[_]) = contentOrigin = -contentSize.toPoint * abovePercents
	/**
	  * Scrolls to a specific percentage on a single axis
	  * @param abovePercent The portion of the content that should be above this view [0, 1]
	  * @param axis The axis on which the scrolling is applied
	  */
	def scrollTo(abovePercent: Double, axis: Axis2D) = contentOrigin =
		contentOrigin.withCoordinate(-contentSize.along(axis) * abovePercent, axis)
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
		addKeyStateListener(listener)
		actorHandler += listener
	}
	
	private def updateScrollBarBounds() =
	{
		if (contentSize.area == 0)
		{
			if (barBounds.nonEmpty)
				barBounds = HashMap()
		}
		else
		{
			barBounds = axes.map
			{
				axis =>
					// Calculates the size of the scroll area
					val barAreaSize = axis match
					{
						case X => Size(width, scrollBarWidth)
						case Y => Size(scrollBarWidth, height)
					}
					
					val length = lengthAlong(axis)
					val contentLength = content.lengthAlong(axis)
					val myBreadth = lengthAlong(axis.perpendicular)
					
					// Calculates scroll bar size
					val barLengthMod = (length / contentLength) min 1.0
					val barSize = barAreaSize * (barLengthMod, axis)
					
					// Calculates the positions of scroll bar area + bar itself
					val barAreaPosition = Point(if (scrollBarIsInsideContent) myBreadth - scrollBarWidth else
						myBreadth, 0, axis.perpendicular)
					
					axis -> ScrollBarBounds(Bounds(barAreaPosition + (barAreaSize.along(axis) * scrollPercents.along(axis),
						axis), barSize), Bounds(barAreaPosition, barAreaSize))
			}.toMap
			
			repaint(Bounds.around(barBounds.values.map { _.area }))
		}
	}
	
	
	// NESTED CLASSES	-----------------------
	
	private class MouseListener(val scrollPerWheelClick: Double, val dragDuration: Duration, val friction: Double,
								val velocityMod: Double) extends MouseButtonStateListener with MouseMoveListener
		with MouseWheelListener with Handleable with Actor with KeyStateListener
	{
		// ATTRIBUTES	-----------------------
		
		private var isDraggingBar = false
		private var barDragPosition = Point.origin
		private var barDragAxis: Axis2D = X
		
		private var isDraggingContent = false
		private var contentDragPosition = Point.origin
		
		private var velocities = Vector[(Instant, Vector3D, Duration)]()
		private var currentVelocity = Vector3D.zero
		
		private var keyState = KeyStatus.empty
		
		
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
				val barUnderEvent = axes.findMap { axis => barBounds.get(axis).filter {
					b => event.isOverArea(b.bar) }.map { axis -> _.bar } }
				
				if (barUnderEvent.isDefined)
				{
					isDraggingContent = false
					barDragAxis = barUnderEvent.get._1
					barDragPosition = event.positionOverArea(barUnderEvent.get._2)
					isDraggingBar = true
					currentVelocity = Vector3D.zero
				}
				// if outside, starts drag scrolling
				else if (event.isOverArea(bounds))
				{
					isDraggingBar = false
					contentDragPosition = event.mousePosition
					isDraggingContent = true
					currentVelocity = Vector3D.zero
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
						val averageVelocity = velocityData.map { v => v._2 * v._3.toPreciseMillis }.reduce { _ + _ } /
							actualDragDutationMillis
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
				scrollTo(newBarOrigin.along(barDragAxis) / lengthAlong(barDragAxis), barDragAxis)
			}
			// If dragging content, updates scrolling and remembers velocity
			else if (isDraggingContent)
			{
				// Drag scrolling is different when both axes are being scrolled
				if (allows2DScrolling)
					scroll(event.transition)
				else
					axes.foreach { axis => scroll(event.transition.projectedOver(axis)) }
				
				val now = Instant.now
				velocities = velocities.dropWhile { _._1 < now - dragDuration } :+ (now, event.velocity, event.duration)
			}
		}
		
		// When wheel is rotated inside component bounds, scrolls
		override def onMouseWheelRotated(event: MouseWheelEvent) =
		{
			// in 2D scroll views, X-scrolling is applied only if shift is being held
			val scrollAxis =
			{
				if (allows2DScrolling)
				{
					if (keyState(KeyEvent.VK_SHIFT))
						X
					else
						Y
				}
				else
					axes.headOption getOrElse Y
			}
			
			scroll(scrollAxis(-event.wheelTurn * scrollPerWheelClick))
			true
		}
		
		override def act(duration: Duration) =
		{
			if (currentVelocity != Vector3D.zero)
			{
				// Applies velocity
				if (allows2DScrolling)
					scroll(currentVelocity * duration.toPreciseMillis)
				else
					axes.foreach { axis => scroll(currentVelocity.projectedOver(axis) * duration.toPreciseMillis) }
				
				// Applies friction to velocity
				if (currentVelocity.length <= friction)
					currentVelocity = Vector3D.zero
				else
					currentVelocity -= friction
			}
		}
		
		override def onKeyState(event: KeyStateEvent) = keyState = event.keyStatus
	}
}
