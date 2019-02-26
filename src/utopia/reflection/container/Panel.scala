package utopia.reflection.container

import javax.swing.JPanel
import utopia.reflection.component.JWrapper
import javax.swing.JComponent
import utopia.reflection.component.Wrapper

/**
* Panel is the standard container that holds other components in it (based on JPanel)
* @author Mikko Hilpinen
* @since 25.2.2019
**/
class Panel extends JWrapper
{
    // ATTRIBUTES    -------------------
    
	private val panel = new JPanel()
	private var _components = Vector[Wrapper]()
	
	/**
	 * The currently stored components inside this panel
	 */
	def components = _components
	
	
	// INITIAL CODE    -----------------
	
	// Layout manager is disabled, panel is transparent by default
	panel.setLayout(null)
	transparent = true
	
	
	// IMPLEMENTED    ------------------
	
	def component: JComponent = panel
	
	
	// OPERATORS    --------------------
	
	/**
	 * Adds a new component to this panel
	 */
	def +=(component: Wrapper) = 
	{
	    _components :+= component
	    panel.add(component.component)
	}
	
	/**
	 * Adds multiple components to this panel
	 */
	def ++=(components: Traversable[Wrapper]) = 
	{
	    _components ++= components
	    components foreach { c => panel.add(c.component) }
	}
	
	/**
	 * Adds multiple components to this panel
	 */
	def ++=(first: Wrapper, second: Wrapper, more: Wrapper*): Unit = ++=(Vector(first, second) ++ more)
	
	/**
	 * Removes a component from this panel
	 */
	def -=(component: Wrapper) = 
	{
	    _components = components filterNot { _.equals(component) }
	    panel.remove(component.component)
	}
	
	/**
	 * Removes multiple components from this panel
	 */
	def --=(components: TraversableOnce[Wrapper]) = components foreach -=
}