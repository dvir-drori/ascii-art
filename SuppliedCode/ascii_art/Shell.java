package ascii_art;

import ascii_output.AsciiOutput;
import ascii_output.ConsoleAsciiOutput;
import ascii_output.HtmlAsciiOutput;
import image.Image;
import image_char_matching.SubImgCharMatcher;

import java.io.IOException;

public class Shell{
	private static final char[] DEFAULT_CHAR_SET = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
	private static final char FIRST_ASCII = 32;
	private static final char LAST_ASCII = 126;
	private static final int DEFAULT_RESOLUTION = 2;
	private static final String NEW_COMMAND_PRINT = ">>> ";
	private static final String COMMAND_SEPARATOR = " ";
	private static final String INVALID_COMMAND_MESSAGE = "Did not execute due to incorrect command.";
	private static final String INVALID_ADD_COMMAND_MESSAGE = "Did not add due to incorrect format.";
	private static final String INVALID_REMOVE_COMMAND_MESSAGE = "Did not remove due to incorrect format.";
	private static final String RESOLUTION_UPDATE_FAILURE_MESSAGE = "Did not change resolution due to exceeding boundaries.";
	private static final String INVALID_RES_COMMAND_MESSAGE = "Did not change resolution due to incorrect format.";
	private static final String INVALID_OUTPUT_COMMAND = "Did not change output method due to incorrect format.";
	private static final String RESOLUTION_MESSAGE = "Resolution set to %d.%n";
	private static final double RESOLUTION_UP_FACTOR = 2.0;
	private static final double RESOLUTION_DOWN_FACTOR = 0.5;
	private static final String EXIT_COMMAND = "exit";
	private static final String PRINT_CHARS_COMMAND = "chars";
	private static final String ADD_CHARS_COMMAND = "add";
	private static final String REMOVE_CHARS_COMMAND = "remove";
	private static final String CHANGE_RES_COMMAND = "res";
	private static final String UP_RES_COMMAND = "up";
	private static final String DOWN_RES_COMMAND = "down";
	private static final String REVERSE_COMMAND = "reverse";
	private static final String CHANGE_OUTPUT_COMMAND = "output";
	private static final String EXECUTE_COMMAND = "asciiArt";
	private static final String DEFAULT_HTML_OUTPUT_FILE = "out.html";
	private static final String DEFAULT_HTML_FONT = "Courier New";


	private final SubImgCharMatcher matcher;
	private AsciiArtAlgorithm algorithm;
	private AsciiOutput printer;
	private int resolution;
	private int maxResolution;
	private int minResolution;
	private Image image;
	private boolean reverse;

	/**
	 * constructs the shell
	 */
	public Shell() {
		this.matcher = new SubImgCharMatcher(DEFAULT_CHAR_SET);
		this.printer = new ConsoleAsciiOutput();
		resolution = DEFAULT_RESOLUTION;
		reverse = false;
	}

	/**
	 * main function of the program
	 * initiate the shell
	 * @param args command line argument, route to image file
	 */
	public static void main(String[] args) {
		Shell shell = new Shell();
		try{
			shell.run(args[0]);
		} catch (IOException e){
			return;
		}

	}

