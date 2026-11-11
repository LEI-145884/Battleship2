package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Tasks.
 * Author: Mafalda Costa (122714)
 * Date: 2026-11-11
 * Cyclomatic Complexity:
 *   - menuHelp(): 1
 *   - readPosition(): 1
 *   - readShip(): 1
 *   - readClassicPosition(): 8
 *   - buildFleet(): 4
 *   - menu(): 21 (only the branches that do not need the GUI nor System.exit)
 */
@DisplayName("Tests for the Tasks class")
class TasksTest {

    private InputStream originalIn;
    private PrintStream originalOut;
    private ByteArrayOutputStream capturedOut;

    @BeforeEach
    void setUp() {
        originalIn = System.in;
        originalOut = System.out;
        capturedOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    private String output() {
        return capturedOut.toString(StandardCharsets.UTF_8);
    }

    private void input(String text) {
        System.setIn(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    private static int count(String text, String fragment) {
        int n = 0;
        int i = 0;
        while ((i = text.indexOf(fragment, i)) >= 0) {
            n++;
            i += fragment.length();
        }
        return n;
    }

    private static String bargeCommand(int row, int column) {
        return "barca " + row + " " + column + " n\n";
    }

    /** FLEET_SIZE barges spaced two cells apart, so none is too close to another. */
    private static String validFleetInput() {
        StringBuilder sb = new StringBuilder();
        int added = 0;
        for (int r = 0; r < Game.BOARD_SIZE && added < Fleet.FLEET_SIZE; r += 2) {
            for (int c = 0; c < Game.BOARD_SIZE && added < Fleet.FLEET_SIZE; c += 2) {
                sb.append(bargeCommand(r, c));
                added++;
            }
        }
        return sb.toString();
    }

    // ------------------------------------------------------------ menuHelp

    @Test
    @DisplayName("menuHelp: prints the header and every menu command")
    void menuHelp() {
        Tasks.menuHelp();
        String out = output();
        assertAll("help text",
                () -> assertTrue(out.contains("AJUDA DO MENU"), "Error: help header is missing."),
                () -> assertTrue(out.contains("gerafrota"), "Error: 'gerafrota' is missing."),
                () -> assertTrue(out.contains("lefrota"), "Error: 'lefrota' is missing."),
                () -> assertTrue(out.contains("estado"), "Error: 'estado' is missing."),
                () -> assertTrue(out.contains("mapa"), "Error: 'mapa' is missing."),
                () -> assertTrue(out.contains("rajada"), "Error: 'rajada' is missing."),
                () -> assertTrue(out.contains("simula"), "Error: 'simula' is missing."),
                () -> assertTrue(out.contains("tiros"), "Error: 'tiros' is missing."),
                () -> assertTrue(out.contains("scoreboard"), "Error: 'scoreboard' is missing."),
                () -> assertTrue(out.contains("desisto"), "Error: 'desisto' is missing."));
    }

    // -------------------------------------------------------- readPosition

    @Test
    @DisplayName("readPosition: reads row and column from the scanner")
    void readPosition() {
        Position p = Tasks.readPosition(new Scanner("3 4"));
        assertAll("position",
                () -> assertEquals(3, p.getRow(), "Error: expected row 3."),
                () -> assertEquals(4, p.getColumn(), "Error: expected column 4."),
                () -> assertEquals(new Position(3, 4), p, "Error: expected position (3,4)."));
    }

    @Test
    @DisplayName("readPosition: non numeric input throws InputMismatchException")
    void readPositionInvalid() {
        assertThrows(InputMismatchException.class, () -> Tasks.readPosition(new Scanner("x 2")),
                "Error: expected InputMismatchException for non numeric row.");
    }

    @Test
    @DisplayName("readPosition: missing input throws NoSuchElementException")
    void readPositionMissing() {
        assertThrows(NoSuchElementException.class, () -> Tasks.readPosition(new Scanner("3")),
                "Error: expected NoSuchElementException when the column is missing.");
    }

    // ------------------------------------------------------------ readShip

    @Test
    @DisplayName("readShip: known ship kind creates the ship")
    void readShip1() {
        Ship ship = Tasks.readShip(new Scanner("barca 3 4 n"));
        assertAll("ship",
                () -> assertNotNull(ship, "Error: a barge should have been created."),
                () -> assertEquals("Barca", ship.getCategory(), "Error: expected category 'Barca'."),
                () -> assertEquals(Compass.charToCompass('n'), ship.getBearing(), "Error: wrong bearing."),
                () -> assertEquals(new Position(3, 4), ship.getPosition(), "Error: wrong position."));
    }

    @Test
    @DisplayName("readShip: unknown ship kind returns null")
    void readShip2() {
        assertNull(Tasks.readShip(new Scanner("xpto 1 1 n")),
                "Error: an unknown ship kind should return null.");
    }

    // ----------------------------------------------- readClassicPosition

    @Test
    @DisplayName("readClassicPosition: 'A3' (letter and number together)")
    void readClassicPosition1() {
        assertEquals(new Position('A', 3), Tasks.readClassicPosition(new Scanner("A3")),
                "Error: expected position A3.");
    }

    @Test
    @DisplayName("readClassicPosition: 'a 3' (lower case, separated by a space)")
    void readClassicPosition2() {
        assertEquals(new Position('A', 3), Tasks.readClassicPosition(new Scanner("a 3")),
                "Error: expected position A3.");
    }

    @Test
    @DisplayName("readClassicPosition: 'B 10' (two digit row)")
    void readClassicPosition3() {
        assertEquals(new Position('B', 10), Tasks.readClassicPosition(new Scanner("B 10")),
                "Error: expected position B10.");
    }

    @Test
    @DisplayName("readClassicPosition: empty input throws IllegalArgumentException")
    void readClassicPosition4() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Tasks.readClassicPosition(new Scanner("")),
                "Error: expected IllegalArgumentException for empty input.");
        assertTrue(ex.getMessage().contains("Nenhuma"), "Error: unexpected message: " + ex.getMessage());
    }

    @Test
    @DisplayName("readClassicPosition: single token with wrong format throws")
    void readClassicPosition5() {
        assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(new Scanner("33")),
                "Error: '33' is not a valid position.");
    }

