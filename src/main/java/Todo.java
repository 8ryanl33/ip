/**
 * A task with no date or time attached to it.
 * It adds nothing to Task beyond its "[T]" label.
 */
public class Todo extends Task {
    /**
     * Creates a todo that starts off not done.
     *
     * @param description the task text as the user typed it
     */
    public Todo(String description) {
        super(description);
    }

    /**
     * @return the task formatted as "[T][X] description"
     */
    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
