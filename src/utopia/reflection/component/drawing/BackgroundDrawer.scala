package utopia.reflection.component.drawing
import utopia.genesis.color.Color
import utopia.genesis.shape.shape2D.Bounds
import utopia.genesis.util.Drawer
import utopia.reflection.component.drawing.DrawLevel.Background

/**
  * A custom drawer that draws a background for the targeted component
  * @author Mikko Hilpine
  * @since 28.2.2020, v1
  */
class BackgroundDrawer(color: Color) extends CustomDrawer
{
	override val drawLevel = Background
	
	override def draw(drawer: Drawer, bounds: Bounds) = drawer.onlyFill(color).draw(bounds)
}
