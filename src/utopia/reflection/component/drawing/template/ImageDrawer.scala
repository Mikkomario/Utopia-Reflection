package utopia.reflection.component.drawing.template
import utopia.genesis.image.Image
import utopia.genesis.shape.shape2D.Bounds
import utopia.genesis.util.Drawer
import utopia.reflection.shape.{Alignment, StackInsets}

/**
  * A common trait for image drawer implementations
  * @author Mikko Hilpinen
  * @since 25.3.2020, v1
  */
trait ImageDrawer extends CustomDrawer
{
	// ABSTRACT	----------------------------
	
	/**
	  * @return The image being drawn by this drawer
	  */
	def image: Image
	
	/**
	  * @return Whether the image is allowed to scale up from its normal size when presented with more space
	  *         (the natural resolution of the image is still respected).
	  */
	def useUpscaling: Boolean
	
	/**
	  * @return The insets being placed around the image
	  */
	def insets: StackInsets
	
	/**
	  * @return Alignment that determines the position of the image
	  */
	def alignment: Alignment
	
	
	// IMPLEMENTED	------------------------
	
	override def draw(drawer: Drawer, bounds: Bounds) =
	{
		// Calculates the size of the drawn image
		val defaultSize = image.size + insets.optimal.total
		val imageToDraw =
		{
			// Default case: No upscaling used or required
			if (!useUpscaling || defaultSize.height >= bounds.height || defaultSize.width >= bounds.width)
			{
				// Checks whether downscaling is required
				val minNaturalSize = image.size + insets.min.total
				if (minNaturalSize.height > bounds.height || minNaturalSize.width > bounds.width)
				{
					val availableSize = bounds.size - insets.min.total
					if (availableSize.isPositive)
						Some(image.fitting(bounds.size - insets.min.total))
					else
						None
				}
				else
					Some(image)
			}
			else
			{
				// Case: Upscaling is required (still limited by original image resolution, unless original image is
				// already over source resolution)
				val imageSize = (bounds.size - insets.mapToInsets { l => l.max.getOrElse(l.optimal) }.total) min
					(image.size max image.sourceResolution)
				Some(image.fitting(imageSize))
			}
		}
		
		// Draws the image
		imageToDraw.foreach { img =>
			val position = alignment.position(img.size, bounds, insets, fitWithinBounds = false).position
			img.drawWith(drawer, position)
		}
	}
}
