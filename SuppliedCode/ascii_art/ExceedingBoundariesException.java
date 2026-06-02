package ascii_art;

/**
 * Thrown when a resolution change would exceed the allowed minimum or maximum.
 */
public class ExceedingBoundariesException extends Exception {
	/**
	 * @param message the error message to display
	 */
	public ExceedingBoundariesException(String message) {
		super(message);
	}
}
