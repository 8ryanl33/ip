/**
 * Removes one task from the list.
 */
public class DeleteCommand extends Command {

    /** Which task to remove, as the user counts them, from 1. */
    private final int taskNumber;

    /**
     * @param taskNumber the task's position as the user counts it, from 1
     */
    public DeleteCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws GoatException {
        // delete() checks that the number refers to a real task, and hands
        // back what it took out so it can be shown to the user.
        Task removed = tasks.delete(taskNumber);
        storage.save(tasks);
        ui.show("Noted. I've removed this task:",
                "  " + removed,
                "Now you have " + tasks.size() + " tasks in the list.");
    }
}
