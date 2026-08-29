import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the task list from disk and writes it back again.
 *
 * Keeping all file handling in one class means the rest of Goat never has
 * to know where the data lives or how it is encoded; if the format changes,
 * only this class and the tasks' own {@code toFileFormat} methods change.
 *
 * The methods are static because Goat only ever uses one save file, so
 * there is nothing for an instance to remember. An instance-based version
 * (holding the path as a field) would be the way to support several files
 * or to swap in a fake file during testing.
 */
public class Storage {
    /**
     * Where the tasks are stored, relative to the folder the program is run from.
     *
     * Built with {@link Paths#get(String, String...)} from separate name parts
     * rather than written as "./data/goat.txt", so that Java inserts whichever
     * separator the current operating system uses ("/" or "\").
     */
    private static final Path FILE_PATH = Paths.get("data", "goat.txt");

    /**
     * Loads the saved tasks.
     *
     * A missing file is not an error: it simply means nothing has been saved
     * yet, which is exactly the situation on someone's first run.
     *
     * @return the saved tasks, or an empty list if there is no save file
     * @throws GoatException if the file exists but cannot be read or understood
     */
    public static ArrayList<Task> load() throws GoatException {
        ArrayList<Task> tasks = new ArrayList<>();
        if (!Files.exists(FILE_PATH)) {
            return tasks;
        }
        try {
            List<String> lines = Files.readAllLines(FILE_PATH);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) {
                    continue; // Tolerate blank lines, e.g. a trailing newline.
                }
                try {
                    tasks.add(parseTask(line));
                } catch (GoatException e) {
                    // Report which line is at fault: the user may want to fix
                    // it by hand rather than lose the whole file.
                    throw new GoatException("the save file " + FILE_PATH
                            + " is damaged on line " + (i + 1) + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new GoatException("I couldn't read " + FILE_PATH + ": " + e.getMessage());
        }
        return tasks;
    }

    /**
     * Writes the whole task list to disk, replacing whatever was there before.
     *
     * Rewriting the entire file on every change is far simpler than editing
     * one line in place, and a task list is small enough that the cost of
     * doing so is not worth avoiding.
     *
     * @param tasks the tasks to save
     * @throws GoatException if the file or its folder cannot be written
     */
    public static void save(ArrayList<Task> tasks) throws GoatException {
        try {
            // The ./data folder will not exist on a fresh checkout, so create
            // it first. createDirectories does nothing if it is already there.
            Path parent = FILE_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            ArrayList<String> lines = new ArrayList<>();
            for (Task task : tasks) {
                lines.add(task.toFileFormat());
            }
            Files.write(FILE_PATH, lines);
        } catch (IOException e) {
            throw new GoatException("I couldn't save to " + FILE_PATH + ": " + e.getMessage());
        }
    }

    /**
     * Rebuilds one task from the line that was written for it.
     * This is the exact inverse of {@link Task#toFileFormat()}.
     *
     * @param line one line of the save file, already trimmed
     * @return the task that line describes
     * @throws GoatException if the line does not match any known task format
     */
    private static Task parseTask(String line) throws GoatException {
        // split() takes a regular expression, and "|" means "or" in one, so
        // the bar has to be escaped to be treated as a literal character.
        String[] parts = line.split("\\s*\\|\\s*");
        if (parts.length < 3) {
            throw new GoatException("too few fields");
        }
        String type = parts[0];
        String doneFlag = parts[1];
        String description = parts[2];

        Task task;
        switch (type) {
        case "T":
            task = new Todo(description);
            break;
        case "D":
            if (parts.length < 4) {
                throw new GoatException("a deadline needs a due date");
            }
            task = new Deadline(description, parts[3]);
            break;
        case "E":
            if (parts.length < 5) {
                throw new GoatException("an event needs a start and an end");
            }
            task = new Event(description, parts[3], parts[4]);
            break;
        default:
            throw new GoatException("unknown task type '" + type + "'");
        }

        // Tasks are always built as not done, so only "1" needs acting on.
        if (doneFlag.equals("1")) {
            task.markAsDone();
        } else if (!doneFlag.equals("0")) {
            throw new GoatException("the done flag should be 0 or 1, not '" + doneFlag + "'");
        }
        return task;
    }
}
