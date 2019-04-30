package utopia.reflection.container.stack

import utopia.genesis.shape.Axis2D
import utopia.genesis.shape.shape2D.Bounds
import utopia.genesis.util.Drawer

/**
  * These drawers draw scroll bars
  * @author Mikko Hilpinen
  * @since 30.4.2019, v1+
  */
trait ScrollBarDrawer
{
	def draw(drawer: Drawer, areaBounds: Bounds, barBounds: Bounds, barDirection: Axis2D): Unit
}