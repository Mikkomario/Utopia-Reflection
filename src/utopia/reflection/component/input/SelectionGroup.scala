package utopia.reflection.component.input

/**
  * Keeps track of multiple selectable items and deselects others when one becomes selected
  * @author Mikko Hilpinen
  * @since 2.8.2019, v1+
  */
case class SelectionGroup[A <: InteractionWithPointer[Boolean]](options: Set[A])
{
	// ATTRIBUTES	--------------------
	
	private var lastSelected = options.find { _.value }
	
	
	// INITIAL CODE	--------------------
	
	// Deselects other options
	options.foreach { o => if (o.value && !lastSelected.contains(o)) o.value = false }
	
	// Adds listening
	options.foreach { _.addValueListener { e => if (e.newValue) updateSelection() else lastSelected = None } }
	
	
	// OTHER	------------------------
	
	private def updateSelection() =
	{
		// Changes selection to newly selected item, if one is found
		val newSelected = options.find { o => o.value && !lastSelected.contains(o) }
		lastSelected.foreach { _.value = false }
		lastSelected = newSelected
	}
}
