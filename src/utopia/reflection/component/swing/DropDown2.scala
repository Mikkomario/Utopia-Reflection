package utopia.reflection.component.swing

import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.genesis.color.Color
import utopia.genesis.handling.mutable.ActorHandler
import utopia.reflection.component.Refreshable
import utopia.reflection.component.drawing.template.CustomDrawer
import utopia.reflection.component.swing.label.TextLabel
import utopia.reflection.container.stack.StackLayout
import utopia.reflection.container.stack.StackLayout.Fit
import utopia.reflection.container.swing.Stack.AwtStackable
import utopia.reflection.localization.{DisplayFunction, LocalizedString}
import utopia.reflection.shape.{Alignment, StackLength, StackSize}
import utopia.reflection.text.{Font, Prompt}

import scala.concurrent.ExecutionContext

/**
  * This component allows the user to choose from a set of pre-existing options via a selection pop-up
  * @author Mikko Hilpinen
  * @since 14.3.2020, v1
  */
class DropDown2[A, C <: AwtStackable with Refreshable[A]]
(override protected val actorHandler: ActorHandler, override protected val noResultsView: AwtStackable,
 selectionDrawer: CustomDrawer, selectionPrompt: Prompt, noContentPrompt: Prompt, defaultFont: Font,
 defaultTextColor: Color = Color.textBlack, textAlignment: Alignment = Alignment.Left,
 displayFunction: DisplayFunction[A] = DisplayFunction.raw,
 textMargin: StackSize = StackSize.any, imageMargin: StackSize = StackSize.any,
 betweenDisplaysMargin: StackLength = StackLength.any, displayStackLayout: StackLayout = Fit,
 override val contentPointer: PointerWithEvents[Vector[A]] = new PointerWithEvents[Vector[A]](Vector()),
 valuePointer: PointerWithEvents[Option[A]] = new PointerWithEvents[Option[A]](None), textHasMinWidth: Boolean = true,
 allowImageUpscaling: Boolean = false, equalsCheck: (A, A) => Boolean = (a: A, b: A) => a == b)
(makeDisplayFunction: A => C)(implicit exc: ExecutionContext)
	extends DropDownFieldLike[A, C](selectionDrawer, betweenDisplaysMargin, displayStackLayout, contentPointer, valuePointer)
{
	// ATTRIBUTES	------------------------------
	
	// initialText: LocalizedString,initialFont: Font,margins: StackSize = StackSize.any,hasMinWidth: Boolean = true,
	// initialAlignment: Alignment = Alignment.Left,initialTextColor: Color = Color.textBlack)
	private val textLabel = new TextLabel(selectionPrompt.text, selectionPrompt.font, textMargin, textHasMinWidth,
		textAlignment, selectionPrompt.color)
	// TODO: Continue once text labels support proper text placement (margin on left)
	
	
	// INITIAL CODE	------------------------------
	
	setup()
	
	
	// IMPLEMENTED	------------------------------
	
	override protected def checkEquals(first: A, second: A) = equalsCheck(first, second)
	
	override protected def makeDisplay(item: A) = makeDisplayFunction(item)
	
	override protected def mainDisplay = ???
}
