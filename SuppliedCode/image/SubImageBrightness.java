package image;

import java.awt.*;

/**
 * computing the average grey-scale brightness of an image
 *in the range [0.0, 1.0].
 */
public final class SubImageBrightness {
	private SubImageBrightness() {}

	/**
	 * Computes the average brightness of the given image.
	 * @param image any Image (typically a sub-image tile)
	 * @return brightness in [0.0, 1.0], where 0 = black and 1 = white
	 */
	public static double of(Image image) {
		double sumGrey = 0.0;
		for (int row = 0; row < image.getHeight(); row++) {
			for (int col = 0; col < image.getWidth(); col++) {
				Color c = image.getPixel(row, col);
				sumGrey += c.getRed() * 0.2126  + c.getGreen() * 0.7152  + c.getBlue() * 0.0722;
			}
		}
		int totalPixels = image.getWidth() * image.getHeight();
		return sumGrey / (totalPixels * 255.0);
	}
}
