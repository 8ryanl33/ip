/**
 * A simple command-line chatbot.
 * Currently it greets the user, stores todos, deadlines and events,
 * lists the stored tasks on request, lets a task be marked as done
 * or deleted, and exits when the user types "bye".
 *
 * Every job the program does now belongs to some other class: {@link Ui} does
 * the talking, {@link TaskList} holds the tasks, {@link Storage} reads and
 * writes the save file, {@link Parser} makes sense of what the user typed, and
 * a {@link Command} carries one instruction out.
 *
 * What is left here is the only thing none of them could own: the shape of a
 * session. Set the four of them up, greet, then read a line and run it, over
 * and over, until something says to stop or the input runs out.
 *
 * Notice that this class no longer mentions a single command by name. Adding
 * one means writing a Command subclass and naming it in Parser; nothing here
 * changes.
 */
public class Goat {

    /** Does all the talking to and reading from the user. */
    private final Ui ui;

    /** The tasks being tracked, loaded at start-up and saved after each change. */
    private final TaskList tasks;

    /** The save file the tasks are read from and written back to. */
    private final Storage storage;

    /**
     * Sets up a chatbot with a task list read from the given file.
     *
     * The constructor is the right place for the loading because a Goat that
     * has not read its tasks is not ready to be used, and a constructor is the
     * one method that is guaranteed to run before anything else can.
     *
     * @param filePath where the tasks are kept, e.g. "data/goat.txt"
     */
    public Goat(String filePath) {
        this.ui = new Ui();
        this.storage = new Storage(filePath);
        // Greet before loading, not after: loading can fail, and a complaint
        // about the save file should not be the first thing the user sees.
        ui.showWelcome();
        // Assigned to a local first because a final field cannot be set twice,
        // and the catch below needs a second go at deciding what it holds.
        TaskList loaded;
        try {
            loaded = new TaskList(storage.load());
        } catch (GoatException e) {
            // A save file that cannot be read should not stop the program, but
            // the user is warned, because the next change will overwrite it.
            ui.showLoadingError(e.getMessage());
            loaded = new TaskList();
        }
        this.tasks = loaded;
    }

    /**
     * Reads and carries out commands until one of them says to stop, or until
     * the input runs out. The greeting has already been shown by the constructor.
     */
    public void run() {
        boolean isExit = false;
        // The second test matters when commands are piped in from a file and
        // the file ends without a "bye".
        while (!isExit && ui.hasNextCommand()) {
            String fullCommand = ui.readCommand();

            // One catch for the whole loop: parsing the line and running it
            // both just throw when something is wrong, and this decides how the
            // problem is shown. Catching inside the loop means a bad command
            // never ends the program.
            try {
                Command command = Parser.parse(fullCommand);
                command.execute(tasks, ui, storage);
                isExit = command.isExit();
            } catch (GoatException e) {
                ui.showError(e.getMessage());
            }
        }

        // Outside the loop, so the farewell is shown whether the user typed
        // "bye" or the input simply ran out.
        ui.showGoodbye();
        ui.close();
    }

    public static void main(String[] args) {
        // The path is named here, at the outermost edge of the program, so it
        // is the one thing a caller has to change to run Goat over a different
        // file -- no class below this line decides where the tasks live.
        new Goat("data/goat.txt").run();
    }
}
