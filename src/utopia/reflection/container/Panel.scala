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
class Panel extends Container[Wrapper] with JWrapper
{
    // ATTRIBUTES    -------------------
    
	private val panel = new JPanel()
	private var _components = Vector[Wrapper]()
	
	
	// INITIAL CODE    -----------------
	
	// Layout manager is disabled, panel is transparent by default
	panel.setLayout(null)
	transparent = true
	
	
	// IMPLEMENTED    ------------------
	
	def component: JComponent = panel
	
	def components = _components
	
	def +=(component: Wrapper) = 
	{
	    _components :+= component
	    panel.add(component.component)
	}
	
	def -=(component: Wrapper) = 
	{
	    _components = components filterNot { _.equals(component) }
	    panel.remove(component.component)
	}
}