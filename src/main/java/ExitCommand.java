/**
 * Ends the program.
 *
 * {@link #execute} does nothing at all, which is deliberate: the farewell is
 * owed whether the user typed "bye" or the input simply ran out, so
 * {@link Goat} shows it once after the loop rather than here. All this command
 * has to do is answer yes to {@link #isExit()}.
 */
public class ExitCommand extends Command {

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        // Nothing to do: see the class comment.
    }

    @Override
    public boolean isExit() {
        return true;
    }
}
