package ascii_art;

import ascii_output.AsciiOutput;
import ascii_output.ConsoleAsciiOutput;
import ascii_output.HtmlAsciiOutput;
import image.Image;
import image.PaddedImage;
import image.SubImageBrightness;
import image_char_matching.SubImgCharMatcher;

import java.io.IOException;

/**
 * Interactive command-line shell for configuring and running the ASCII art algorithm.
 * Supports 8 commands: exit, chars, add, remove, res, output, reverse, asciiArt.
 *
 * <p>Errors detected in command processing are thrown as exceptions from the helper
 * methods and caught centrally in the {@link #run} loop, where the error message is
 * printed via {@code System.out.println}.
 */
public class Shell {

	// ---- default values ----
	private static final char[] DEFAULT_CHAR_SET =
			{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
	private static final int DEFAULT_RESOLUTION = 2;
	private static final char FIRST_ASCII = 32;
	private static final char LAST_ASCII = 126;
	private static final int MIN_CHAR_SET_SIZE = 2;

	// ---- UI strings ----
	private static final String PROMPT = ">>> ";
	private static final String RESOLUTION_MESSAGE = "Resolution set to %d.";

	// ---- command keywords ----
	private static final String CMD_EXIT = "exit";
	private static final String CMD_CHARS = "chars";
	private static final String CMD_ADD = "add";
	private static final String CMD_REMOVE = "remove";
	private static final String CMD_RES = "res";
	private static final String CMD_OUTPUT = "output";
	private static final String CMD_REVERSE = "reverse";
	private static final String CMD_ASCII_ART = "asciiArt";
	private static final String RES_UP = "up";
	private static final String RES_DOWN = "down";
	private static final String OUTPUT_CONSOLE = "console";
	private static final String OUTPUT_HTML = "html";
	private static final String ADD_ALL = "all";
	private static final String ADD_SPACE = "space";
	private static final String HTML_FILE = "out.html";
	private static final String HTML_FONT = "Courier New";

	// ---- error messages (exact strings required by spec) ----
	private static final String ERR_COMMAND =
			"Did not execute due to incorrect command.";
	private static final String ERR_ADD_FORMAT =
			"Did not add due to incorrect format.";
	private static final String ERR_REMOVE_FORMAT =
			"Did not remove due to incorrect format.";
	private static final String ERR_RES_BOUNDARY =
			"Did not change resolution due to exceeding boundaries.";
	private static final String ERR_RES_FORMAT =
			"Did not change resolution due to incorrect format.";
	private static final String ERR_OUTPUT_FORMAT =
			"Did not change output method due to incorrect format.";
	private static final String ERR_CHARSET_SMALL =
			"Did not execute. Charset is too small.";

	// ---- mutable state ----
	private final SubImgCharMatcher matcher;
	private AsciiOutput output;
	private int resolution;
	private boolean reverse;

	// ---- image-dependent state (set in run) ----
	private PaddedImage paddedImage;
	private int maxResolution;
	private int minResolution;

	// ---- caching ----
	private double[][] cachedBrightness;
	private int cachedResolution;

	/**
	 * Constructs a Shell with default parameters: charset 0-9, resolution 2,
	 * console output, reverse off.
	 */
	public Shell() {
		this.matcher = new SubImgCharMatcher(DEFAULT_CHAR_SET);
		this.output = new ConsoleAsciiOutput();
		this.resolution = DEFAULT_RESOLUTION;
		this.reverse = false;
		this.cachedResolution = -1;
	}

	/**
	 * Entry point for the program.
	 *
	 * @param args command-line arguments; args[0] is the path to the image file
	 */
	public static void main(String[] args) {
		Shell shell = new Shell();
		shell.run(args[0]);
	}

	/**
	 * Runs the interactive command loop. Reads commands from the user,
	 * processes them, and prints results or error messages.
	 *
	 * @param imageName path to the source image file
	 */
	public void run(String imageName) {
		Image image;
		try {
			image = new Image(imageName);
		} catch (IOException e) {
			return;
		}
		this.paddedImage = new PaddedImage(image);
		this.maxResolution = paddedImage.getWidth();
		this.minResolution = Math.max(1,
				paddedImage.getWidth() / paddedImage.getHeight());

		boolean running = true;
		while (running) {
			System.out.print(PROMPT);
			String input = KeyboardInput.readLine().trim();
			String[] parts = input.split(" ");
			try {
				switch (parts[0]) {
					case CMD_EXIT -> running = false;
					case CMD_CHARS -> matcher.printCharSet();
					case CMD_ADD -> handleAddRemove(parts, true);
					case CMD_REMOVE -> handleAddRemove(parts, false);
					case CMD_RES -> handleRes(parts);
					case CMD_REVERSE -> reverse = !reverse;
					case CMD_OUTPUT -> handleOutput(parts);
					case CMD_ASCII_ART -> handleAsciiArt();
					default -> throw new InvalidCommandException(ERR_COMMAND);
				}
			} catch (InvalidCommandException | InvalidFormatException |
					 ExceedingBoundariesException | CharsetTooSmallException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	/**
	 * Handles the add or remove command by parsing the argument and updating
	 * the matcher's charset.
	 *
	 * @param parts the split command tokens
	 * @param add   true for add, false for remove
	 * @throws InvalidFormatException if the argument format is invalid
	 */
	private void handleAddRemove(String[] parts, boolean add)
			throws InvalidFormatException {
		String errMsg = add ? ERR_ADD_FORMAT : ERR_REMOVE_FORMAT;
		if (parts.length < 2) {
			throw new InvalidFormatException(errMsg);
		}
		String arg = parts[1];

		if (arg.equals(ADD_ALL)) {
			for (char c = FIRST_ASCII; c <= LAST_ASCII; c++) {
				if (add) {
					matcher.addChar(c);
				} else {
					matcher.removeChar(c);
				}
			}
		} else if (arg.equals(ADD_SPACE)) {
			if (add) {
				matcher.addChar(' ');
			} else {
				matcher.removeChar(' ');
			}
		} else if (arg.length() == 1) {
			char c = arg.charAt(0);
			if (c < FIRST_ASCII || c > LAST_ASCII) {
				throw new InvalidFormatException(errMsg);
			}
			if (add) {
				matcher.addChar(c);
			} else {
				matcher.removeChar(c);
			}
		} else if (arg.length() == 3 && arg.charAt(1) == '-') {
			char a = arg.charAt(0);
			char b = arg.charAt(2);
			if (a < FIRST_ASCII || a > LAST_ASCII ||
					b < FIRST_ASCII || b > LAST_ASCII) {
				throw new InvalidFormatException(errMsg);
			}
			char lo = (char) Math.min(a, b);
			char hi = (char) Math.max(a, b);
			for (char c = lo; c <= hi; c++) {
				if (add) {
					matcher.addChar(c);
				} else {
					matcher.removeChar(c);
				}
			}
		} else {
			throw new InvalidFormatException(errMsg);
		}
	}

	/**
	 * Handles the res command: prints current resolution or changes it.
	 *
	 * @param parts the split command tokens
	 * @throws InvalidFormatException      if the res sub-command is unrecognized
	 * @throws ExceedingBoundariesException if the new resolution is out of bounds
	 */
	private void handleRes(String[] parts)
			throws InvalidFormatException, ExceedingBoundariesException {
		if (parts.length == 1) {
			System.out.println(String.format(RESOLUTION_MESSAGE, resolution));
			return;
		}
		switch (parts[1]) {
			case RES_UP -> changeResolution(true);
			case RES_DOWN -> changeResolution(false);
			default -> throw new InvalidFormatException(ERR_RES_FORMAT);
		}
	}

	/**
	 * Doubles or halves the resolution if within bounds.
	 *
	 * @param up true to double, false to halve
	 * @throws ExceedingBoundariesException if the new value exceeds min or max
	 */
	private void changeResolution(boolean up)
			throws ExceedingBoundariesException {
		int newRes = up ? resolution * 2 : resolution / 2;
		if (newRes > maxResolution || newRes < minResolution) {
			throw new ExceedingBoundariesException(ERR_RES_BOUNDARY);
		}
		resolution = newRes;
		System.out.println(String.format(RESOLUTION_MESSAGE, resolution));
	}

	/**
	 * Handles the output command: switches between console and HTML output.
	 *
	 * @param parts the split command tokens
	 * @throws InvalidFormatException if the output target is unrecognized
	 */
	private void handleOutput(String[] parts) throws InvalidFormatException {
		if (parts.length < 2) {
			throw new InvalidFormatException(ERR_OUTPUT_FORMAT);
		}
		switch (parts[1]) {
			case OUTPUT_CONSOLE -> output = new ConsoleAsciiOutput();
			case OUTPUT_HTML ->
					output = new HtmlAsciiOutput(HTML_FILE, HTML_FONT);
			default -> throw new InvalidFormatException(ERR_OUTPUT_FORMAT);
		}
	}

	/**
	 * Handles the asciiArt command: runs the algorithm and sends output.
	 *
	 * @throws CharsetTooSmallException if charset has fewer than 2 characters
	 */
	private void handleAsciiArt() throws CharsetTooSmallException {
		if (matcher.getCharsetSize() < MIN_CHAR_SET_SIZE) {
			throw new CharsetTooSmallException(ERR_CHARSET_SMALL);
		}
		double[][] brightness = getBrightnessGrid();
		AsciiArtAlgorithm algorithm =
				new AsciiArtAlgorithm(brightness, matcher, reverse);
		output.out(algorithm.run());
	}

	/**
	 * Returns the cached sub-image brightness grid, recomputing it only when the
	 * resolution has changed since the last computation. This avoids redundant
	 * brightness calculations when re-running the algorithm with the same image
	 * and resolution but a different charset.
	 *
	 * @return 2D array of brightness values in [0.0, 1.0]
	 */
	private double[][] getBrightnessGrid() {
		if (cachedBrightness == null || cachedResolution != resolution) {
			Image[][] tiles = paddedImage.subImages(resolution);
			int rows = tiles.length;
			int cols = tiles[0].length;
			cachedBrightness = new double[rows][cols];
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					cachedBrightness[i][j] = SubImageBrightness.of(tiles[i][j]);
				}
			}
			cachedResolution = resolution;
		}
		return cachedBrightness;
	}
}
