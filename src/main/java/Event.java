/**
 * A task that runs from one date or time to another.
 */
public class Event extends Task {
    /** When the event starts, kept as free text the user typed. */
    protected String from;

    /** When the event ends, kept as free text the user typed. */
    protected String to;

    /**
     * Creates an event that starts off not done.
     *
     * @param description the task text as the user typed it
     * @param from        when the event starts
     * @param to          when the event ends
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * @return the task formatted as "[E][X] description (from: start to: end)"
     */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
