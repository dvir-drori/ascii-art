package ascii_art;

/**
 * Thrown when the ASCII art algorithm is invoked with fewer than two characters
 * in the charset.
 */
public class CharsetTooSmallException extends Exception {
	/**
	 * @param message the error message to display
	 */
	public CharsetTooSmallException(String message) {
		super(message);
	}
}
