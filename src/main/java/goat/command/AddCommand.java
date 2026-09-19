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
     * Creates a command that will add the given task.
     *
     * @param task the task to add to the list
     */
    public AddCommand(Task task) {
        this.task = task;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Appends the task to the list, writes the list to disk, and confirms
     * the addition together with the new total.
     *
     * @throws GoatException if the list cannot be written to disk
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws GoatException {
        tasks.add(task);
        // Save before confirming, so the user is never told a change was made
        // that did not actually reach the disk.
        storage.save(tasks);
        ui.show("On the list:",
                "  " + task,
                "That makes " + tasks.size() + ".");
    }
}
