package goat.command;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import goat.GoatException;
import goat.storage.Storage;
import goat.task.TaskList;
import goat.ui.Ui;

/**
 * Shows every task in the list.
 *
 * This is the one command that changes nothing, so it never calls
 * {@code storage.save}.
 */
public class ListCommand extends Command {

    /**
     * {@inheritDoc}
     *
     * <p>Shows every task, numbered from 1, or says the list is empty. Nothing
     * is written to disk, since nothing changed.
     *
     * @throws GoatException never in practice: the numbers used below are all
     *                       in range
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws GoatException {
        if (tasks.isEmpty()) {
            ui.show("There is nothing in your list yet.");
            return;
        }
        // One header line, then one line per task, numbered from 1 because
        // that reads more naturally than a 0-based index. Stream.concat
        // joins the header to the numbered lines without a counter or an
        // array index to keep in step.
        String[] lines = Stream.concat(
                Stream.of("Here are the tasks in your list:"),
                IntStream.rangeClosed(1, tasks.size())
                        .mapToObj(taskNumber -> taskNumber + "." + describe(tasks, taskNumber)))
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