	/**
	 * run the shell command interface
	 * receive input from the user and handles it accordingly
	 * @param imageName route to image file
	 */
	public void run(String imageName) throws IOException{
		Image image;
		try {
			image = new Image(imageName);
		} catch (IOException e) {
			throw e;
		}
		this.maxResolution = image.getWidth();
		this.minResolution = Math.max(1, image.getWidth()/image.getHeight());
		this.algorithm = new AsciiArtAlgorithm(image, resolution, matcher, reverse);
		this.image = image;
		boolean exit = false;
		while (!exit){
			System.out.print(NEW_COMMAND_PRINT);
			String input = KeyboardInput.readLine().trim();
			String[] parts = input.split(COMMAND_SEPARATOR);
			switch (parts[0]){
				case EXIT_COMMAND -> exit =true;
				case PRINT_CHARS_COMMAND -> matcher.printCharSet();
				case ADD_CHARS_COMMAND -> {
					if (parts.length<2){
						System.out.println(INVALID_ADD_COMMAND_MESSAGE);
						continue;
					}
					updateChars(parts[1], true);
				}
				case REMOVE_CHARS_COMMAND ->{
					if (parts.length<2){
						System.out.println(INVALID_REMOVE_COMMAND_MESSAGE);
						continue;
					}
					updateChars(parts[1], false);
				}
				case CHANGE_RES_COMMAND -> {
					if(parts.length == 1){
						System.out.printf(RESOLUTION_MESSAGE, resolution);
					}
					else {
						switch (parts[1]){
						case UP_RES_COMMAND -> updateResolution(RESOLUTION_UP_FACTOR);
						case DOWN_RES_COMMAND -> updateResolution(RESOLUTION_DOWN_FACTOR);
						default -> System.out.println(INVALID_RES_COMMAND_MESSAGE);
						}
					}
				}
				case REVERSE_COMMAND -> {
					algorithm.flipReverse();
					reverse = !reverse;
				}
				case CHANGE_OUTPUT_COMMAND -> {
					if (parts.length < 2){
						System.out.println(INVALID_OUTPUT_COMMAND);
					}
					else{
						changeOutput(parts[1]);
					}
				}
				case EXECUTE_COMMAND -> printer.out(algorithm.run());
			default -> System.out.println(INVALID_COMMAND_MESSAGE);
			}

		}
	}

	private void changeOutput(String target) {
		switch (target){
			case "console" -> printer = new ConsoleAsciiOutput();
			case "html" -> printer = new HtmlAsciiOutput(DEFAULT_HTML_OUTPUT_FILE, DEFAULT_HTML_FONT);
		default -> System.out.println(INVALID_OUTPUT_COMMAND);
		}
	}

	private void updateResolution(double factor) {
		int newResolution = (int)(resolution*factor);
		if (newResolution >maxResolution || newResolution<minResolution ){
			System.out.println(RESOLUTION_UPDATE_FAILURE_MESSAGE);
		}
		else{
			resolution = newResolution;
			algorithm = new AsciiArtAlgorithm(image, resolution, matcher, reverse);
			System.out.printf(RESOLUTION_MESSAGE, resolution);
		}
	}

	private void updateChars(String chars, boolean add) {
		String message = add ? INVALID_ADD_COMMAND_MESSAGE : INVALID_REMOVE_COMMAND_MESSAGE;
		//add all
		if (chars.equals("all")){
			for (char c = FIRST_ASCII; c <= LAST_ASCII; c++){
				if (add){
					matcher.addChar(c);
				}
				else matcher.removeChar(c);

			}
		}
		//add space
		else if (chars.equals("space")) {
			if (add){
				matcher.addChar(' ');
			}
			else matcher.removeChar(' ');

		}
		//add 1 character
		else if (chars.length() == 1) {
			if (chars.charAt(0) <FIRST_ASCII || chars.charAt(0) >LAST_ASCII){
				System.out.println(message);
				return;
			}
			if(add){
				matcher.addChar(chars.charAt(0));
			}
			else matcher.removeChar(chars.charAt(0));

		}
		//add range of characters
		else if (chars.length() == 3 && chars.charAt(1) == '-') {
			char a = chars.charAt(0);
			char b = chars.charAt(2);
			if (a<FIRST_ASCII || a>LAST_ASCII || b<FIRST_ASCII||b>LAST_ASCII){
				System.out.println(message);
				return;
			}
			for (char c = (char)Math.min(a, b); c <= (char)Math.max(a, b); c++) {
				if (add){
					matcher.addChar(c);
				}
				else matcher.removeChar(c);
			}
		}
		//invalid add
		else {
			System.out.println(message);
		}
	}
}