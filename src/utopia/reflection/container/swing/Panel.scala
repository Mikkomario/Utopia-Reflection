package utopia.reflection.container.swing

import javax.swing.{JPanel, SwingUtilities}
import utopia.reflection.component.ComponentLike
import utopia.reflection.component.swing.{AwtComponentRelated, JWrapper}
import utopia.reflection.container.MultiContainer

/**
* Panel is the standard container that holds other components in it (based on JPanel)
* @author Mikko Hilpinen
* @since 25.2.2019
**/
class Panel[C <: ComponentLike with AwtComponentRelated] extends MultiContainer[C] with JWrapper with AwtContainerRelated
{
    // ATTRIBUTES    -------------------
    
	private val panel = new JPanel()
	private var _components = Vector[C]()
	
	
	// INITIAL CODE    -----------------
	
	// Layout manager is disabled, panel is transparent by default
	panel.setLayout(null)
	isTransparent = true
	
	
	// IMPLEMENTED    ------------------
	
	def component = panel
	
	def components = _components
	
	protected def add(component: C) =
	{
	    _components :+= component
		// Adds the component to the underlying panel in GUI thread
		SwingUtilities.invokeLater(() => panel.add(component.component))
	}
	
	protected def remove(component: C) =
	{
	    _components = components filterNot { _.equals(component) }
		// Panel action is done in the GUI thread
	    SwingUtilities.invokeLater(() => panel.remove(component.component))
	}
}