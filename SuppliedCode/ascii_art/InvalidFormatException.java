package ascii_art;

/**
 * Thrown when a command receives arguments in an incorrect format.
 */
public class InvalidFormatException extends Exception {
	/**
	 * @param message the error message to display
	 */
	public InvalidFormatException(String message) {
		super(message);
	}
}
