package utopia.reflection.container.stack

import utopia.reflection.component.Stackable
import utopia.reflection.container.Container

/**
  * This is a common trait for containers with stackable
  * @author Mikko Hilpinen
  * @since 21.4.2019, v1+
  */
trait StackContainer[C <: Stackable] extends Container[C] with Stackable