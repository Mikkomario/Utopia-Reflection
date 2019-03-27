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
    def apply(component: JComponent, getSize: () => StackSize): JStackable = new JStackWrapper(component, getSize)    
    
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

private class JStackWrapper(val component: JComponent, val getSize: () => StackSize) extends JStackable
{
    def stackSize = getSize()
}