package restserver;

import battleship.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import restserver.Dtos.ShotRequest;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class CallbackClient.
 * Author: ${user.name}
 * Date: 2026-11-06 15:50
 * Cyclomatic Complexity:
 * - constructor: 1
 * - sendShots(): 9 (approx.; serialization-error and status < 200 branches cannot be provoked from outside)
 * - recordAiMove(): 6 (private, covered through sendShots)
 */
class CallbackClientTest {

    private static final String ONGOING_OK =
            "{\"results\":[" +
                    "{\"row\":\"A\",\"column\":1,\"outcome\":\"MISS\"}," +
                    "{\"row\":\"G\",\"column\":7,\"outcome\":\"HIT\",\"shipType\":\"caravela\"}," +
                    "{\"row\":\"D\",\"column\":10,\"outcome\":\"SUNK\",\"shipType\":\"galeao\"}]," +
                    "\"shipsRemaining\":8,\"gameStatus\":\"ONGOING\"}";

    private CallbackClient client;
    private GameSession session;
    private ObjectMapper mapper;

    // Local HTTP server that plays the role of the student's server (receives m2a, answers m2b)
    private HttpServer server;
    private final List<String> paths = new CopyOnWriteArrayList<>();
    private final List<String> bodies = new CopyOnWriteArrayList<>();

    @BeforeEach
    void setUp() {
        client = new CallbackClient();
        mapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
        client = null;
        server = null;
        session = null;
        mapper = null;
        paths.clear();
        bodies.clear();
    }

