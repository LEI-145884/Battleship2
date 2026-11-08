package restserver;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class BattleshipServer.
 * Author: ${user.name}
 * Date: 2026-11-06 15:25
 * Cyclomatic Complexity:
 * - constructor: 1
 * - main(): 1
 */
class BattleshipServerTest {

    private BattleshipServer server;

    @BeforeEach
    void setUp() {
        server = new BattleshipServer();
    }

    @AfterEach
    void tearDown() {
        server = null;
    }

    @Test
    @DisplayName("constructor: instance is created")
    void constructor() {
        assertNotNull(server, "Error: expected an instance but got null");
    }

    @Test
    @DisplayName("main: Spring Boot application starts without exceptions (random port)")
    void main() {
        // port 0 = random free port, so it never clashes with a running server on 8080
        assertDoesNotThrow(() -> BattleshipServer.main(new String[]{"--server.port=0"}),
                "Error: application should start without throwing");
    }
}