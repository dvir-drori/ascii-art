package image_char_matching;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Matches characters from a configurable charset to image-region brightness values.
 *
 * <p>Each character's "raw brightness" is the fraction of its 16x16 rendered glyph
 * that is white (via {@link CharConverter}). Raw values are cached across runs so that
 * adding or removing a character never recomputes brightness for other characters.
 * Normalized brightness (linear stretch to [0,1]) is recomputed lazily on the next
 * call to {@link #getCharByImageBrightness} after any charset mutation.
 */
public class SubImgCharMatcher {
	/** Sorted set of characters currently in the charset. TreeSet keeps them in ascending
	 *  ASCII order, giving O(log n) add/remove and O(n) iteration for printing and lookup. */
	private final TreeSet<Character> charset;

	/** Maps every character ever computed to its raw brightness (trueCount / totalPixels).
	 *  Acts as a cache so re-adding a previously seen character is O(1) instead of
	 *  re-rendering the glyph. Uses HashMap for O(1) amortised get/put. */
	private final Map<Character, Double> rawBrightness;

	/** Maps each character in the current charset to its linearly-stretched brightness
	 *  in [0,1]. Rebuilt lazily when {@link #normalizedDirty} is true. */
	private Map<Character, Double> normalizedBrightness;

	/** Flag indicating that the charset has changed since the last normalization. */
	private boolean normalizedDirty;

	/**
	 * Constructs a matcher seeded with the given charset.
	 *
	 * @param charset the initial set of characters available for matching
	 */
	public SubImgCharMatcher(char[] charset) {
		this.rawBrightness = new HashMap<>();
		this.charset = new TreeSet<>();
		for (char c : charset) {
			this.charset.add(c);
			this.rawBrightness.put(c, computeRawBrightness(c));
		}
		this.normalizedDirty = true;
	}

	/**
	 * Returns the character whose normalized brightness is closest to the given value.
	 * If several characters tie, the one with the lower ASCII value is returned.
	 *
	 * @param brightness target brightness in [0.0, 1.0]
	 * @return best-matching character from the current charset
	 */
	public char getCharByImageBrightness(double brightness) {
		if (normalizedDirty) {
			recomputeNormalized();
			normalizedDirty = false;
		}
		char best = charset.first();
		double bestDiff = Double.MAX_VALUE;
		for (char c : charset) {
			double diff = Math.abs(normalizedBrightness.get(c) - brightness);
			if (diff < bestDiff || (diff == bestDiff && c < best)) {
				best = c;
				bestDiff = diff;
			}
		}
		return best;
	}

	/**
	 * Adds a character to the charset. If the character is already present this is
	 * a no-op. Raw brightness is computed once and cached.
	 *
	 * @param c character to add
	 */
	public void addChar(char c) {
		if (charset.add(c)) {
			if (!rawBrightness.containsKey(c)) {
				rawBrightness.put(c, computeRawBrightness(c));
			}
			normalizedDirty = true;
		}
	}

	/**
	 * Removes a character from the charset. If the character is not present this is
	 * a no-op.
	 *
	 * @param c character to remove
	 */
	public void removeChar(char c) {
		if (charset.remove(c)) {
			normalizedDirty = true;
		}
	}

	/**
	 * Returns the number of characters currently in the charset.
	 *
	 * @return charset size
	 */
	public int getCharsetSize() {
		return charset.size();
	}

	/**
	 * Prints all characters in the charset in ascending ASCII order,
	 * each followed by a space, on a single line.
	 * If the charset is empty, nothing is printed.
	 */
	public void printCharSet() {
		if (charset.isEmpty()) {
			return;
		}
		for (char c : charset) {
			System.out.print(c + " ");
		}
		System.out.println();
	}

	/**
	 * Rebuilds the normalized brightness map by linear-stretching every character's
	 * raw brightness into [0.0, 1.0] using the min and max raw values in the
	 * current charset.
	 */
	private void recomputeNormalized() {
		normalizedBrightness = new HashMap<>();
		if (charset.isEmpty()) {
			return;
		}
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		for (char c : charset) {
			double b = rawBrightness.get(c);
			if (b < min) {
				min = b;
			}
			if (b > max) {
				max = b;
			}
		}
		double range = max - min;
		for (char c : charset) {
			double normalized = (range == 0) ? 0 :
					(rawBrightness.get(c) - min) / range;
			normalizedBrightness.put(c, normalized);
		}
	}

	/**
	 * Computes the raw brightness of a character: the fraction of white (true)
	 * pixels in its 16x16 rendered glyph.
	 *
	 * @param c the character to measure
	 * @return brightness in [0.0, 1.0]
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
}
