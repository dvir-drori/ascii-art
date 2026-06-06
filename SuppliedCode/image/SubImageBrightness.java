package image;

import java.awt.*;

/**
 * Utility class for computing the average grey-scale brightness of an image
 * in the range [0.0, 1.0]. Uses the standard luminance formula:
 * grey = 0.2126*R + 0.7152*G + 0.0722*B, then normalises by 255.
 */
public final class SubImageBrightness {
	private static final double RED_CONSTANT = 0.2126;
	private static final double GREEN_CONSTANT = 0.7152;
	private static final double BLUE_CONSTANT = 0.0722;
	private static final double NUM_OF_SHADES = 255.0;
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
				sumGrey += c.getRed() * RED_CONSTANT  + c.getGreen() * GREEN_CONSTANT  + c.getBlue() * BLUE_CONSTANT;
			}
		}
		int totalPixels = image.getWidth() * image.getHeight();
		return sumGrey / (totalPixels * NUM_OF_SHADES);
	}
}
