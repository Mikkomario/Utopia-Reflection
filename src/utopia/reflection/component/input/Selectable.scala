package utopia.reflection.component.input

import scala.collection.generic.CanBuildFrom

object Selectable
{
	implicit class OptionalSelectable[A, CP <: Traversable[A]](val s: Selectable[Option[A], CP]) extends AnyVal
	{
		/**
		  * @param item The item to be selected
		  * @param generateEvents Whether events should be generated on selection change (default = true)
		  */
		def selectOne(item: A, generateEvents: Boolean = true) = s.select(Some(item), generateEvents)
		
		/**
		  * Selects the first available item
		  * @param generateEvents Whether events should be generated on selection change (default = true)
		  */
		def selectFirst(generateEvents: Boolean = true) = s.select(s.content.headOption, generateEvents)
		
		/**
		  * If there isn't a value selected, selects the first one available
		  * @param generateEvents Whether events should be generated on selection change (default = true)
		  */
		def selectAny(generateEvents: Boolean = true) = if (!s.isDefined) selectFirst(generateEvents)
		
		/**
		  * Clears any selection
		  * @param generateEvents Whether events should be generated on selection change (default = true)
		  */
		def selectNone(generateEvents: Boolean = true) = s.select(None, generateEvents)
	}
	
	implicit class MultiSelectable[A, CS <: Traversable[A], CP <: Traversable[A]](val s: Selectable[CS, CP]) extends AnyVal
	{
		/**
		  * If no item is selected, selects the first item
		  * @param generateEvents Whether selection events should be generated
		  * @param cbf Can build from (implicit)
		  */
		def selectAny(generateEvents: Boolean = true)(implicit cbf: CanBuildFrom[Vector[A], A, CS]) =
			if (!s.isSelected) selectFirst(generateEvents)
		
		/**
		  * Selects the first item in this selectable
		  * @param generateEvents Whether selection events should be generated
		  * @param cbf Can build from (implicit)
		  */
		def selectFirst(generateEvents: Boolean = true)(implicit cbf: CanBuildFrom[Vector[A], A, CS]) =
			s.selected.headOption.foreach { selectOne(_, generateEvents) }
		
		/**
		  * Selects all currently available items
		  * @param generateEvents Whether selection events should be generated
		  * @param cbf Can build from (implicit)
		  */
		def selectAll(generateEvents: Boolean = true)(implicit cbf: CanBuildFrom[CP, A, CS]) = selectMany(s.content, generateEvents)
		
		/**
		  * Selects no items
		  * @param generateEvents Whether selection events should be generated
		  * @param cbf Can build from (implicit)
		  */
		def clearSelection(generateEvents: Boolean = true)(implicit cbf: CanBuildFrom[Vector[A], A, CS]) =
			selectMany(Vector[A](), generateEvents)
		
		/**
		  * Selects exactly one item
		  * @param item Target item
		  * @param generateEvents Whether selection events should be generated
		  * @param cbf Can build from (implicit)
		  */
		def selectOne(item: A, generateEvents: Boolean = true)(implicit cbf: CanBuildFrom[Vector[A], A, CS]) =
			selectMany(Vector(item), generateEvents)
		
		/**
		  * Selects multiple items
		  * @param many Items
		  * @param generateEvents Whether selection events should be generated
		  * @param cbf Can build from (implicit)
		  * @tparam C The type of item collection
		  */
		def selectMany[C <: TraversableOnce[A]](many: C, generateEvents: Boolean = true)(implicit cbf: CanBuildFrom[C, A, CS]) = select(many, generateEvents)
		
		private def select[C <: TraversableOnce[A]](many: C, makeEvents: Boolean)
												   (implicit cbf: CanBuildFrom[C, A, CS]) =
		{
			val builder = cbf(many)
			builder ++= many
			s.select(builder.result(), makeEvents)
		}
	}
}

/**
  * Selectable components are selections that can be interacted with from the program side
  * @author Mikko Hilpinen
  * @since 23.4.2019, v1+
  */
trait Selectable[S, P] extends Selection[S, P] with InteractionWithEvents[S]
{
	// COMPUTED	------------------
	
	/**
	  * Updates the currently selected value, also generating events (same as calling value = ...)
	  * @param newValue The new value for this selection
	  */
	def selected_=(newValue: S) = value = newValue
	
	
	// OTHER	------------------
	
	/**
	  * Updates the currently selected value (same as calling setValue(...))
	  * @param newValue The new value for this selection
	  * @param generateEvents Whether selection events should be generated
	  */
	def select(newValue: S, generateEvents: Boolean = true) = setValue(newValue, generateEvents)
}
