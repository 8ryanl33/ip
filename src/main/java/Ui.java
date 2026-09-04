import java.util.Scanner;

/**
 * Handles everything Goat says to the user and everything the user types back.
 *
 * Collecting the input and output here means the rest of the program never
 * touches {@code System.out} or {@code System.in} directly. That keeps the
 * layout of a reply (the dividers, the indent, the "OOPS!!! " prefix) in one
 * place, and it means a change of interface -- a GUI, say -- would replace
 * this class rather than being scattered through the command handling.
 *
 * Unlike {@link Storage}, this class is instance-based: it owns a
 * {@link Scanner} that has to be created and closed, and an object is the
 * natural home for something with a lifetime like that.
 */
public class Ui {
    /** Horizontal line used to separate the chatbot's replies. */
    private static final String DIVIDER = "    " + "_".repeat(60);

    /** Left padding for reply text, so it sits just inside the divider. */
    private static final String INDENT = "     ";

    /** Prefix put in front of every error message shown to the user. */
    private static final String ERROR_PREFIX = "OOPS!!! ";

    /** The logo shown once when the program starts. */
    private static final String BANNER = "  ____   ___      _     _____ \n"
            + " / ___| / _ \\    / \\   |_   _|\n"
            + "| |  _ | | | |  / _ \\    | |  \n"
            + "| |_| || |_| | / ___ \\   | |  \n"
            + " \\____| \\___/ /_/   \\_\\  |_|  \n";

    /** Reads the user's input from the terminal, one line at a time. */
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Prints a reply wrapped between two horizontal lines,
     * so every message the chatbot sends looks the same.
     * Accepts any number of lines, since replies such as the task
     * listing span several lines inside a single pair of dividers.
     *
     * @param lines the text to show to the user, one element per line
     */
    public void show(String... lines) {
        System.out.println(DIVIDER);
        for (String line : lines) {
            System.out.println(INDENT + line);
        }
        System.out.println(DIVIDER);
        System.out.println();
    }

    /** Shows the logo and the greeting, once, at start-up. */
    public void showWelcome() {
        System.out.println(BANNER);
        show("Hello! I'm Goat", "What can I do for you?");
    }

    /** Shows the parting message, once, just before the program ends. */
    public void showGoodbye() {
        show("Bye. Hope to see you again soon!");
    }

    /**
     * Shows something that went wrong, in the same box as any other reply.
     *
     * @param message the explanation to show, without the "OOPS!!! " prefix
     */
    public void showError(String message) {
        show(ERROR_PREFIX + message);
    }

    /**
     * Warns that the save file could not be read.
     * This is its own method because the reassurance that follows the error
     * is fixed wording, not something the caller should have to supply.
     *
     * @param message why the file could not be read
     */
    public void showLoadingError(String message) {
        show(ERROR_PREFIX + message,
                "I'll start with an empty list; fix the file now if you want to keep it.");
    }

    /**
     * Reports whether there is another line of input waiting.
     * Input can run out without a "bye", for example when commands are piped
     * in from a file, so the caller has to be able to check before reading.
     *
     * @return true if {@link #readCommand()} has a line to return
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Reads one line typed by the user.
     *
     * @return the line, with surrounding spaces removed
     */
    public String readCommand() {
        return scanner.nextLine().trim();
    }

    /** Releases the terminal input once the program is finished with it. */
    public void close() {
        scanner.close();
    }
}
