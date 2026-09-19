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
 * The other three jobs have homes of their own too -- {@link Ui} does the
 * talking, {@link TaskList} holds the tasks, {@link Parser} makes sense of
 * what the user typed -- which leaves this class with what is genuinely its
 * own: deciding which of them to call for each command, and in what order.
 */
public class Goat {

    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();

        // The list starts off holding whatever was saved the last time Goat ran.
        TaskList tasks;
        try {
            tasks = new TaskList(Storage.load());
        } catch (GoatException e) {
            // A save file that cannot be read should not stop the program, but
            // the user is warned, because the next change will overwrite it.
            ui.showLoadingError(e.getMessage());
            tasks = new TaskList();
        }

        // "bye" now ends the loop by clearing this flag, because a break inside
        // the switch below would only leave the switch, not the loop.
        boolean isRunning = true;
        while (isRunning && ui.hasNextCommand()) {
            String fullCommand = ui.readCommand();

            // One catch for the whole dispatch: reading the line and acting on
            // it both just throw when something is wrong, and this decides how
            // the problem is shown. Catching inside the loop means a bad
            // command never ends the program.
            try {
                CommandType command = Parser.parseCommandType(fullCommand);
                String argument = Parser.parseArgument(fullCommand);
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
    private static void addTask(Ui ui, TaskList tasks, CommandType command, String argument)
            throws GoatException {
        Task newTask = Parser.parseNewTask(command, argument);
        tasks.add(newTask);
        // Save before confirming, so the user is never told a change was made
        // that did not actually reach the disk.
        Storage.save(tasks);
        ui.show("Got it. I've added this task:",
                "  " + newTask,
                "Now you have " + tasks.size() + " tasks in the list.");
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
    private static void setDone(Ui ui, TaskList tasks, String argument, boolean done)
            throws GoatException {
        // The "which command was it?" part of this complaint is only known
        // here, which is why the check for a missing number stays out of Parser.
        if (argument.isEmpty()) {
            throw new GoatException("give a number for (un)marking");
        }
        // Parser reads the number; the list checks that it refers to a real task.
        Task task = tasks.get(Parser.parseTaskNumber(argument));
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
     *
     * @param ui       used to confirm the change to the user
     * @param tasks    the list of tasks
     * @param argument the text the user typed after the command word
     * @throws GoatException if no task number was given, or it does not
     *                       refer to a task in the list
     */
    private static void deleteTask(Ui ui, TaskList tasks, String argument)
            throws GoatException {
        if (argument.isEmpty()) {
            throw new GoatException("give a number for deleting");
        }
        // delete() hands back what it took out, so it can be shown to the user.
        Task removed = tasks.delete(Parser.parseTaskNumber(argument));
        Storage.save(tasks);
        ui.show("Noted. I've removed this task:",
                "  " + removed,
                "Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Prints the stored tasks as a numbered list, counting from 1
     * because that reads more naturally than the array's 0-based index.
     *
     * @param ui    used to show the listing
     * @param tasks the list of tasks
     * @throws GoatException never in practice: the numbers below are all in range
     */
    private static void showTasks(Ui ui, TaskList tasks) throws GoatException {
        if (tasks.isEmpty()) {
            ui.show("There is nothing in your list yet.");
            return;
        }
        // One header line, then one line per task.
        String[] lines = new String[tasks.size() + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int taskNumber = 1; taskNumber <= tasks.size(); taskNumber++) {
            lines[taskNumber] = taskNumber + "." + tasks.get(taskNumber);
        }
        ui.show(lines);
    }
}
