package goat.command;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import goat.GoatException;
import goat.storage.Storage;
import goat.task.TaskList;
import goat.ui.Ui;

/**
 * Shows the tasks whose descriptions contain a given keyword.
 *
 * Like {@link ListCommand} this command changes nothing, so it never calls
 * {@code storage.save}. The two are kept apart rather than merged into one
 * "show some tasks" command because what they are asked for differs: list
 * takes no argument and always shows everything, find takes a keyword and can
 * legitimately come back with nothing.
 */
public class FindCommand extends Command {

    /** The text to look for in each task's description. */
    private final String keyword;

    /**
     * Creates a command that will search for the given text.
     *
     * @param keyword the text to look for
     */
    public FindCommand(String keyword) {
        this.keyword = keyword;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Shows the tasks whose descriptions contain the keyword, numbered from
     * 1 within the results, or says there were none. Nothing is written to
     * disk, since nothing changed.
     *
     * @throws GoatException never in practice: the numbers used below are all
     *                       in range
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws GoatException {
        TaskList matches = tasks.find(keyword);
        if (matches.isEmpty()) {
            ui.show("There are no matching tasks in your list.");
            return;
        }
        // One header line, then one line per task, numbered from 1 because
        // that reads more naturally than a 0-based index. Stream.concat
        // joins the header to the numbered lines without a counter or an
        // array index to keep in step.
        String[] lines = Stream.concat(
                Stream.of("Here are the matching tasks in your list:"),
                IntStream.rangeClosed(1, matches.size())
                        .mapToObj(taskNumber -> taskNumber + "." + describe(matches, taskNumber)))
                .toArray(String[]::new);
        ui.show(lines);
    }

    /**
     * Returns one task as it should read on screen.
     *
     * A small wrapper so the numbering above can be written as a mapping:
     * TaskList.get throws a checked exception, which a lambda cannot pass on,
     * and the numbers used are always in range.
     *
     * @param tasks      the tasks being shown
     * @param taskNumber the task's position, from 1
     * @return that task's text
     */
    private static String describe(TaskList tasks, int taskNumber) {
        try {
            return tasks.get(taskNumber).toString();
        } catch (GoatException e) {
            // Unreachable: rangeClosed(1, size) never leaves the list.
            throw new IllegalStateException("task " + taskNumber + " vanished", e);
        }
    }
}
