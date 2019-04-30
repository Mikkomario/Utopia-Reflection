package utopia.reflection.container.stack

import utopia.genesis.color.Color
import utopia.genesis.shape.Axis2D
import utopia.genesis.shape.shape2D.Bounds
import utopia.genesis.util.Drawer

object BoxScrollBarDrawer
{
	/**
	  * Creates a new drawer
	  * @param barColor The color used when drawing the scroll bar
	  * @param backgroundColor The color used for drawing scroll bar area background
	  * @return A new drawer
	  */
	def apply(barColor: Color, backgroundColor: Color): BoxScrollBarDrawer = new BoxScrollBarDrawer(barColor, Some(backgroundColor))
}

/**
  * This drawer draws a scroll bar with simple rectangles
  * @author Mikko Hilpinen
  * @since 30.4.2019, v1+
  */
class BoxScrollBarDrawer(val barColor: Color, val backgroundColor: Option[Color] = None) extends ScrollBarDrawer
{
	override def draw(drawer: Drawer, areaBounds: Bounds, barBounds: Bounds, barDirection: Axis2D) =
	{
		// Fills background and bar
		drawer.noEdges.disposeAfter
		{
			d =>
				backgroundColor.foreach { d.withFillColor(_).draw(areaBounds) }
				d.withFillColor(barColor).draw(barBounds)
		}
	}
}
