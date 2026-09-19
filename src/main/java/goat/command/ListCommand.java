package goat.command;

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
        // One header line, then one line per task. The tasks are numbered from
        // 1 because that reads more naturally than a 0-based index.
        String[] lines = new String[tasks.size() + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int taskNumber = 1; taskNumber <= tasks.size(); taskNumber++) {
            lines[taskNumber] = taskNumber + "." + tasks.get(taskNumber);
        }
        ui.show(lines);
    }
}
