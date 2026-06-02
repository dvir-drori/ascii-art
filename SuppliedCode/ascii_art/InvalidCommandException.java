package ascii_art;

/**
 * Thrown when the user enters an unrecognized command.
 */
public class InvalidCommandException extends Exception {
	/**
	 * @param message the error message to display
	 */
	public InvalidCommandException(String message) {
		super(message);
	}
}
