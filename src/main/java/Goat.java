import java.util.Scanner;

/**
 * A simple command-line chatbot.
 * Currently it greets the user, stores todos, deadlines and events,
 * lists the stored tasks on request, lets a task be marked as done,
 * and exits when the user types "bye".
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

    /** The command that marks a task as done, e.g. "mark 2". */
    private static final String MARK_COMMAND = "mark";

    /** The command that marks a task as not done again, e.g. "unmark 2". */
    private static final String UNMARK_COMMAND = "unmark";

    /** The command that adds a task with no date attached, e.g. "todo read". */
    private static final String TODO_COMMAND = "todo";

    /** The command that adds a task due by a date, e.g. "deadline x /by Sun". */
    private static final String DEADLINE_COMMAND = "deadline";

    /** The command that adds a task spanning a period, e.g. "event x /from a /to b". */
    private static final String EVENT_COMMAND = "event";

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

        // Each Task bundles its own description and done status, so one array
        // is enough. taskCount doubles as "how many are stored" and
        // "where the next one goes".
        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;

        // Scanner reads the user's input from the terminal, one line at a time.
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();

            if (command.equals(EXIT_COMMAND)) {
                break;
            } else if (command.equals(LIST_COMMAND)) {
                showTasks(tasks, taskCount);
            } else if (command.startsWith(MARK_COMMAND + " ")) {
                setDone(tasks, taskCount,
                        command.substring(MARK_COMMAND.length() + 1), true);
            } else if (command.startsWith(UNMARK_COMMAND + " ")) {
                setDone(tasks, taskCount,
                        command.substring(UNMARK_COMMAND.length() + 1), false);
            } else {
                Task newTask = createTask(command);
                if (newTask == null) {
                    reply("Sorry, I couldn't understand that. Try one of:",
                            "  todo <description>",
                            "  deadline <description> /by <when>",
                            "  event <description> /from <start> /to <end>");
                } else if (taskCount < MAX_TASKS) {
                    tasks[taskCount] = newTask;
                    taskCount++;
                    reply("Got it. I've added this task:",
                            "  " + newTask,
                            "Now you have " + taskCount + " tasks in the list.");
                } else {
                    reply("Sorry, I can only remember " + MAX_TASKS + " tasks.");
                }
            }
        }

        reply("Bye. Hope to see you again soon!");
        scanner.close();
    }

    /**
     * Builds the right kind of task for what the user typed.
     * The command word decides the subclass, and the text after it
     * supplies the description and any dates.
     *
     * @param command the whole line the user typed, already trimmed
     * @return the new task, or null if a todo/deadline/event command
     *         was recognised but its parts were missing
     */
    private static Task createTask(String command) {
        if (command.startsWith(TODO_COMMAND + " ")) {
            String description = command.substring(TODO_COMMAND.length() + 1).trim();
            return description.isEmpty() ? null : new Todo(description);
        }
        if (command.startsWith(DEADLINE_COMMAND + " ")) {
            // Split once on "/by": everything before it is the description.
            String[] parts = command.substring(DEADLINE_COMMAND.length() + 1).split("/by", 2);
            if (parts.length < 2) {
                return null;
            }
            String description = parts[0].trim();
            String by = parts[1].trim();
            return description.isEmpty() || by.isEmpty()
                    ? null : new Deadline(description, by);
        }
        if (command.startsWith(EVENT_COMMAND + " ")) {
            // Split on "/from" first, then split what follows on "/to".
            String[] fromParts = command.substring(EVENT_COMMAND.length() + 1).split("/from", 2);
            if (fromParts.length < 2) {
                return null;
            }
            String[] toParts = fromParts[1].split("/to", 2);
            if (toParts.length < 2) {
                return null;
            }
            String description = fromParts[0].trim();
            String from = toParts[0].trim();
            String to = toParts[1].trim();
            return description.isEmpty() || from.isEmpty() || to.isEmpty()
                    ? null : new Event(description, from, to);
        }
        // Anything else is still kept as a plain task, as it was before.
        return new Task(command);
    }

    /**
     * Changes the done status of one task and confirms the change.
     * Marking and unmarking differ only in the value stored and the
     * wording of the reply, so both share this method.
     *
     * @param tasks     the backing array of tasks
     * @param taskCount how many tasks are currently stored
     * @param argument  the text the user typed after the command word
     * @param done      the status to store: true for done, false for not done
     */
    private static void setDone(Task[] tasks, int taskCount,
            String argument, boolean done) {
        int index = parseTaskNumber(argument, taskCount);
        if (index < 0) {
            reply("Sorry, I don't have a task numbered '" + argument.trim() + "'.");
            return;
        }
        Task task = tasks[index];
        if (done) {
            task.markAsDone();
        } else {
            task.markAsNotDone();
        }
        reply(done ? "Nice! I've marked this task as done:"
                        : "OK, I've marked this task as not done yet:",
                "  " + task);
    }

    /**
     * Converts the argument of a "mark" command into an array index.
     * The user counts from 1, so 1 maps to index 0.
     *
     * @param argument  the text the user typed after the command word
     * @param taskCount how many tasks are currently stored
     * @return the matching 0-based index, or -1 if the argument is not a
     *         whole number that refers to an existing task
     */
    private static int parseTaskNumber(String argument, int taskCount) {
        try {
            int taskNumber = Integer.parseInt(argument.trim());
            if (taskNumber >= 1 && taskNumber <= taskCount) {
                return taskNumber - 1;
            }
        } catch (NumberFormatException e) {
            // Not a number at all; fall through to the -1 below.
        }
        return -1;
    }

    /**
     * Prints the stored tasks as a numbered list, counting from 1
     * because that reads more naturally than the array's 0-based index.
     *
     * @param tasks     the backing array of tasks
     * @param taskCount how many entries of the array are actually in use
     */
    private static void showTasks(Task[] tasks, int taskCount) {
        if (taskCount == 0) {
            reply("There is nothing in your list yet.");
            return;
        }
        // One header line, then one line per task.
        String[] lines = new String[taskCount + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int i = 0; i < taskCount; i++) {
            lines[i + 1] = (i + 1) + "." + tasks[i];
        }
        reply(lines);
    }
}
