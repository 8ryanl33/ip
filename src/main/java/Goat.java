import java.util.Scanner;

/**
 * A simple command-line chatbot.
 * Currently it greets the user, stores any text entered as a task,
 * lists the stored tasks on request, and exits when the user types "bye".
 */
public class Goat {
    /** Horizontal line used to separate the chatbot's replies. */
    private static final String DIVIDER = "    " + "_".repeat(60);

    /** Left padding for reply text, so it sits just inside the divider. */
    private static final String INDENT = "     ";

    /** The command that ends the conversation. */
    private static final String EXIT_COMMAND = "bye";

    /** The command that shows everything stored so far. */
    private static final String LIST_COMMAND = "list";

    /** Upper bound on stored tasks, as allowed by the requirements. */
    private static final int MAX_TASKS = 100;

    /**
     * Prints a reply wrapped between two horizontal lines,
     * so every message the chatbot sends looks the same.
     * Accepts any number of lines, since replies such as the task
     * listing span several lines inside a single pair of dividers.
     *
     * @param lines the text to show to the user, one element per line
     */
    private static void reply(String... lines) {
        System.out.println(DIVIDER);
        for (String line : lines) {
            System.out.println(INDENT + line);
        }
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
        reply("Hello! I'm Goat", "What can I do for you?");

        // Fixed-size storage: tasks[0..taskCount-1] hold the entries added so far,
        // so taskCount doubles as "how many are stored" and "where the next one goes".
        String[] tasks = new String[MAX_TASKS];
        int taskCount = 0;

        // Scanner reads the user's input from the terminal, one line at a time.
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();

            if (command.equals(EXIT_COMMAND)) {
                break;
            } else if (command.equals(LIST_COMMAND)) {
                showTasks(tasks, taskCount);
            } else if (taskCount < MAX_TASKS) {
                tasks[taskCount] = command;
                taskCount++;
                reply("added: " + command);
            } else {
                reply("Sorry, I can only remember " + MAX_TASKS + " tasks.");
            }
        }

        reply("Bye. Hope to see you again soon!");
        scanner.close();
    }

    /**
     * Prints the stored tasks as a numbered list, counting from 1
     * because that reads more naturally than the array's 0-based index.
     *
     * @param tasks     the backing array of stored tasks
     * @param taskCount how many entries of the array are actually in use
     */
    private static void showTasks(String[] tasks, int taskCount) {
        if (taskCount == 0) {
            reply("There is nothing in your list yet.");
            return;
        }
        String[] lines = new String[taskCount];
        for (int i = 0; i < taskCount; i++) {
            lines[i] = (i + 1) + ". " + tasks[i];
        }
        reply(lines);
    }
}
