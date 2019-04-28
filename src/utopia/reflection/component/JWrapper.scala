package utopia.reflection.component

import javax.swing.JComponent
import javax.swing.AbstractAction
import java.awt.event.ActionEvent

import javax.swing.KeyStroke
import utopia.genesis.color.Color
import utopia.reflection.shape.StackSize

object JWrapper
{
    /**
     * Wraps a JComponent
     */
    def apply(component: JComponent): JWrapper = new SimpleJWrapper(component)
}

/**
* This is an extended version of Wrapper that allows access for JComponent functions
* @author Mikko Hilpinen
* @since 25.2.2019
**/
trait JWrapper extends AwtComponentWrapper with SwingComponentRelated
{
    override def background_=(color: Color) = super[SwingComponentRelated].background_=(color)
}

private class SimpleJWrapper(val component: JComponent) extends JWrapper