    @Test
    @DisplayName("readClassicPosition: letter followed by a negative number throws")
    void readClassicPosition6() {
        assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(new Scanner("A -3")),
                "Error: 'A -3' is not a valid position.");
    }

    @Test
    @DisplayName("readClassicPosition: number followed by number throws")
    void readClassicPosition7() {
        assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(new Scanner("1 3")),
                "Error: '1 3' is not a valid position.");
    }

    // ----------------------------------------------------------- buildFleet

    @Test
    @DisplayName("buildFleet: reads valid ships until the fleet is complete")
    void buildFleet1() {
        Fleet fleet = Tasks.buildFleet(new Scanner(validFleetInput()));
        assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size(),
                "Error: the fleet should have FLEET_SIZE ships.");
    }

    @Test
    @DisplayName("buildFleet: skips unknown ships and ships that cannot be added")
    void buildFleet2() {
        String in = "xpto 1 1 n\n"          // unknown ship kind
                + bargeCommand(0, 0)        // valid
                + bargeCommand(0, 0)        // same place: cannot be added
                + validFleetInput();        // enough valid ships to finish
        Fleet fleet = Tasks.buildFleet(new Scanner(in));
        assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size(),
                "Error: the fleet should still end with FLEET_SIZE ships.");
    }

    // ----------------------------------------------------------------- menu

    @Test
    @DisplayName("menu: 'desisto' ends the game with the goodbye message")
    void menu1() {
        input("desisto\n");
        Tasks.menu();
        String out = output();
        assertAll("menu output",
                () -> assertTrue(out.contains("AJUDA DO MENU"), "Error: help should be shown at start."),
                () -> assertTrue(out.contains("Bons ventos!"), "Error: goodbye message is missing."));
    }

    @Test
    @DisplayName("menu: unknown command prints the error message")
    void menu2() {
        input("xpto\ndesisto\n");
        Tasks.menu();
        String out = output();
        assertTrue(out.contains("Que comando") && out.contains("Repete"),
                "Error: unknown command message is missing.");
    }

    @Test
    @DisplayName("menu: 'ajuda' prints the help a second time")
    void menu3() {
        input("ajuda\ndesisto\n");
        Tasks.menu();
        assertEquals(2, count(output(), "AJUDA DO MENU"),
                "Error: help should appear at start and after 'ajuda'.");
    }

    @Test
    @DisplayName("menu: game commands without a fleet are ignored")
    void menu4() {
        input("estado\nmapa\ntiros\nrajada\nsimula\ndesisto\n");
        Tasks.menu();
        String out = output();
        assertAll("no fleet yet",
                () -> assertFalse(out.contains("Que comando"), "Error: all commands are valid commands."),
                () -> assertTrue(out.contains("Bons ventos!"), "Error: the menu should end normally."));
    }

    @Test
    @DisplayName("menu: 'scoreboard' prints the scoreboard")
    void menu5() {
        input("scoreboard\ndesisto\n");
        Tasks.menu();
        assertTrue(output().contains("SCOREBOARD DE JOGOS PASSADOS"),
                "Error: scoreboard header is missing.");
    }
}