package ascii_art;

import image.Image;
import image.PaddedImage;
import image.SubImageBrightness;
import image_char_matching.SubImgCharMatcher;

/**
 * Runs the ASCII-art pipeline: pads the source image, splits it into a grid of
 * sub-images at the given resolution, and matches each sub-image's brightness
 * to the closest character in the provided charset.
 */

public class AsciiArtAlgorithm {
	private final Image image;
	private final int resolution;
	private final SubImgCharMatcher matcher;
	private boolean reverse;

	/**
	 * @param image      the source image
	 * @param resolution number of sub-images per row
	 * @param matcher    SubImgCharMatcher object
	 */
	public AsciiArtAlgorithm(Image image, int resolution, SubImgCharMatcher matcher, boolean reverse) {
		this.image = image;
		this.resolution = resolution;
		this.matcher = matcher;
		this.reverse = reverse;
	}

	/**
	 * Produces the ASCII rendering of the image.
	 *
	 * @return a char[rows][cols] grid where each cell is the chosen character
	 *         for the corresponding sub-image
	 */
	public char[][] run(){
		PaddedImage padded = new PaddedImage(image);
		Image[][] tiles = padded.subImages(resolution);
		int rows = tiles.length;
		int cols = tiles[0].length;
		char[][] result = new char[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				double brightness = SubImageBrightness.of(tiles[i][j]);
				if (!reverse){
					result[i][j] = matcher.getCharByImageBrightness(brightness);
				}
				else{
					result[i][j] = matcher.getCharByImageBrightness(1-brightness);
				}

			}
		}
		return result;
	}

	/**
	 * flips the reverse option of the algorithm
	 */
	public void flipReverse(){
		reverse = !reverse;
	}


}
