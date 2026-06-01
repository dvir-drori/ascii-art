package image_char_matching;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Matches characters from a configurable charset to image-region brightness values.
 *
 * <p>Each character's "raw brightness" is the fraction of its 16x16 rendered glyph
 * that is filled in (via {@link CharConverter}). Raw values are then linear-stretched
 * across the current charset so the sparsest char maps to 0.0 and the densest to 1.0;
 * matching is performed against these normalized values.
 *
 * <p>Characters can be added or removed at runtime; the normalized map is rebuilt
 * automatically whenever the charset changes.
 */

public class SubImgCharMatcher {
	private final TreeSet<Character> charset;
	private final Map<Character, Double> rawBrightness;
	private Map<Character, Double> normalizedBrightness;

	/**
	 * Constructs a matcher seeded with the given charset.
	 *
	 * @param charset the initial set of characters available for matching
	 */

	public SubImgCharMatcher (char[] charset){
		this.rawBrightness = new HashMap<>();
		this.charset = new TreeSet<>();
		for(char c : charset){
			this.charset.add(c);
			this.rawBrightness.put(c, computeRawBrightness(c));
		}
		recomputeNormalized();

	}

	/**
	 * Adds a character to the charset. No-op if the character is already present.
	 * Triggers a rebuild of the normalized-brightness map.
	 * @param ch char to add
	 */

	public void addChar(char ch){
		if (charset.add(ch)){
			rawBrightness.put(ch, computeRawBrightness(ch));
			recomputeNormalized();
		}
	}

	/**
	 * Removes a character from the charset.
	 * Triggers a rebuild of the normalized-brightness map.
	 * @param ch char to remove.
	 */

	public void removeChar(char ch){
		if (charset.remove(ch)){
			rawBrightness.remove(ch);
			recomputeNormalized();
		}
	}

	/**
	 * Rebuilds {@link #normalizedBrightness} by linear-stretching every char's raw
	 * brightness into [0.0, 1.0] across the current charset's min and max.
	 * Called whenever the charset is mutated.
	 */

	private void recomputeNormalized() {
		normalizedBrightness = new HashMap<>();
		if (charset.isEmpty()){
			return;
		}
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		for (double b : rawBrightness.values()){
			if (b < min){
				min = b;
			}
			if (b > max){
				max = b;
			}
		}
		double range = max - min;
		for (Map.Entry<Character, Double> entry : rawBrightness.entrySet()){
			double n;
			if (range == 0){
				n = 0;
			}
			else {
				n = (entry.getValue() - min) / range;
			}
			normalizedBrightness.put(entry.getKey(), n);
		}
	}

	/**
	 * Computes the raw brightness of a character: the fraction of its 16x16 rendered
	 * glyph that is filled (true cells / total cells).
	 */

	private static double computeRawBrightness(char c) {
		boolean[][] grid = CharConverter.convertToBoolArray(c);
		int trueCount = 0;
		for (boolean[] row : grid) {
			for (boolean b : row) {
				if (b) {
					trueCount++;
				}
			}
		}
		return trueCount / (double) (grid.length * grid[0].length);
	}

	/**
	 * Returns the character whose normalized brightness is closest to the given value.
	 * Ties are broken by smaller ASCII value.
	 *
	 * @param brightness target brightness in [0.0, 1.0]
	 * @return best-matching character from the current charset
	 */
	public char getCharByImageBrightness(double brightness){
		char best = charset.first();
		double bestDiff = Double.MAX_VALUE;
		for(char c : charset){
			double curDiff = Math.abs(normalizedBrightness.get(c)-brightness);
			if(curDiff<bestDiff || (curDiff ==  bestDiff && c < best)){
				best = c;
				bestDiff = curDiff;
			}
		}
		return best;
	}

	/**
	 * prints all available chars by ascending ascii order
	 */
	public void printCharSet(){
		for (char c : charset){
			System.out.print(c+" ");
		}
		System.out.println();
	}

}
