/**
 * Turns the text the user typed into the values the rest of Goat works with.
 *
 * Everything here answers the same question -- "what does this line mean?" --
 * and nothing here does anything about the answer. That split is the point:
 * {@link Goat} was previously both reading the user's text and acting on it,
 * so a change to the wording of a command (say, allowing "by:" as well as
 * "/by") meant editing the same method that also adds tasks and saves files.
 *
 * Because this class only reads text and never touches the list, the screen or
 * the disk, its methods can be tested by calling them with a string and
 * checking what comes back -- which is what the A-JUnit increment will want.
 *
 * The whole translation now ends in a {@link Command}: {@link #parse} takes a
 * line and hands back an object that knows how to carry that line out, so
 * {@link Goat} never has to look at the user's text at all.
 *
 * The methods are static because there is nothing for an instance to remember:
 * each call is a self-contained translation from one string to one value.
 */
public class Parser {

    /** How a deadline's due date is introduced. */
    private static final String BY_MARKER = "/by";

    /** How an event's start time is introduced. */
    private static final String FROM_MARKER = "/from";

    /** How an event's end time is introduced. */
    private static final String TO_MARKER = "/to";

    /**
     * Turns one line of input into the command it asks for.
     *
     * This is the only method {@link Goat} calls. Everything below it is a
     * step in getting here, and the switch that used to sit in Goat now lives
     * here instead -- which is the right place for it, because choosing a
     * class based on a keyword is a parsing decision.
     *
     * @param fullCommand one whole line as the user typed it
     * @return a command ready to be executed
     * @throws GoatException if the line is not a command Goat knows, or its
     *                       argument is missing or malformed
     */
    public static Command parse(String fullCommand) throws GoatException {
        CommandType commandType = parseCommandType(fullCommand);
        String argument = parseArgument(fullCommand);
        // Arrow labels cannot fall through, so no break is needed.
        return switch (commandType) {
        case BYE -> new ExitCommand();
        case LIST -> new ListCommand();
        case MARK -> new MarkCommand(parseTaskNumberFor(commandType, argument), true);
        case UNMARK -> new MarkCommand(parseTaskNumberFor(commandType, argument), false);
        case DELETE -> new DeleteCommand(parseTaskNumberFor(commandType, argument));
        case TODO, DEADLINE, EVENT -> new AddCommand(parseNewTask(commandType, argument));
        };
    }

    /**
     * Works out which command a line is asking for.
     *
     * @param fullCommand one whole line as the user typed it
     * @return the command named by the line's first word
     * @throws GoatException if the first word is not a command Goat knows
     */
    private static CommandType parseCommandType(String fullCommand) throws GoatException {
        return CommandType.fromKeyword(commandWord(fullCommand));
    }

    /**
     * Returns everything the user typed after the command word.
     *
     * @param fullCommand one whole line as the user typed it
     * @return the rest of the line, with surrounding spaces removed;
     *         an empty string if the line was only a command word
     */
    private static String parseArgument(String fullCommand) {
        String commandWord = commandWord(fullCommand);
        return fullCommand.substring(commandWord.length()).trim();
    }

    /**
     * Reads the number the user typed after "mark", "unmark" or "delete".
     *
     * This method only reads the number. Whether it refers to a task that
     * exists is {@link TaskList}'s business, checked when the command runs.
     *
     * @param argument the text typed after the command word
     * @return the number as the user wrote it, counting from 1
     * @throws GoatException if the text is not a whole number
     */
    private static int parseTaskNumber(String argument) throws GoatException {
        try {
            return Integer.parseInt(argument.trim());
        } catch (NumberFormatException e) {
            throw new GoatException("no task such as '" + argument + "'.");
        }
    }

