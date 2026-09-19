/**
 * One instruction from the user, already understood and ready to be carried out.
 *
 * Until now {@link Goat} held a switch over {@link CommandType} with one
 * handler method per branch, so adding a command meant editing Goat: a new
 * enum constant, a new switch branch, a new private method. The switch was the
 * kind that keeps growing, and it sat in the class that is meant to be about
 * running the program rather than about any particular command.
 *
 * Each command is now its own class. {@link Parser} decides which one a line
 * asks for and builds it; Goat calls {@link #execute} without knowing or
 * caring which subclass it is holding. Adding a command becomes a matter of
 * writing one new class, and no existing class changes except the one line in
 * Parser that names it.
 *
 * Note the division of labour with {@link CommandType}: the enum answers
 * "which word did the user type?", which is a parsing question, while a
 * Command answers "what should happen?". Keeping them apart is why the enum
 * was renamed in an earlier commit.
 *
 * This class is abstract because "a command" on its own is not a thing that
 * can be carried out -- only a specific one is -- and because {@link #execute}
 * has no sensible default. {@link #isExit()} does have one, so it is given a
 * body here and overridden only by {@link ExitCommand}.
 */
public abstract class Command {

    /**
     * Carries out this command.
     *
     * All three collaborators are passed in rather than stored as fields,
     * because a command is a short-lived description of one instruction: it is
     * built, run once and discarded. Taking them as parameters also states
     * plainly that a command is not allowed to reach for anything else.
     *
     * @param tasks   the task list to read or change
     * @param ui      used to tell the user what happened
     * @param storage used to write the list back to disk after a change
     * @throws GoatException if the command cannot be carried out, for example
     *                       because it names a task that does not exist
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws GoatException;

    /**
     * Reports whether Goat should stop after this command.
     *
     * Only one command ever says yes, so the default is no and
     * {@link ExitCommand} is the single override.
     *
     * @return true if this command ends the program
     */
    public boolean isExit() {
        return false;
    }
}