    /** Starts the local server answering every request with the given status/body and creates the session. */
    private void startStudent(int status, String responseBody) throws Exception {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0), 0);
        server.createContext("/", exchange -> {
            paths.add(exchange.getRequestURI().getPath());
            bodies.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] out = responseBody == null ? new byte[0] : responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, out.length == 0 ? -1 : out.length);
            if (out.length > 0) {
                exchange.getResponseBody().write(out);
            }
            exchange.close();
        });
        server.start();
        session = new GameSession("gid", "Alice", "http://127.0.0.1:" + server.getAddress().getPort());
    }

    private static List<IPosition> aiShots() {
        return List.of(new Position(0, 0), new Position(6, 6), new Position(3, 9)); // A1, G7, D10
    }

    @Test
    @DisplayName("constructor: client is created")
    void constructor() {
        assertNotNull(client, "Error: expected an instance but got null");
    }

    @Test
    @DisplayName("sendShots1: valid m2b -> true, correct request sent, move recorded")
    void sendShots1() throws Exception {
        startStudent(200, ONGOING_OK);
        boolean ok = client.sendShots(session, aiShots());
        ShotRequest sent = mapper.readValue(bodies.get(0), ShotRequest.class);
        List<IMove> moves = session.getGame().getAlienMoves();
        assertAll(
                () -> assertTrue(ok, "Error: expected true but got false"),
                () -> assertEquals("/game/gid/shots", paths.get(0), "Error: wrong callback path"),
                () -> assertEquals(3, sent.shots.size(), "Error: expected 3 shots in the request"),
                () -> assertEquals("A", sent.shots.get(0).row, "Error: first row should be A"),
                () -> assertEquals(1, sent.shots.get(0).column, "Error: first column should be 1"),
                () -> assertEquals("G", sent.shots.get(1).row, "Error: second row should be G"),
                () -> assertEquals(7, sent.shots.get(1).column, "Error: second column should be 7"),
                () -> assertEquals("D", sent.shots.get(2).row, "Error: third row should be D"),
                () -> assertEquals(10, sent.shots.get(2).column, "Error: third column should be 10"),
                () -> assertEquals(1, moves.size(), "Error: expected exactly one recorded move"),
                () -> assertFalse(session.isGameOver(), "Error: game must still be ongoing")
        );
    }

    @Test
    @DisplayName("sendShots2: outcomes are mapped to valid/repeated/sunk flags")
    void sendShots2() throws Exception {
        startStudent(200, ONGOING_OK);
        client.sendShots(session, aiShots());
        List<IGame.ShotResult> r = session.getGame().getAlienMoves().get(0).getShotResults();
        assertAll(
                () -> assertEquals(3, r.size(), "Error: expected 3 recorded results"),
                () -> assertTrue(r.get(0).valid() && !r.get(0).repeated() && !r.get(0).sunk(),
                        "Error: MISS should be valid, not repeated, not sunk"),
                () -> assertTrue(r.get(1).valid() && !r.get(1).repeated() && !r.get(1).sunk(),
                        "Error: HIT should be valid, not repeated, not sunk"),
                () -> assertTrue(r.get(2).valid() && !r.get(2).repeated() && r.get(2).sunk(),
                        "Error: SUNK should be valid, not repeated, sunk")
        );
    }

    @Test
    @DisplayName("sendShots3: INVALID and REPEATED outcomes are mapped correctly")
    void sendShots3() throws Exception {
        startStudent(200, "{\"results\":[" +
                "{\"row\":\"A\",\"column\":1,\"outcome\":\"INVALID\"}," +
                "{\"row\":\"G\",\"column\":7,\"outcome\":\"REPEATED\"}," +
                "{\"row\":\"D\",\"column\":10,\"outcome\":\"MISS\"}]," +
                "\"shipsRemaining\":10,\"gameStatus\":\"ONGOING\"}");
        client.sendShots(session, aiShots());
        List<IGame.ShotResult> r = session.getGame().getAlienMoves().get(0).getShotResults();
        assertAll(
                () -> assertFalse(r.get(0).valid(), "Error: INVALID should be recorded as not valid"),
                () -> assertTrue(r.get(1).repeated(), "Error: REPEATED should be recorded as repeated"),
                () -> assertTrue(r.get(2).valid() && !r.get(2).repeated(), "Error: MISS should be valid, not repeated")
        );
    }

    @Test
    @DisplayName("sendShots4: fewer results than shots -> only the available results are recorded")
    void sendShots4() throws Exception {
        startStudent(200, "{\"results\":[{\"row\":\"A\",\"column\":1,\"outcome\":\"MISS\"}]," +
                "\"shipsRemaining\":10,\"gameStatus\":\"ONGOING\"}");
        boolean ok = client.sendShots(session, aiShots());
        assertAll(
                () -> assertTrue(ok, "Error: expected true"),
                () -> assertEquals(1, session.getGame().getAlienMoves().get(0).getShotResults().size(),
                        "Error: expected a single recorded result")
        );
    }

    @Test
    @DisplayName("sendShots5: null results -> true but no move recorded")
    void sendShots5() throws Exception {
        startStudent(200, "{\"shipsRemaining\":4,\"gameStatus\":\"ONGOING\"}");
        boolean ok = client.sendShots(session, aiShots());
        assertAll(
                () -> assertTrue(ok, "Error: expected true"),
                () -> assertTrue(session.getGame().getAlienMoves().isEmpty(), "Error: no move should be recorded"),
                () -> assertFalse(session.isGameOver(), "Error: game must still be ongoing")
        );
    }

    @Test
    @DisplayName("sendShots6: shipsRemaining == 0 -> AI wins")
    void sendShots6() throws Exception {
        startStudent(200, "{\"results\":[],\"shipsRemaining\":0,\"gameStatus\":\"ONGOING\"}");
        boolean ok = client.sendShots(session, aiShots());
        assertAll(
                () -> assertTrue(ok, "Error: expected true"),
                () -> assertTrue(session.isGameOver(), "Error: game should be over"),
                () -> assertEquals("AI_WINS", session.getWinner(), "Error: expected AI_WINS")
        );
    }

    @Test
    @DisplayName("sendShots7: gameStatus GAME_OVER with ships remaining -> AI wins")
    void sendShots7() throws Exception {
        startStudent(200, "{\"results\":[],\"shipsRemaining\":3,\"gameStatus\":\"GAME_OVER\"}");
        boolean ok = client.sendShots(session, aiShots());
        assertAll(
                () -> assertTrue(ok, "Error: expected true"),
                () -> assertTrue(session.isGameOver(), "Error: game should be over"),
                () -> assertEquals("AI_WINS", session.getWinner(), "Error: expected AI_WINS")
        );
    }

    @Test
    @DisplayName("sendShots8: student server unreachable -> false")
    void sendShots8() throws Exception {
        startStudent(200, ONGOING_OK);
        server.stop(0); // port is now closed
        boolean ok = client.sendShots(session, aiShots());
        assertAll(
                () -> assertFalse(ok, "Error: expected false when the server is down"),
                () -> assertTrue(session.getGame().getAlienMoves().isEmpty(), "Error: no move should be recorded")
        );
    }

    @Test
    @DisplayName("sendShots9: HTTP 500 from student -> false")
    void sendShots9() throws Exception {
        startStudent(500, "{\"error\":\"boom\"}");
        assertFalse(client.sendShots(session, aiShots()), "Error: expected false on HTTP 500");
    }

    @Test
    @DisplayName("sendShots10: HTTP 3xx (not followed) -> false")
    void sendShots10() throws Exception {
        startStudent(302, null);
        assertFalse(client.sendShots(session, aiShots()), "Error: expected false on HTTP 302");
    }

    @Test
    @DisplayName("sendShots11: body is not valid JSON -> false")
    void sendShots11() throws Exception {
        startStudent(200, "this is not json");
        boolean ok = client.sendShots(session, aiShots());
        assertAll(
                () -> assertFalse(ok, "Error: expected false for an unparsable m2b"),
                () -> assertTrue(session.getGame().getAlienMoves().isEmpty(), "Error: no move should be recorded")
        );
    }
}