package goat.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import goat.GoatException;
import goat.parser.DateTimes;
import goat.task.Deadline;
import goat.task.Event;
import goat.task.Task;
import goat.task.TaskList;
import goat.task.Todo;

/**
 * Reads the task list from disk and writes it back again.
 *
 * Keeping all file handling in one class means the rest of Goat never has
 * to know where the data lives or how it is encoded; if the format changes,
 * only this class and the tasks' own {@code toFileFormat} methods change.
 *
 * Each Storage remembers the one file it is responsible for. That was a
 * constant until now, since Goat only ever uses one save file, but holding
 * the path in a field means the caller says where the tasks live instead of
 * this class deciding for everyone -- and a test can point a Storage at a
 * temporary file of its own rather than trampling the real one.
 */
public class Storage {
    /** Where this Storage reads and writes its tasks. */
    private final Path filePath;

    /**
     * Creates a Storage for one save file.
     *
     * Write the path with "/" between the folder names. Java's own file
     * classes accept "/" on every operating system, including Windows, so
     * there is no need to build the path out of separate name parts.
     *
     * @param filePath where the tasks should be kept, e.g. "data/goat.txt"
     */
    public Storage(String filePath) {
        this.filePath = Paths.get(filePath);
    }

    /**
     * Loads the saved tasks.
     *
     * A missing file is not an error: it simply means nothing has been saved
     * yet, which is exactly the situation on someone's first run.
     *
     * @return the saved tasks, or an empty list if there is no save file
     * @throws GoatException if the file exists but cannot be read or understood
     */
    public ArrayList<Task> load() throws GoatException {
        ArrayList<Task> tasks = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return tasks;
        }
        try {
            List<String> lines = Files.readAllLines(filePath);
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
                    throw new GoatException("the save file " + filePath
                            + " is damaged on line " + (i + 1) + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new GoatException("I couldn't read " + filePath + ": " + e.getMessage());
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
    public void save(TaskList tasks) throws GoatException {
        try {
            // The ./data folder will not exist on a fresh checkout, so create
            // it first. createDirectories does nothing if it is already there.
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            // Numbered from 1 rather than streamed over the list directly,
            // because TaskList deliberately does not hand out what it wraps.
            // rangeClosed keeps that constraint while still reading as a
            // mapping from tasks to lines.
            List<String> lines = IntStream.rangeClosed(1, tasks.size())
                    .mapToObj(taskNumber -> toFileLine(tasks, taskNumber))
                    .collect(Collectors.toList());
            Files.write(filePath, lines);
        } catch (IOException e) {
            throw new GoatException("I couldn't save to " + filePath + ": " + e.getMessage());
        }
    }

    /**
     * Returns one task's saved line.
     *
     * A small wrapper so that save() can be written as a mapping: TaskList.get
     * throws a checked exception, which a lambda cannot pass on, and the
     * numbers used here are always in range.
     *
     * @param tasks      the list being saved
     * @param taskNumber the task's position, from 1
     * @return the line to write for that task
     */
    private static String toFileLine(TaskList tasks, int taskNumber) {
        try {
            return tasks.get(taskNumber).toFileFormat();
        } catch (GoatException e) {
            // Unreachable: rangeClosed(1, size) never leaves the list.
            throw new IllegalStateException("task " + taskNumber + " vanished while saving", e);
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
                // DateTimes.parse throws GoatException on a date it cannot read,
                // and load() already turns that into "damaged on line N", so a
                // hand-edited file with a bad date is reported like any other fault.
                task = new Deadline(description, DateTimes.parse(parts[3]));
                break;
            case "E":
                if (parts.length < 5) {
                    throw new GoatException("an event needs a start and an end");
                }
                task = new Event(description, DateTimes.parse(parts[3]), DateTimes.parse(parts[4]));
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
