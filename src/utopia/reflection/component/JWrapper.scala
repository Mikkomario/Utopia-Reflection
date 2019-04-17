package utopia.reflection.component

import javax.swing.JComponent
import java.awt.Color
import javax.swing.AbstractAction
import java.awt.event.ActionEvent
import javax.swing.KeyStroke
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
trait JWrapper extends Wrapper
{
    // ABSTRACT    -----------------------
    
    override def component: JComponent
    
    
    // COMPUTED    -----------------------
    
    def transparent_=(isTransparent: Boolean) = component.setOpaque(!isTransparent)
    
	override def background_=(color: Color) = 
	{
	    component.setBackground(color)
	    transparent = false
	}
    
    /**
     * Adds a keystroke shortcut to the wrapped component
     * @param key the target key code
     * @param action the action performed when the key is pressed
     */
    def addShortcut(key: Int, action: () => Unit) = 
    {
        val actionName = s"action-for-key-$key"
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key, 0), actionName)
        component.getActionMap.put(actionName, new RunAction(action))
    }
    
    
    // OTHER    --------------------------
    
    /**
     * Transforms this wrapper into a Stackable
     */
    override def withStackSize(getSize: () => StackSize) = JStackable(component, getSize)
    
    /**
     * Transforms this wrapper into a Stackable
     */
    override def withStackSize(size: StackSize) = JStackable(component, size)
    
    
    // NESTED CLASSES    -----------------
    
    private class RunAction(val action: () => Unit) extends AbstractAction
    {
        def actionPerformed(e: ActionEvent) = action()
    }
}

private class SimpleJWrapper(val component: JComponent) extends JWrapper