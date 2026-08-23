/**
 * A single item on the user's task list: a description plus
 * whether it has been completed.
 *
 * The fields are protected rather than private so that more specific
 * kinds of task can subclass this later and reuse them directly.
 */
public class Task {
    protected String description;
    protected boolean isDone;

    /**
     * Creates a task that starts off not done, since a task
     * only becomes done once the user says so.
     *
     * @param description the task text as the user typed it
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Returns the symbol shown inside the status box.
     *
     * @return "X" if the task is done, or a single space if it is not
     */
    public String getStatusIcon() {
        return (isDone ? "X" : " "); // mark done task with X
    }

    /** Marks this task as completed. */
    public void markAsDone() {
        this.isDone = true;
    }

    /** Marks this task as not completed, undoing a previous mark. */
    public void markAsNotDone() {
        this.isDone = false;
    }

    /**
     * Renders the task the way it should appear to the user.
     * Overriding toString means a Task can be concatenated straight
     * into a message without the caller formatting it by hand.
     *
     * @return the task formatted as "[X] description" or "[ ] description"
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
