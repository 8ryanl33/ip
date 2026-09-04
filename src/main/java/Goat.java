import java.util.ArrayList;

/**
 * A simple command-line chatbot.
 * Currently it greets the user, stores todos, deadlines and events,
 * lists the stored tasks on request, lets a task be marked as done
 * or deleted, and exits when the user types "bye".
 *
 * The task list is kept on disk by {@link Storage}: it is loaded at
 * start-up and written out again after every change, so the list
 * survives the program being closed.
 *
 * All talking to the user goes through {@link Ui}, so this class is left
 * with the part that is genuinely its own job: deciding what each command
 * means and doing it.
 */
public class Goat {

    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();

        // An ArrayList grows as needed and tracks its own size, so there is no
        // fixed cap and no separate counter to keep in step with the contents.
        // It starts off holding whatever was saved the last time Goat ran.
        ArrayList<Task> tasks;
        try {
            tasks = Storage.load();
        } catch (GoatException e) {
            // A save file that cannot be read should not stop the program, but
            // the user is warned, because the next change will overwrite it.
            ui.showLoadingError(e.getMessage());
            tasks = new ArrayList<>();
        }

        // "bye" now ends the loop by clearing this flag, because a break inside
        // the switch below would only leave the switch, not the loop.
        boolean isRunning = true;
        while (isRunning && ui.hasNextCommand()) {
            String line = ui.readCommand();

            // Split into the first word and everything after it, so that a bare
            // "todo" is recognised as the todo command with a missing description
            // rather than as some unknown command.
            String commandWord = line.split(" ", 2)[0];
            String argument = line.substring(commandWord.length()).trim();

            // One catch for the whole dispatch: each step below just throws when
            // something is wrong, and this decides how the problem is shown.
            // Catching inside the loop means a bad command never ends the program.
            try {
                Command command = Command.fromKeyword(commandWord);
                // Arrow labels cannot fall through, so no break is needed.
                switch (command) {
                case BYE -> isRunning = false;
                case LIST -> showTasks(ui, tasks);
                case MARK -> setDone(ui, tasks, argument, true);
                case UNMARK -> setDone(ui, tasks, argument, false);
                case DELETE -> deleteTask(ui, tasks, argument);
                case TODO, DEADLINE, EVENT -> addTask(ui, tasks, command, argument);
                }
            } catch (GoatException e) {
                ui.showError(e.getMessage());
            }
        }

        ui.showGoodbye();
        ui.close();
    }

    /**
     * Creates a task from what the user typed, stores it, and confirms it.
     *
     * @param ui       used to confirm the change to the user
     * @param tasks    the list of tasks
     * @param command  which of the task-adding commands was used
     * @param argument the text the user typed after the command word
     * @throws GoatException if the description or dates are missing or unreadable
     */
    private static void addTask(Ui ui, ArrayList<Task> tasks, Command command, String argument)
            throws GoatException {
        Task newTask = createTask(command, argument);
        tasks.add(newTask);
        // Save before confirming, so the user is never told a change was made
        // that did not actually reach the disk.
        Storage.save(tasks);
        ui.show("Got it. I've added this task:",
                "  " + newTask,
                "Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Builds the right kind of task for what the user typed.
     * The command word decides the subclass, and the text after it
     * supplies the description and any dates.
     *
     * @param command  which of the task-adding commands was used
     * @param argument everything after the command word, already trimmed
     * @return the new task
     * @throws GoatException if the description or dates are missing, or a
     *                       date is not written in a format Goat understands
     */
    private static Task createTask(Command command, String argument) throws GoatException {
        switch (command) {
        case TODO: {
            if (argument.isEmpty()) {
                throw new GoatException("give descp");
            }
            return new Todo(argument);
        }
        case DEADLINE: {
            // Split once on "/by": everything before it is the description.
            String[] parts = argument.split("/by", 2);
            if (parts.length < 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
                throw new GoatException("give descp and time for deadline, "
                        + "e.g. deadline return book /by 2019-12-02 1800.");
            }
            // The date is turned into a value here, at the edge where the
            // user's text comes in, so that a Deadline can never hold a date
            // that was never understood.
            return new Deadline(parts[0].trim(), DateTimes.parse(parts[1]));
        }
        case EVENT: {
            // Split on "/from" first, then split what follows on "/to".
            String[] fromParts = argument.split("/from", 2);
            String[] toParts = fromParts.length < 2
                    ? new String[0] : fromParts[1].split("/to", 2);
            if (toParts.length < 2 || fromParts[0].trim().isEmpty()
                    || toParts[0].trim().isEmpty() || toParts[1].trim().isEmpty()) {
                throw new GoatException("give descp, start and end for event, "
                        + "e.g. event project meeting /from 2019-12-02 1400 "
                        + "/to 2019-12-02 1600.");
            }
            return new Event(fromParts[0].trim(),
                    DateTimes.parse(toParts[0]), DateTimes.parse(toParts[1]));
        }
        default:
            // Unreachable: only TODO, DEADLINE and EVENT are dispatched here.
            throw new IllegalStateException("not a task-adding command: " + command);
        }
    }

    /**
     * Changes the done status of one task and confirms the change.
     * Marking and unmarking differ only in the value stored and the
     * wording of the reply, so both share this method.
     *
     * @param ui       used to confirm the change to the user
     * @param tasks    the list of tasks
     * @param argument the text the user typed after the command word
     * @param done     the status to store: true for done, false for not done
     * @throws GoatException if no task number was given, or it does not
     *                       refer to a task in the list
     */
    private static void setDone(Ui ui, ArrayList<Task> tasks,
            String argument, boolean done) throws GoatException {
        if (argument.isEmpty()) {
            throw new GoatException("give a number for (un)marking");
        }
        int index = parseTaskNumber(argument, tasks.size());
        if (index < 0) {
            throw new GoatException("no task such as '" + argument + "'.");
        }
        Task task = tasks.get(index);
        if (done) {
            task.markAsDone();
        } else {
            task.markAsNotDone();
        }
        Storage.save(tasks);
        ui.show(done ? "Nice! I've marked this task as done:"
                        : "OK, I've marked this task as not done yet:",
                "  " + task);
    }

    /**
     * Removes one task from the list and confirms what was removed.
     * Everything after the removed task shifts down a place, so the
     * numbers shown by "list" stay contiguous.
     *
     * @param ui       used to confirm the change to the user
     * @param tasks    the list of tasks
     * @param argument the text the user typed after the command word
     * @throws GoatException if no task number was given, or it does not
     *                       refer to a task in the list
     */
    private static void deleteTask(Ui ui, ArrayList<Task> tasks, String argument)
            throws GoatException {
        if (argument.isEmpty()) {
            throw new GoatException("give a number for deleting");
        }
        int index = parseTaskNumber(argument, tasks.size());
        if (index < 0) {
            throw new GoatException("no task such as '" + argument + "'.");
        }
        // remove() hands back what it took out, so it can be shown to the user.
        Task removed = tasks.remove(index);
        Storage.save(tasks);
        ui.show("Noted. I've removed this task:",
                "  " + removed,
                "Now you have " + tasks.size() + " tasks in the list.");
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
     * @param ui    used to show the listing
     * @param tasks the list of tasks
     */
    private static void showTasks(Ui ui, ArrayList<Task> tasks) {
        if (tasks.isEmpty()) {
            ui.show("There is nothing in your list yet.");
            return;
        }
        // One header line, then one line per task.
        String[] lines = new String[tasks.size() + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int i = 0; i < tasks.size(); i++) {
            lines[i + 1] = (i + 1) + "." + tasks.get(i);
        }
        ui.show(lines);
    }
}
