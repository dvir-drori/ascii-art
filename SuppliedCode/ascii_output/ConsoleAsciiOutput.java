package ascii_output;

/**
 * Output a 2D array of chars to the console.
 * @author Dan Nirel
 */
public class ConsoleAsciiOutput implements AsciiOutput{
    @Override
    public void out(char[][] chars) {
        for (char[] row : chars) {
            for (char c : row) {
                System.out.print(c + " ");
            }
            System.out.println();
        }
    }
}
