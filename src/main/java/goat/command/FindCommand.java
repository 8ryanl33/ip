package goat.command;

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

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws GoatException {
        TaskList matches = tasks.find(keyword);
        if (matches.isEmpty()) {
            ui.show("There are no matching tasks in your list.");
            return;
        }
        // One header line, then one line per match, numbered from 1 within the
        // results rather than carrying over the numbers from the full list.
        String[] lines = new String[matches.size() + 1];
        lines[0] = "Here are the matching tasks in your list:";
        for (int taskNumber = 1; taskNumber <= matches.size(); taskNumber++) {
            lines[taskNumber] = taskNumber + "." + matches.get(taskNumber);
        }
        ui.show(lines);
    }
}
