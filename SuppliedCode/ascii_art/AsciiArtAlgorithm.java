package ascii_art;

import image_char_matching.SubImgCharMatcher;

/**
 * Executes a single pass of the ASCII-art conversion: maps a pre-computed
 * brightness grid of sub-images to ASCII characters using the provided matcher.
 *
 * <p>A new instance should be created for each run. Sub-image brightness values
 * and character brightness values are cached externally (by the caller and by
 * {@link SubImgCharMatcher} respectively) so that creating a new instance does
 * not trigger redundant computation.
 */
public class AsciiArtAlgorithm {
	private final double[][] brightnessGrid;
	private final SubImgCharMatcher matcher;
	private final boolean reverse;

	/**
	 * Constructs an algorithm instance for a single run.
	 *
	 * @param brightnessGrid pre-computed brightness values for each sub-image,
	 *                       where brightnessGrid[row][col] is in [0.0, 1.0]
	 * @param matcher        character matcher mapping brightness to chars
	 * @param reverse        if true, use the complement brightness (1 - value)
	 *                       when matching characters
	 */
	public AsciiArtAlgorithm(double[][] brightnessGrid,
							 SubImgCharMatcher matcher,
							 boolean reverse) {
		this.brightnessGrid = brightnessGrid;
		this.matcher = matcher;
		this.reverse = reverse;
	}

	/**
	 * Produces the ASCII rendering by matching each sub-image brightness to the
	 * closest character in the charset.
	 *
	 * @return a 2D char array where each cell is the matched ASCII character
	 */
	public char[][] run() {
		int rows = brightnessGrid.length;
		int cols = brightnessGrid[0].length;
		char[][] result = new char[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				double brightness = brightnessGrid[i][j];
				if (reverse) {
					brightness = 1 - brightness;
				}
				result[i][j] = matcher.getCharByImageBrightness(brightness);
			}
		}
		return result;
	}
}
