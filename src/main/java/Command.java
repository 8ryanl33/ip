/**
 * The set of instructions Goat understands.
 *
 * Each constant pairs the name used internally with the keyword the user
 * actually types. Keeping them in an enum means a command can only be
 * referred to by a name the compiler knows: a mistyped "case LSIT" fails
 * to build, whereas a mistyped string literal would compile and simply
 * never match at runtime.
 */
public enum Command {
    BYE("bye"),
    LIST("list"),
    MARK("mark"),
    UNMARK("unmark"),
    TODO("todo"),
    DEADLINE("deadline"),
    EVENT("event"),
    DELETE("delete");

    /** The word the user types to invoke this command. */
    private final String keyword;

    Command(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Finds the command that a typed word refers to.
     * This is the single place where unrecognised input is rejected.
     *
     * @param keyword the first word of the line the user typed
     * @return the matching command
     * @throws GoatException if no command uses that keyword
     */
    public static Command fromKeyword(String keyword) throws GoatException {
        for (Command command : values()) {
            if (command.keyword.equals(keyword)) {
                return command;
            }
        }
        throw new GoatException("blahhlhahlha");
    }
}
