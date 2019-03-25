package utopia.reflection.container

import utopia.reflection.component.Wrapper

/**
* This is a common wrapper for all window implementations
* @author Mikko Hilpinen
* @since 25.3.2019
**/
trait Window extends Wrapper
{
	// ABSTRACT    -----------------
    
    override def component: java.awt.Window
    
}