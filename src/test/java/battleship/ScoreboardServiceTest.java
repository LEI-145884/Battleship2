package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class ScoreboardService.
 * Author: Mafalda Costa (122714)
 * Date: 2026-11-11
 * Cyclomatic Complexity:
 *   - saveGame(): 2
 *   - loadScores(): 3
 *   - printScoreboard(): 3
 *   - ScoreEntry(): 1
 *   - ScoreEntry(int, int): 1
 */
@DisplayName("Tests for the ScoreboardService class")
class ScoreboardServiceTest {

    private static final Path FILE = Path.of("scoreboard.json");

    private static final String TWO_ENTRIES =
            "[{\"date\":\"2026-01-01 10:00:00\",\"totalShots\":30,\"hits\":20},"
                    + "{\"date\":\"2026-01-02 11:30:00\",\"totalShots\":25,\"hits\":18}]";

    private byte[] backup;
    private PrintStream originalOut;
    private PrintStream originalErr;
    private ByteArrayOutputStream capturedOut;
    private ByteArrayOutputStream capturedErr;

    @BeforeEach
    void setUp() throws IOException {
        // Keep the real scoreboard (if any) and start every test without one
        backup = Files.isRegularFile(FILE) ? Files.readAllBytes(FILE) : null;
        Files.deleteIfExists(FILE);

        originalOut = System.out;
        originalErr = System.err;
        capturedOut = new ByteArrayOutputStream();
        capturedErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(capturedErr, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() throws IOException {
        System.setOut(originalOut);
        System.setErr(originalErr);
        Files.deleteIfExists(FILE);
        if (backup != null) {
            Files.write(FILE, backup);
        }
    }

    private String out() {
        return capturedOut.toString(StandardCharsets.UTF_8);
    }

    private String err() {
        return capturedErr.toString(StandardCharsets.UTF_8);
    }

    // ----------------------------------------------------------- ScoreEntry

    @Test
    @DisplayName("ScoreEntry(): default constructor leaves the fields empty")
    void scoreEntryDefault() {
        ScoreboardService.ScoreEntry e = new ScoreboardService.ScoreEntry();
        assertAll("default entry",
                () -> assertNull(e.date, "Error: date should be null."),
                () -> assertEquals(0, e.totalShots, "Error: totalShots should be 0."),
                () -> assertEquals(0, e.hits, "Error: hits should be 0."));
    }

    @Test
    @DisplayName("ScoreEntry(int, int): stores the values and a formatted date")
    void scoreEntryValues() {
        ScoreboardService.ScoreEntry e = new ScoreboardService.ScoreEntry(10, 5);
        assertAll("entry",
                () -> assertEquals(10, e.totalShots, "Error: totalShots should be 10."),
                () -> assertEquals(5, e.hits, "Error: hits should be 5."),
                () -> assertNotNull(e.date, "Error: date should be set."),
                () -> assertTrue(e.date.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
                        "Error: date has an unexpected format: " + e.date));
    }

    // ----------------------------------------------------------- loadScores

    @Test
    @DisplayName("loadScores: missing file returns an empty list")
    void loadScores1() {
        assertTrue(ScoreboardService.loadScores().isEmpty(),
                "Error: without a file the list should be empty.");
    }

    @Test
    @DisplayName("loadScores: valid JSON file returns its entries")
    void loadScores2() throws IOException {
        Files.writeString(FILE, TWO_ENTRIES);
        List<ScoreboardService.ScoreEntry> scores = ScoreboardService.loadScores();
        assertAll("entries",
                () -> assertEquals(2, scores.size(), "Error: expected 2 entries."),
                () -> assertEquals("2026-01-01 10:00:00", scores.get(0).date, "Error: wrong date."),
                () -> assertEquals(30, scores.get(0).totalShots, "Error: wrong totalShots."),
                () -> assertEquals(20, scores.get(0).hits, "Error: wrong hits."),
                () -> assertEquals(18, scores.get(1).hits, "Error: wrong hits in 2nd entry."));
    }

    @Test
    @DisplayName("loadScores: corrupted file returns an empty list")
    void loadScores3() throws IOException {
        Files.writeString(FILE, "isto nao e json");
        assertTrue(ScoreboardService.loadScores().isEmpty(),
                "Error: a corrupted file should give an empty list.");
    }

    // ------------------------------------------------------------- saveGame

    @Test
    @DisplayName("saveGame: creates the file with the new game")
    void saveGame1() {
        ScoreboardService.saveGame(30, 20);

        List<ScoreboardService.ScoreEntry> scores = ScoreboardService.loadScores();
        assertAll("saved game",
                () -> assertTrue(Files.exists(FILE), "Error: the file should have been created."),
                () -> assertEquals(1, scores.size(), "Error: expected exactly 1 entry."),
                () -> assertEquals(30, scores.get(0).totalShots, "Error: wrong totalShots."),
                () -> assertEquals(20, scores.get(0).hits, "Error: wrong hits."),
                () -> assertTrue(out().contains("guardado com sucesso"), "Error: success message is missing."));
    }

    @Test
    @DisplayName("saveGame: appends to the games already saved")
    void saveGame2() {
        ScoreboardService.saveGame(30, 20);
        ScoreboardService.saveGame(25, 18);

        List<ScoreboardService.ScoreEntry> scores = ScoreboardService.loadScores();
        assertAll("two games",
                () -> assertEquals(2, scores.size(), "Error: expected 2 entries."),
                () -> assertEquals(25, scores.get(1).totalShots, "Error: 2nd entry should be the last saved."));
    }

    @Test
    @DisplayName("saveGame: write error is reported and does not throw")
    void saveGame3() throws IOException {
        // A directory with the name of the file makes both reading and writing fail with IOException
        Files.createDirectory(FILE);

        assertDoesNotThrow(() -> ScoreboardService.saveGame(30, 20),
                "Error: saveGame should handle the IOException.");
        assertTrue(err().contains("Erro ao guardar"), "Error: the error message is missing.");
    }

    // ------------------------------------------------------ printScoreboard

    @Test
    @DisplayName("printScoreboard: no games saved")
    void printScoreboard1() {
        ScoreboardService.printScoreboard();
        String o = out();
        assertAll("empty scoreboard",
                () -> assertTrue(o.contains("SCOREBOARD DE JOGOS PASSADOS"), "Error: header is missing."),
                () -> assertTrue(o.contains("existem jogos registados"), "Error: empty message is missing."));
    }

    @Test
    @DisplayName("printScoreboard: lists every saved game")
    void printScoreboard2() throws IOException {
        Files.writeString(FILE, TWO_ENTRIES);
        ScoreboardService.printScoreboard();
        String o = out();
        assertAll("scoreboard with games",
                () -> assertTrue(o.contains("Jogo #1"), "Error: game 1 is missing."),
                () -> assertTrue(o.contains("Jogo #2"), "Error: game 2 is missing."),
                () -> assertTrue(o.contains("Tiros: 30"), "Error: shots of game 1 are missing."),
                () -> assertTrue(o.contains("Acertos: 18"), "Error: hits of game 2 are missing."),
                () -> assertFalse(o.contains("existem jogos registados"), "Error: should not show the empty message."));
    }
}