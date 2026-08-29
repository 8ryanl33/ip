/**
 * A task that must be finished before a given date or time.
 */
public class Deadline extends Task {
    /** When the task is due, kept as free text the user typed. */
    protected String by;

    /**
     * Creates a deadline that starts off not done.
     *
     * @param description the task text as the user typed it
     * @param by          when the task is due
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    /**
     * @return the saved line "D | 1 | description | when"
     */
    @Override
    public String toFileFormat() {
        return "D | " + super.toFileFormat() + " | " + by;
    }

    /**
     * @return the task formatted as "[D][X] description (by: when)"
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
