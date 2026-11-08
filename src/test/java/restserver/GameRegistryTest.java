package restserver;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class GameRegistry.
 * Author: ${user.name}
 * Date: 2026-11-06 15:25
 * Cyclomatic Complexity:
 * - createSession(): 1
 * - getSession(): 1
 * - removeSession(): 1
 */
class GameRegistryTest {

    private GameRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new GameRegistry();
    }

    @AfterEach
    void tearDown() {
        registry = null;
    }

    @Test
    @DisplayName("createSession: creates, stores and returns a session with the given data")
    void createSession() {
        GameSession s1 = registry.createSession("Alice", "http://localhost:9090");
        GameSession s2 = registry.createSession("Bob", "http://localhost:9091");
        assertAll(
                () -> assertNotNull(s1, "Error: expected a session but got null"),
                () -> assertNotNull(s1.getGameId(), "Error: expected a gameId but got null"),
                () -> assertEquals("Alice", s1.getPlayerName(), "Error: expected player Alice"),
                () -> assertEquals("http://localhost:9090", s1.getCallbackUrl(), "Error: wrong callback url"),
                () -> assertNotEquals(s1.getGameId(), s2.getGameId(), "Error: two sessions must have different ids"),
                () -> assertSame(s1, registry.getSession(s1.getGameId()), "Error: session was not stored in the registry")
        );
    }

    @Test
    @DisplayName("getSession: returns the stored session for a known id")
    void getSession() {
        GameSession s = registry.createSession("Alice", "http://localhost:9090");
        assertSame(s, registry.getSession(s.getGameId()), "Error: expected the same session instance");
    }

    @Test
    @DisplayName("getSession: returns null for an unknown id")
    void getSessionUnknown() {
        assertNull(registry.getSession("does-not-exist"), "Error: expected null for an unknown game id");
    }

    @Test
    @DisplayName("removeSession: removes an existing session")
    void removeSession() {
        GameSession s = registry.createSession("Alice", "http://localhost:9090");
        registry.removeSession(s.getGameId());
        assertNull(registry.getSession(s.getGameId()), "Error: session should have been removed");
    }

    @Test
    @DisplayName("removeSession: removing an unknown id does nothing and does not throw")
    void removeSessionUnknown() {
        GameSession s = registry.createSession("Alice", "http://localhost:9090");
        assertDoesNotThrow(() -> registry.removeSession("does-not-exist"),
                "Error: removing an unknown id must not throw");
        assertNotNull(registry.getSession(s.getGameId()), "Error: other sessions must be untouched");
    }
}