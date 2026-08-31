import java.time.LocalDateTime;

/**
 * A task that runs from one date or time to another.
 */
public class Event extends Task {
    /** When the event starts. See {@link Deadline#by} for why it is a LocalDateTime. */
    protected LocalDateTime from;

    /** When the event ends. */
    protected LocalDateTime to;

    /**
     * Creates an event that starts off not done.
     *
     * @param description the task text as the user typed it
     * @param from        when the event starts
     * @param to          when the event ends
     */
    public Event(String description, LocalDateTime from, LocalDateTime to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * @return the saved line "E | 1 | description | 2019-12-02 1400 | 2019-12-02 1600"
     */
    @Override
    public String toFileFormat() {
        return "E | " + super.toFileFormat()
                + " | " + DateTimes.toFileFormat(from)
                + " | " + DateTimes.toFileFormat(to);
    }

    /**
     * @return the task formatted as
     *         "[E][X] description (from: Dec 02 2019, 2:00pm to: Dec 02 2019, 4:00pm)"
     */
    @Override
    public String toString() {
        return "[E]" + super.toString()
                + " (from: " + DateTimes.format(from)
                + " to: " + DateTimes.format(to) + ")";
    }
}
