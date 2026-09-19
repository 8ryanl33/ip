package goat.command;

import goat.GoatException;
import goat.storage.Storage;
import goat.task.Task;
import goat.task.TaskList;
import goat.ui.Ui;

/**
 * Adds one new task to the list.
 *
 * The task is built by {@link goat.parser.Parser} and handed to this command already
 * finished, which is why one class covers todos, deadlines and events alike:
 * by the time the command exists, the difference between them has already been
 * settled and is the {@link Task} subclass's business, not this class's.
 */
public class AddCommand extends Command {

    /** The task to add, already built from the user's text. */
    private final Task task;

    /**
     * @param task the task to add to the list
     */
    public AddCommand(Task task) {
        this.task = task;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws GoatException {
        tasks.add(task);
        // Save before confirming, so the user is never told a change was made
        // that did not actually reach the disk.
        storage.save(tasks);
        ui.show("Got it. I've added this task:",
                "  " + task,
                "Now you have " + tasks.size() + " tasks in the list.");
    }
}
