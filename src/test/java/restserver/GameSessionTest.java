package restserver;

import battleship.Game;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class GameSession.
 * Author: ${user.name}
 * Date: 2026-11-06 15:25
 * Cyclomatic Complexity:
 * - constructor: 1
 * - getGameId(): 1
 * - getPlayerName(): 1
 * - getCallbackUrl(): 1
 * - getGame(): 1
 * - getShotsPerTurn(): 1
 * - isGameOver(): 1
 * - getWinner(): 1
 * - markAiWins(): 1
 * - markStudentWins(): 1
 */
class GameSessionTest {

    private GameSession session;

    @BeforeEach
    void setUp() {
        session = new GameSession("id-1", "Alice", "http://localhost:9090");
    }

    @AfterEach
    void tearDown() {
        session = null;
    }

    @Test
    @DisplayName("constructor: initial state is a running game with no winner")
    void constructor() {
        assertAll(
                () -> assertFalse(session.isGameOver(), "Error: new game should not be over"),
                () -> assertNull(session.getWinner(), "Error: new game should have no winner"),
                () -> assertNotNull(session.getGame(), "Error: game should have been created")
        );
    }

    @Test
    @DisplayName("getGameId: returns the id given in the constructor")
    void getGameId() {
        assertEquals("id-1", session.getGameId(), "Error: expected id-1");
    }

    @Test
    @DisplayName("getPlayerName: returns the player name given in the constructor")
    void getPlayerName() {
        assertEquals("Alice", session.getPlayerName(), "Error: expected Alice");
    }

    @Test
    @DisplayName("getCallbackUrl: returns the callback url given in the constructor")
    void getCallbackUrl() {
        assertEquals("http://localhost:9090", session.getCallbackUrl(), "Error: wrong callback url");
    }

    @Test
    @DisplayName("getGame: returns a game with a fleet and no AI moves yet")
    void getGame() {
        assertAll(
                () -> assertNotNull(session.getGame(), "Error: expected a game but got null"),
                () -> assertTrue(session.getGame().getRemainingShips() > 0, "Error: AI fleet should have ships"),
                () -> assertTrue(session.getGame().getAlienMoves().isEmpty(), "Error: no alien moves expected yet")
        );
    }

    @Test
    @DisplayName("getShotsPerTurn: equals Game.NUMBER_SHOTS")
    void getShotsPerTurn() {
        assertEquals(Game.NUMBER_SHOTS, session.getShotsPerTurn(), "Error: shots per turn should be Game.NUMBER_SHOTS");
    }

    @Test
    @DisplayName("isGameOver: false before, true after a winner is marked")
    void isGameOver() {
        assertFalse(session.isGameOver(), "Error: expected false before the game ends");
        session.markAiWins();
        assertTrue(session.isGameOver(), "Error: expected true after the game ends");
    }

    @Test
    @DisplayName("getWinner: null before, winner string afterwards")
    void getWinner() {
        assertNull(session.getWinner(), "Error: expected no winner before the end");
        session.markStudentWins();
        assertEquals("STUDENT_WINS", session.getWinner(), "Error: expected STUDENT_WINS");
    }

    @Test
    @DisplayName("markAiWins: game over and winner AI_WINS")
    void markAiWins() {
        session.markAiWins();
        assertAll(
                () -> assertTrue(session.isGameOver(), "Error: game should be over"),
                () -> assertEquals("AI_WINS", session.getWinner(), "Error: expected AI_WINS")
        );
    }

    @Test
    @DisplayName("markStudentWins: game over and winner STUDENT_WINS")
    void markStudentWins() {
        session.markStudentWins();
        assertAll(
                () -> assertTrue(session.isGameOver(), "Error: game should be over"),
                () -> assertEquals("STUDENT_WINS", session.getWinner(), "Error: expected STUDENT_WINS")
        );
    }
}