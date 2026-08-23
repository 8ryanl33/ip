import java.util.Scanner;

/**
 * A simple command-line chatbot.
 * Currently it greets the user, echoes back every command entered,
 * and exits when the user types "bye".
 */
public class Goat {
    /** Horizontal line used to separate the chatbot's replies. */
    private static final String DIVIDER = "    " + "_".repeat(60);

    /** The command that ends the conversation. */
    private static final String EXIT_COMMAND = "bye";

    /**
     * Prints a reply wrapped between two horizontal lines,
     * so every message the chatbot sends looks the same.
     *
     * @param message the text to show to the user
     */
    private static void reply(String message) {
        System.out.println(DIVIDER);
        System.out.println(" " + message);
        System.out.println(DIVIDER);
        System.out.println();
    }

    public static void main(String[] args) {
        String banner = "  ____   ___      _     _____ \n"
                + " / ___| / _ \\    / \\   |_   _|\n"
                + "| |  _ | | | |  / _ \\    | |  \n"
                + "| |_| || |_| | / ___ \\   | |  \n"
                + " \\____| \\___/ /_/   \\_\\  |_|  \n";
        System.out.println(banner);
        System.out.println(" Hello! I'm Goat");
        reply("What can I do for you?");

        // Scanner reads the user's input from the terminal, one line at a time.
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();
            if (command.equals(EXIT_COMMAND)) {
                break;
            }
            reply(command); // Echo stage: repeat back whatever was typed.
        }

        reply("Bye. Hope to see you again soon!");
        scanner.close();
    }
}
