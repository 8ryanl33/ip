import java.util.ArrayList;

/**
 * Holds the tasks Goat is keeping track of, and the operations that change them.
 *
 * Before this class existed, {@link Goat} passed a bare {@code ArrayList<Task>}
 * to every helper method. That worked, but it left the list itself with no
 * behaviour of its own: anything holding the list could do anything to it, and
 * "the rules about the task list" were spread across whoever happened to be
 * handling the current command.
 *
 * Wrapping the list gives those rules one home. In particular, the translation
 * from the number the user types (counting from 1) to the index the list uses
 * (counting from 0), and the check that the number refers to a task that really
 * exists, now happen here in {@link #get(int)} and {@link #delete(int)} rather
 * than being repeated by each caller.
 *
 * The wrapped list is kept private and is never handed out, so a task can only
 * be added or removed through the methods below.
 */
public class TaskList {
    /**
     * The tasks, in the order the user sees them.
     *
     * An ArrayList grows as needed and tracks its own size, so there is no
     * fixed cap and no separate counter to keep in step with the contents.
     */
    private final ArrayList<Task> tasks;

    /** Creates an empty task list, for a first run or after a failed load. */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a task list holding tasks that were already built elsewhere,
     * typically the ones {@link Storage#load()} has just read from disk.
     *
     * The list is copied rather than stored directly, so that whoever supplied
     * it cannot go on changing the contents behind this object's back.
     *
     * @param tasks the tasks to start with
     */
    public TaskList(ArrayList<Task> tasks) {
        this.tasks = new ArrayList<>(tasks);
    }

    /**
     * Adds one task to the end of the list.
     *
     * @param task the task to add
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Removes the task with the given number and hands it back, so the caller
     * can show the user what was removed.
     *
     * Everything after the removed task shifts down a place, so the numbers
     * shown by "list" stay contiguous.
     *
     * @param taskNumber the task's position as the user counts it, from 1
     * @return the task that was removed
     * @throws GoatException if no task has that number
     */
    public Task delete(int taskNumber) throws GoatException {
        return tasks.remove(toIndex(taskNumber));
    }

    /**
     * Returns the task with the given number, without removing it.
     *
     * @param taskNumber the task's position as the user counts it, from 1
     * @return the task at that position
     * @throws GoatException if no task has that number
     */
    public Task get(int taskNumber) throws GoatException {
        return tasks.get(toIndex(taskNumber));
    }

    /**
     * Returns how many tasks are stored.
     *
     * @return the number of tasks
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Reports whether there are no tasks at all.
     *
     * @return true if the list is empty
     */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /**
     * Converts a task number as the user counts it into a list index, checking
     * on the way that it refers to a task that exists.
     *
     * This is the single place that knows about the off-by-one between the two
     * ways of counting, which is why every other method here goes through it.
     *
     * @param taskNumber the task's position as the user counts it, from 1
     * @return the matching 0-based index
     * @throws GoatException if the number is outside the list
     */
    private int toIndex(int taskNumber) throws GoatException {
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new GoatException("no task such as '" + taskNumber + "'.");
        }
        return taskNumber - 1;
    }
}
