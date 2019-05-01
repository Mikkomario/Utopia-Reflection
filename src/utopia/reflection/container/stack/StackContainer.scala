package utopia.reflection.container.stack

import utopia.reflection.component.ComponentLike
import utopia.reflection.component.stack.Stackable
import utopia.reflection.container.Container

/**
  * This is a common trait for containers with stackable
  * @author Mikko Hilpinen
  * @since 21.4.2019, v1+
  */
trait StackContainer[C <: ComponentLike] extends Container[C] with Stackable
