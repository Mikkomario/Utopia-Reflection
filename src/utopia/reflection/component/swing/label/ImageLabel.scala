package utopia.reflection.component.swing.label

import utopia.genesis.image.Image
import utopia.genesis.shape.shape2D.{Bounds, Point}
import utopia.genesis.util.Drawer
import utopia.reflection.component.drawing.CustomDrawer
import utopia.reflection.component.drawing.DrawLevel.Normal
import utopia.reflection.component.stack.CachingStackable
import utopia.reflection.shape.StackSize

/**
  * This label shows an image
  * @author Mikko Hilpinen
  * @since 7.7.2019, v1+
  * @param initialImage The initially displayed image
  * @param alwaysFillArea Whether the whole label area should be filled with the image (default = true)
  * @param allowUpscaling Whether the image should be allowed to scale above its size (default = false)
  * @constructor Creates a new image label with specified settings (always fill area and upscaling allowing)
  */
class ImageLabel(initialImage: Image, val alwaysFillArea: Boolean = true, val allowUpscaling: Boolean = false)
	extends Label with CachingStackable
{
	// ATTRIBUTES	-----------------
	
	private var _image = initialImage
	
	private var scaledImage = initialImage
	private var relativeImagePosition = Point.origin
	
	
	// INITIAL CODE	-----------------
	
	addCustomDrawer(new ImageDrawer)
	addResizeListener(updateLayout())
	
	
	// COMPUTED	---------------------
	
	/**
	  * @return The currently displayed image in this label
	  */
	def image = _image
	/**
	  * Updates displayed image
	  * @param newImage The new image to be displayed in this label
	  */
	def image_=(newImage: Image) =
	{
		_image = newImage
		revalidate()
	}
	
	
	// IMPLEMENTED	-----------------
	
	override protected def updateVisibility(visible: Boolean) = super[Label].isVisible_=(visible)
	
	override def updateLayout() =
	{
		// Updates image scaling to match this label's size
		if (_image.size == size)
			scaledImage = _image
		else if (alwaysFillArea || !_image.size.fitsInto(size))
			scaledImage = _image.withSize(size)
		else
			scaledImage = _image
		
		relativeImagePosition = (size - scaledImage.size).toPoint / 2
	}
	
	override protected def calculatedStackSize =
	{
		// Optimal size is always set to image size
		// Upscaling may also be allowed (limited if upscaling is not allowed and image must fill area)
		val imageSize = _image.size
		val isLimited = alwaysFillArea && !allowUpscaling
		
		if (isLimited)
			StackSize.downscaling(imageSize)
		else
			StackSize.any(imageSize)
	}
	
	
	// NESTED CLASSES	------------
	
	private class ImageDrawer extends CustomDrawer
	{
		override def drawLevel = Normal
		
		override def draw(drawer: Drawer, bounds: Bounds) =
		{
			// Draws the image with prepared settings
			scaledImage.drawWith(drawer, bounds.position + relativeImagePosition)
		}
	}
}
