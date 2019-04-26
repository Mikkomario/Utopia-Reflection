package utopia.reflection.container

import javax.swing.{JComponent, JPanel, SwingUtilities}
import utopia.reflection.component.JWrapper
import utopia.reflection.component.Wrapper

/**
* Panel is the standard container that holds other components in it (based on JPanel)
* @author Mikko Hilpinen
* @since 25.2.2019
**/
class Panel extends MultiContainer[Wrapper] with JWrapper
{
    // ATTRIBUTES    -------------------
    
	private val panel = new JPanel()
	private var _components = Vector[Wrapper]()
	
	
	// INITIAL CODE    -----------------
	
	// Layout manager is disabled, panel is transparent by default
	panel.setLayout(null)
	isTransparent = true
	
	
	// IMPLEMENTED    ------------------
	
	def component: JComponent = panel
	
	def components = _components
	
	protected def add(component: Wrapper) =
	{
	    _components :+= component
		// Adds the component to the underlying panel in GUI thread
		SwingUtilities.invokeLater(() => panel.add(component.component))
	}
	
	protected def remove(component: Wrapper) =
	{
	    _components = components filterNot { _.equals(component) }
		// Panel action is done in the GUI thread
	    SwingUtilities.invokeLater(() => panel.remove(component.component))
	}
}