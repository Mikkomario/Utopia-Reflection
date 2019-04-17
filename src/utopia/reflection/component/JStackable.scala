package utopia.reflection.component

import javax.swing.JComponent
import utopia.reflection.shape.StackSize

object JStackable
{
    /**
     * Wraps a component as stackable
     * @param component wrapped component
     * @param getSize a function for retrieving component size
     */
    def apply(component: JComponent, getSize: () => StackSize, update: () => Unit = () => Unit,
              children: Set[Wrapper] = Set()): JStackable = new JStackWrapper(component, children, getSize, update)
    
    /**
     * Wraps a component as stackable
     * @param component wrapped component
     * @param size fixed component sizes
     */
    def apply(component: JComponent, size: StackSize): JStackable = apply(component, () => size)
}

/**
* This trait combines JWrapper and Stackable traits
* @author Mikko Hilpinen
* @since 27.3.2019
**/
trait JStackable extends Stackable with JWrapper

private class JStackWrapper(val component: JComponent, val children: Set[Wrapper] = Set(),
                            val getSize: () => StackSize, val update: () => Unit) extends JStackable
{
    def calculatedStackSize = getSize()
    
    override def updateLayout() = update()
}