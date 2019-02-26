package utopia.reflection.container

import utopia.reflection.component.JWrapper
import utopia.reflection.component.Stackable
import utopia.genesis.shape.Axis2D
import utopia.reflection.shape.StackLength
import utopia.genesis.shape.shape2D.Point
import utopia.genesis.shape.shape2D.Size
import utopia.flow.datastructure.mutable.Lazy
import utopia.reflection.component.Area

/**
* A stack holds multiple stackable components in a stack-like manner either horizontally or vertically
* @author Mikko Hilpinen
* @since 25.2.2019
**/
class Stack(val direction: Axis2D, val layout: StackLayout, val margin: StackLength, 
        val cap: StackLength) extends Stackable with JWrapper
{
	// ATTRIBUTES    --------------------
    
    private val panel = new Panel()
    private var _components = Vector[Stackable]()
    
    def components = _components
    
    
    // IMPLEMENTED    -------------------
    
    def component = panel.component
    
    def stackSize = ???
    
    // TODO: Continue
}

private class CacheStackable(val source: Stackable) extends Area
{
    // ATTRIBUTES    -------------------
    
    private var nextPosition: Option[Point] = None
    private var nextSize: Option[Size] = None
    
    private val sizes = new Lazy(() => source.stackSize)
    
    
    // IMPLEMENTED    -----------------
    
    def position = nextPosition getOrElse source.position
    def position_=(p: Point) = nextPosition = Some(p)
    
    def size = nextSize getOrElse source.size
    def size_=(s: Size) = nextSize = Some(s)
    
    
    // COMPUTED    --------------------
    
    def component = source.component
    
    def stackSize = sizes.get
    
    
    // OTHER    -----------------------
    
    def updateBounds() = 
    {
        nextPosition filterNot { _ ~== source.position } foreach { source.position = _ }
        nextPosition = None
        
        nextSize filterNot { _ ~== source.size } foreach { source.size = _ }
        nextSize = None
    }
    
    def resetStackSize() = sizes.reset()
}