    /**
     * Reads the task number for a command that needs one, complaining in that
     * command's own words if the user left it out.
     *
     * The wording differs between "mark"/"unmark" and "delete", and only this
     * method knows which command is being read, so the check lives here rather
     * than in {@link #parseTaskNumber}.
     *
     * @param command  the command the number belongs to
     * @param argument the text typed after the command word
     * @return the number as the user wrote it, counting from 1
     * @throws GoatException if the number is missing or is not a whole number
     */
    private static int parseTaskNumberFor(CommandType command, String argument)
            throws GoatException {
        if (argument.isEmpty()) {
            throw new GoatException(command == CommandType.DELETE
                    ? "give a number for deleting"
                    : "give a number for (un)marking");
        }
        return parseTaskNumber(argument);
    }

    /**
     * Builds the right kind of task for what the user typed.
     * The command decides the subclass, and the text after it supplies the
     * description and any dates.
     *
     * The dates are turned into values here, at the edge where the user's text
     * comes in, so that a {@link Deadline} or {@link Event} can never end up
     * holding a date that was never understood.
     *
     * @param command  which of the task-adding commands was used
     * @param argument everything after the command word, already trimmed
     * @return the new task
     * @throws GoatException if the description or dates are missing, or a
     *                       date is not written in a format Goat understands
     */
    private static Task parseNewTask(CommandType command, String argument) throws GoatException {
        switch (command) {
        case TODO:
            return parseTodo(argument);
        case DEADLINE:
            return parseDeadline(argument);
        case EVENT:
            return parseEvent(argument);
        default:
            // Unreachable: only TODO, DEADLINE and EVENT describe a new task.
            throw new IllegalStateException("not a task-adding command: " + command);
        }
    }

    /**
     * Reads a "todo" argument, which is the description and nothing else.
     *
     * @param argument everything after the command word
     * @return the new todo
     * @throws GoatException if no description was given
     */
    private static Todo parseTodo(String argument) throws GoatException {
        if (argument.isEmpty()) {
            throw new GoatException("give descp");
        }
        return new Todo(argument);
    }

    /**
     * Reads a "deadline" argument, which is a description and a due date
     * separated by "/by".
     *
     * @param argument everything after the command word
     * @return the new deadline
     * @throws GoatException if either half is missing, or the date is unreadable
     */
    private static Deadline parseDeadline(String argument) throws GoatException {
        // Split once on "/by": everything before it is the description.
        String[] parts = argument.split(BY_MARKER, 2);
        if (parts.length < 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
            throw new GoatException("give descp and time for deadline, "
                    + "e.g. deadline return book /by 2019-12-02 1800.");
        }
        return new Deadline(parts[0].trim(), DateTimes.parse(parts[1]));
    }

    /**
     * Reads an "event" argument, which is a description, a start and an end,
     * separated by "/from" and "/to".
     *
     * @param argument everything after the command word
     * @return the new event
     * @throws GoatException if any of the three parts is missing, or a date
     *                       is unreadable
     */
    private static Event parseEvent(String argument) throws GoatException {
        // Split on "/from" first, then split what follows on "/to".
        String[] fromParts = argument.split(FROM_MARKER, 2);
        String[] toParts = fromParts.length < 2
                ? new String[0] : fromParts[1].split(TO_MARKER, 2);
        if (toParts.length < 2 || fromParts[0].trim().isEmpty()
                || toParts[0].trim().isEmpty() || toParts[1].trim().isEmpty()) {
            throw new GoatException("give descp, start and end for event, "
                    + "e.g. event project meeting /from 2019-12-02 1400 "
                    + "/to 2019-12-02 1600.");
        }
        return new Event(fromParts[0].trim(),
                DateTimes.parse(toParts[0]), DateTimes.parse(toParts[1]));
    }

    /**
     * Returns the first word of a line.
     *
     * Splitting into the first word and the rest -- rather than into every
     * word -- means a bare "todo" is still recognised as the todo command with
     * a missing description, instead of as some unknown command.
     *
     * @param fullCommand one whole line as the user typed it
     * @return the line's first word, which may be empty if the line was blank
     */
    private static String commandWord(String fullCommand) {
        return fullCommand.split(" ", 2)[0];
    }
}
