package goat.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import goat.GoatException;
import goat.task.Task;
import goat.task.TaskList;
import goat.task.Todo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests reading and writing the save file.
 *
 * Every test writes inside a folder JUnit creates and deletes for it, so the
 * user's real data/goat.txt is never touched. Being able to do that is the
 * reason Storage took its path as a constructor argument in the first place.
 *
 * The damaged-file cases matter more than the happy path: the save file is
 * plain text and a user may well edit it by hand, so Goat has to say which
 * line is at fault rather than crash or silently lose the rest.
 */
public class StorageTest {

    /** Writes the given lines into the temp folder and returns a Storage for them. */
    private static Storage storageContaining(Path folder, String... lines) throws IOException {
        Path file = folder.resolve("goat.txt");
        Files.write(file, java.util.List.of(lines));
        return new Storage(file.toString());
    }

    @Test
    public void load_noFileAtAll_returnsEmptyList(@TempDir Path folder) throws GoatException {
        // Someone's very first run: a missing file is not an error.
        Storage storage = new Storage(folder.resolve("goat.txt").toString());
        assertTrue(storage.load().isEmpty());
    }

    @Test
    public void load_emptyFile_returnsEmptyList(@TempDir Path folder) throws Exception {
        Storage storage = storageContaining(folder);
        assertTrue(storage.load().isEmpty());
    }

    @Test
    public void load_oneOfEachType_readsThemBack(@TempDir Path folder) throws Exception {
        Storage storage = storageContaining(folder,
                "T | 1 | read book",
                "D | 0 | return book | 2019-12-02 1800",
                "E | 0 | project meeting | 2019-08-06 1400 | 2019-08-06 1600");

        ArrayList<Task> tasks = storage.load();

        assertEquals(3, tasks.size());
        assertEquals("[T][X] read book", tasks.get(0).toString());
        assertEquals("[D][ ] return book (by: Dec 02 2019, 6:00pm)", tasks.get(1).toString());
        assertEquals("[E][ ] project meeting (from: Aug 06 2019, 2:00pm to: Aug 06 2019, 4:00pm)",
                tasks.get(2).toString());
    }

    @Test
    public void load_blankLines_skipped(@TempDir Path folder) throws Exception {
        // A trailing newline is the usual source of these.
        Storage storage = storageContaining(folder, "T | 0 | read book", "", "   ");
        assertEquals(1, storage.load().size());
    }

    @Test
    public void load_paddingAroundTheBars_tolerated(@TempDir Path folder) throws Exception {
        Storage storage = storageContaining(folder, "T|0|read book");
        assertEquals("[T][ ] read book", storage.load().get(0).toString());
    }

    @Test
    public void load_unknownTypeLetter_exceptionNamesTheLine(@TempDir Path folder) throws Exception {
        Storage storage = storageContaining(folder, "T | 0 | read book", "X | 0 | mystery");

        GoatException e = assertThrows(GoatException.class, storage::load);

        assertTrue(e.getMessage().contains("damaged on line 2"), e.getMessage());
        assertTrue(e.getMessage().contains("'X'"), e.getMessage());
    }

    @Test
    public void load_tooFewFields_exceptionThrown(@TempDir Path folder) throws Exception {
        Storage storage = storageContaining(folder, "T | 0");
        assertThrows(GoatException.class, storage::load);
    }

    @Test
    public void load_deadlineWithNoDate_exceptionThrown(@TempDir Path folder) throws Exception {
        Storage storage = storageContaining(folder, "D | 0 | return book");
        assertThrows(GoatException.class, storage::load);
    }

    @Test
    public void load_eventWithNoEnd_exceptionThrown(@TempDir Path folder) throws Exception {
        Storage storage = storageContaining(folder, "E | 0 | meeting | 2019-08-06 1400");
        assertThrows(GoatException.class, storage::load);
    }

    @Test
    public void load_dateInTheWrongFormat_exceptionNamesTheLine(@TempDir Path folder)
            throws Exception {
        // The likeliest hand-editing mistake, and the one text-ui-test uses.
        Storage storage = storageContaining(folder,
                "T | 1 | read book", "D | 0 | return book | 2 Dec 2019");

        GoatException e = assertThrows(GoatException.class, storage::load);

        assertTrue(e.getMessage().contains("damaged on line 2"), e.getMessage());
        assertTrue(e.getMessage().contains("'2 Dec 2019'"), e.getMessage());
    }

    @Test
    public void load_doneFlagNotZeroOrOne_exceptionThrown(@TempDir Path folder) throws Exception {
        Storage storage = storageContaining(folder, "T | yes | read book");
        assertThrows(GoatException.class, storage::load);
    }

    @Test
    public void save_thenLoad_roundTripsEveryTaskType(@TempDir Path folder) throws GoatException {
        Storage storage = new Storage(folder.resolve("goat.txt").toString());
        TaskList tasks = new TaskList(new ArrayList<>(java.util.List.of(
                new Todo("read book"),
                new goat.task.Deadline("return book",
                        java.time.LocalDateTime.of(2019, 12, 2, 18, 0)),
                new goat.task.Event("project meeting",
                        java.time.LocalDateTime.of(2019, 8, 6, 14, 0),
                        java.time.LocalDateTime.of(2019, 8, 6, 16, 0)))));
        tasks.get(1).markAsDone();

        storage.save(tasks);
        ArrayList<Task> reloaded = storage.load();

        assertEquals(3, reloaded.size());
        for (int i = 0; i < reloaded.size(); i++) {
            assertEquals(tasks.get(i + 1).toString(), reloaded.get(i).toString());
        }
    }

    @Test
    public void save_emptyList_fileBecomesEmpty(@TempDir Path folder) throws Exception {
        Storage storage = storageContaining(folder, "T | 0 | left over");

        storage.save(new TaskList());

        assertTrue(storage.load().isEmpty());
    }

    @Test
    public void save_folderDoesNotExist_createsIt(@TempDir Path folder) throws GoatException {
        // A fresh checkout has no ./data folder at all.
        Path nested = folder.resolve("data").resolve("goat.txt");
        Storage storage = new Storage(nested.toString());
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));

        storage.save(tasks);

        assertTrue(Files.exists(nested));
        assertEquals(1, storage.load().size());
    }

    @Test
    public void save_calledTwice_replacesRatherThanAppends(@TempDir Path folder)
            throws GoatException {
        Storage storage = new Storage(folder.resolve("goat.txt").toString());
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));

        storage.save(tasks);
        storage.save(tasks);

        assertEquals(1, storage.load().size());
    }
}
