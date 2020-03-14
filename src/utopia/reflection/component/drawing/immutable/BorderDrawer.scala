package utopia.reflection.component.drawing.immutable

import utopia.genesis.shape.shape2D.{Bounds, Size}
import utopia.genesis.util.Drawer
import utopia.reflection.component.drawing.template.DrawLevel.{Foreground, Normal}
import utopia.reflection.component.drawing.template.CustomDrawer
import utopia.reflection.shape.{Border, Insets}

import scala.collection.immutable.VectorBuilder

/**
  * This custom drawer draws a set of borders inside the component without affecting component layout
  * @author Mikko Hilpinen
  * @since 5.5.2019, v1+
  */
class BorderDrawer(val border: Border, isAboveContent: Boolean = true) extends CustomDrawer
{
	// ATTRIBUTES	------------------
	
	override val drawLevel = if (isAboveContent) Foreground else Normal
	
	
	// IMPLEMENTED	------------------
	
	override def draw(drawer: Drawer, bounds: Bounds) =
	{
		// Draws the border (recursively)
		drawBorder(drawer, bounds, border)
	}
	
	
	// OTHER	----------------------
	
	private def drawBorder(drawer: Drawer, bounds: Bounds, border: Border): Unit =
	{
		if (bounds.width > 0 && bounds.height > 0)
		{
			// Sets the color & draws the borders
			if (border.color.isDefined)
			{
				val d = drawer.withColor(border.color.get, border.color.get)
				boundsFromInsets(bounds, border.insets).foreach(d.draw)
			}
			
			// Moves to the inner border
			border.inner.foreach
			{ b2 => drawBorder(drawer, boundsInsideInsets(bounds, border.insets), b2) }
		}
	}
	
	private def boundsFromInsets(bounds: Bounds, insets: Insets) =
	{
		val buffer = new VectorBuilder[Bounds]()
		
		if (insets.top > 0)
			buffer += Bounds(bounds.position + (insets.left, 0), Size(bounds.width - (insets.left + insets.right), insets.top))
		if (insets.bottom > 0)
			buffer += Bounds(bounds.bottomLeft + (insets.left, -insets.bottom), Size(bounds.width - (insets.left + insets.right), insets.bottom))
		if (insets.left > 0)
			buffer += Bounds(bounds.position, Size(insets.left, bounds.height))
		if (insets.right > 0)
			buffer += Bounds(bounds.topRight - (insets.right, 0), Size(insets.right, bounds.height))
		
		buffer.result()
	}
	
	private def boundsInsideInsets(original: Bounds, insets: Insets) =
		Bounds(original.position + (insets.left, insets.top), original.size - insets.total)
}
