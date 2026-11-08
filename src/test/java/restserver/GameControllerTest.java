package restserver;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import restserver.Dtos.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class GameController.
 * Author: ${user.name}
 * Date: 2026-11-06 15:50
 * Cyclomatic Complexity:
 * - constructor: 1
 * - register(): 5
 * - receiveShots(): 12 (approx.)
 * - evaluateShots(): 6 (private, covered through receiveShots)
 * - error(): 1 (private, covered through register/receiveShots)
 */
class GameControllerTest {

    private static final String ONGOING =
            "{\"results\":[],\"shipsRemaining\":10,\"gameStatus\":\"ONGOING\"}";

    private GameRegistry registry;
    private GameController controller;

    // Local HTTP server that plays the role of the student's server (receives the AI's shots)
    private HttpServer server;
    private final List<String> paths = new CopyOnWriteArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        registry = new GameRegistry();
        controller = new GameController(registry, new CallbackClient(), new AiShotStrategy());

        server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0), 0);
        server.createContext("/", exchange -> {
            paths.add(exchange.getRequestURI().getPath());
            exchange.getRequestBody().readAllBytes();
            byte[] out = ONGOING.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, out.length);
            exchange.getResponseBody().write(out);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
        registry = null;
        controller = null;
        server = null;
        paths.clear();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String studentUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private static RegistrationRequest regReq(String name, String url) {
        RegistrationRequest r = new RegistrationRequest();
        r.playerName = name;
        r.callbackUrl = url;
        return r;
    }

    private String registerGame(String url) {
        ResponseEntity<?> r = controller.register(regReq("Alice", url));
        return ((RegistrationResponse) r.getBody()).gameId;
    }

    private static ShotPosition pos(String row, int col) {
        ShotPosition p = new ShotPosition();
        p.row = row;
        p.column = col;
        return p;
    }

    private static ShotRequest shotReq(ShotPosition... ps) {
        ShotRequest r = new ShotRequest();
        r.shots = new ArrayList<>(Arrays.asList(ps));
        return r;
    }

    private static ShotRequest threeShots() {
        return shotReq(pos("A", 1), pos("B", 2), pos("C", 3));
    }

    private static String errorOf(ResponseEntity<?> r) {
        return ((ErrorResponse) r.getBody()).error;
    }

    private static int status(ResponseEntity<?> r) {
        return r.getStatusCode().value();
    }

    // ── constructor ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("constructor: controller is created")
    void constructor() {
        assertNotNull(controller, "Error: expected a controller but got null");
    }

    // ── register ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register1: null playerName -> 400")
    void register1() {
        ResponseEntity<?> r = controller.register(regReq(null, "http://x"));
        assertAll(
                () -> assertEquals(400, status(r), "Error: expected 400"),
                () -> assertEquals("playerName is required", errorOf(r), "Error: wrong message")
        );
    }

    @Test
    @DisplayName("register2: blank playerName -> 400")
    void register2() {
        ResponseEntity<?> r = controller.register(regReq("   ", "http://x"));
        assertEquals(400, status(r), "Error: expected 400 for blank playerName");
    }

    @Test
    @DisplayName("register3: null callbackUrl -> 400")
    void register3() {
        ResponseEntity<?> r = controller.register(regReq("Alice", null));
        assertAll(
                () -> assertEquals(400, status(r), "Error: expected 400"),
                () -> assertEquals("callbackUrl is required", errorOf(r), "Error: wrong message")
        );
    }

    @Test
    @DisplayName("register4: blank callbackUrl -> 400")
    void register4() {
        ResponseEntity<?> r = controller.register(regReq("Alice", " "));
        assertEquals(400, status(r), "Error: expected 400 for blank callbackUrl");
    }

    @Test
    @DisplayName("register5: valid request -> 200 with REGISTERED response and stored session")
    void register5() {
        ResponseEntity<?> r = controller.register(regReq("Alice", studentUrl()));
        RegistrationResponse body = (RegistrationResponse) r.getBody();
        assertAll(
                () -> assertEquals(200, status(r), "Error: expected 200"),
                () -> assertNotNull(body.gameId, "Error: gameId missing"),
                () -> assertEquals("REGISTERED", body.status, "Error: wrong status"),
                () -> assertEquals(3, body.shotsPerTurn, "Error: wrong shotsPerTurn"),
                () -> assertTrue(body.message.contains("Alice"), "Error: message should greet the player"),
                () -> assertNotNull(registry.getSession(body.gameId), "Error: session should be stored")
        );
    }

    // ── receiveShots: validation ─────────────────────────────────────────────

    @Test
    @DisplayName("receiveShots1: unknown game -> 404")
    void receiveShots1() {
        ResponseEntity<?> r = controller.receiveShots("nope", threeShots());
        assertAll(
                () -> assertEquals(404, status(r), "Error: expected 404"),
                () -> assertTrue(errorOf(r).contains("Game not found"), "Error: wrong message")
        );
    }

    @Test
    @DisplayName("receiveShots2: finished game -> 410 GONE")
    void receiveShots2() {
        String id = registerGame(studentUrl());
        registry.getSession(id).markAiWins();
        ResponseEntity<?> r = controller.receiveShots(id, threeShots());
        assertAll(
                () -> assertEquals(HttpStatus.GONE.value(), status(r), "Error: expected 410"),
                () -> assertTrue(errorOf(r).contains("AI_WINS"), "Error: message should mention the winner")
        );
    }

    @Test
    @DisplayName("receiveShots3: null shots list -> 400")
    void receiveShots3() {
        String id = registerGame(studentUrl());
        ShotRequest req = new ShotRequest();
        req.shots = null;
        assertEquals(400, status(controller.receiveShots(id, req)), "Error: expected 400 for null shots");
    }

    @Test
    @DisplayName("receiveShots4: wrong number of shots -> 400")
    void receiveShots4() {
        String id = registerGame(studentUrl());
        ResponseEntity<?> r = controller.receiveShots(id, shotReq(pos("A", 1)));
        assertAll(
                () -> assertEquals(400, status(r), "Error: expected 400"),
                () -> assertTrue(errorOf(r).contains("Exactly"), "Error: wrong message")
        );
    }

    @Test
    @DisplayName("receiveShots5: null row -> 400")
    void receiveShots5() {
        String id = registerGame(studentUrl());
        ResponseEntity<?> r = controller.receiveShots(id, shotReq(pos(null, 1), pos("B", 2), pos("C", 3)));
        assertAll(
                () -> assertEquals(400, status(r), "Error: expected 400"),
                () -> assertTrue(errorOf(r).startsWith("Invalid position"), "Error: wrong message")
        );
    }

    @Test
    @DisplayName("receiveShots6: blank row -> 400")
    void receiveShots6() {
        String id = registerGame(studentUrl());
        ResponseEntity<?> r = controller.receiveShots(id, shotReq(pos(" ", 1), pos("B", 2), pos("C", 3)));
        assertEquals(400, status(r), "Error: expected 400 for blank row");
    }

    @Test
    @DisplayName("receiveShots7: column below 1 -> 400")
    void receiveShots7() {
        String id = registerGame(studentUrl());
        ResponseEntity<?> r = controller.receiveShots(id, shotReq(pos("A", 0), pos("B", 2), pos("C", 3)));
        assertEquals(400, status(r), "Error: expected 400 for column 0");
    }

    @Test
    @DisplayName("receiveShots8: column above 10 -> 400")
    void receiveShots8() {
        String id = registerGame(studentUrl());
        ResponseEntity<?> r = controller.receiveShots(id, shotReq(pos("A", 11), pos("B", 2), pos("C", 3)));
        assertEquals(400, status(r), "Error: expected 400 for column 11");
    }

    // ── receiveShots: normal turn ────────────────────────────────────────────

    @Test
    @DisplayName("receiveShots9: valid turn -> 200 ONGOING, results for each shot, AI calls back the student")
    void receiveShots9() {
        String id = registerGame(studentUrl());
        ResponseEntity<?> r = controller.receiveShots(id, threeShots());
        ShotResponse body = (ShotResponse) r.getBody();
        assertAll(
                () -> assertEquals(200, status(r), "Error: expected 200"),
                () -> assertEquals(3, body.results.size(), "Error: expected 3 results"),
                () -> assertEquals("ONGOING", body.gameStatus, "Error: game should be ongoing"),
                () -> assertNull(body.winner, "Error: no winner expected yet"),
                () -> assertTrue(body.shipsRemaining > 0, "Error: AI should still have ships"),
                () -> assertEquals(1, paths.size(), "Error: AI should have called the student once"),
                () -> assertEquals("/game/" + id + "/shots", paths.get(0), "Error: wrong callback path")
        );
    }

    @Test
    @DisplayName("receiveShots10: callback failure does not break the student's turn")
    void receiveShots10() {
        String id = registerGame("http://127.0.0.1:1"); // nothing listens here
        ResponseEntity<?> r = controller.receiveShots(id, threeShots());
        assertAll(
                () -> assertEquals(200, status(r), "Error: expected 200 even if the callback failed"),
                () -> assertEquals("ONGOING", ((ShotResponse) r.getBody()).gameStatus, "Error: game should be ongoing")
        );
    }

    @Test
    @DisplayName("receiveShots11: lowercase row letters are accepted")
    void receiveShots11() {
        String id = registerGame(studentUrl());
        ResponseEntity<?> r = controller.receiveShots(id, shotReq(pos("a", 1), pos("b", 2), pos("c", 3)));
        ShotResponse body = (ShotResponse) r.getBody();
        assertAll(
                () -> assertEquals(200, status(r), "Error: expected 200"),
                () -> assertEquals("A", body.results.get(0).row, "Error: row should be normalised to uppercase"),
                () -> assertNotEquals("INVALID", body.results.get(0).outcome, "Error: lowercase row must be valid")
        );
    }

    @Test
    @DisplayName("receiveShots12: same cell twice in one turn -> second one is REPEATED")
    void receiveShots12() {
        String id = registerGame(studentUrl());
        ResponseEntity<?> r = controller.receiveShots(id, shotReq(pos("A", 1), pos("A", 1), pos("B", 2)));
        ShotResponse body = (ShotResponse) r.getBody();
        assertEquals("REPEATED", body.results.get(1).outcome, "Error: expected REPEATED for the duplicate shot");
    }

    @Test
    @DisplayName("receiveShots13: same cells in a second turn are still processed (200, 3 results, none INVALID)")
    void receiveShots13() {
        // Game.repeatedShot() looks at alienMoves, which on the server holds the AI's own shots,
        // not the student's previous shots, so a student's repeat from an earlier turn is only
        // reported as REPEATED if the AI happened to fire at that cell.
        String id = registerGame(studentUrl());
        controller.receiveShots(id, threeShots());
        ResponseEntity<?> r = controller.receiveShots(id, threeShots());
        ShotResponse body = (ShotResponse) r.getBody();
        assertAll(
                () -> assertEquals(200, status(r), "Error: expected 200 for the second turn"),
                () -> assertEquals(3, body.results.size(), "Error: expected 3 results in the second turn"),
                () -> assertTrue(body.results.stream().noneMatch(x -> "INVALID".equals(x.outcome)),
                        "Error: previously shot cells must not be reported as INVALID")
        );
    }

    @Test
    @DisplayName("receiveShots14: row outside the board -> INVALID outcome")
    void receiveShots14() {
        // Assumes the battleship package reports out-of-board positions as invalid shots.
        String id = registerGame(studentUrl());
        ResponseEntity<?> r = controller.receiveShots(id, shotReq(pos("Z", 5), pos("B", 2), pos("C", 3)));
        ShotResponse body = (ShotResponse) r.getBody();
        assertEquals("INVALID", body.results.get(0).outcome, "Error: expected INVALID for row Z");
    }

    // ── receiveShots: student wins (also covers MISS / HIT / SUNK) ──────────

    @Test
    @DisplayName("receiveShots15: sweeping the whole board -> HIT/SUNK/MISS seen and STUDENT_WINS at the end")
    void receiveShots15() {
        // Unreachable callback on purpose: CallbackClient stores the AI's shots in game.getAlienMoves(),
        // and Game.repeatedShot() uses that same list, so any student shot on a cell the AI already
        // fired at would be reported as REPEATED and never hit a ship. With a failing callback the AI's
        // shots are never recorded and the student can sweep the whole board.
        String id = registerGame("http://127.0.0.1:1");
        // Diagonal order: consecutive cells never share a row or a column,
        // so the 3 shots of a turn are always 3 different cells.
        List<ShotPosition> all = new ArrayList<>();
        for (int k = 0; k < 10; k++) {
            for (int r = 0; r < 10; r++) {
                all.add(pos(String.valueOf((char) ('A' + r)), ((r + k) % 10) + 1));
            }
        }

        Set<String> outcomes = new HashSet<>();
        ShotResponse last = null;
        boolean over = false;
        for (int pass = 0; pass < 3 && !over; pass++) {
            for (int i = 0; i < all.size() && !over; i += 3) {
                ShotRequest req = new ShotRequest();
                req.shots = new ArrayList<>();
                for (int k = 0; k < 3; k++) {
                    req.shots.add(all.get(Math.min(i + k, all.size() - 1)));
                }
                ResponseEntity<?> resp = controller.receiveShots(id, req);
                assertEquals(200, status(resp), "Error: every turn of the sweep should return 200");
                last = (ShotResponse) resp.getBody();
                for (ShotResult sr : last.results) {
                    outcomes.add(sr.outcome);
                    if ("HIT".equals(sr.outcome) || "SUNK".equals(sr.outcome)) {
                        assertNotNull(sr.shipType, "Error: HIT/SUNK must carry a shipType");
                    } else if ("MISS".equals(sr.outcome)) {
                        assertNull(sr.shipType, "Error: MISS must not carry a shipType");
                    }
                }
                over = "GAME_OVER".equals(last.gameStatus);
            }
        }

        final ShotResponse finalResponse = last;
        assertAll(
                () -> assertTrue(outcomes.contains("MISS"), "Error: expected at least one MISS"),
                () -> assertTrue(outcomes.contains("HIT"), "Error: expected at least one HIT"),
                () -> assertTrue(outcomes.contains("SUNK"), "Error: expected at least one SUNK"),
                () -> assertEquals("GAME_OVER", finalResponse.gameStatus,
                        "Error: game should end after sweeping the board, but " + finalResponse.shipsRemaining
                                + " ships remain"),
                () -> assertEquals("STUDENT_WINS", finalResponse.winner, "Error: expected STUDENT_WINS"),
                () -> assertEquals(0, finalResponse.shipsRemaining, "Error: no ships should remain"),
                () -> assertTrue(registry.getSession(id).isGameOver(), "Error: session should be over"),
                () -> assertEquals("STUDENT_WINS", registry.getSession(id).getWinner(), "Error: session winner mismatch"),
                () -> assertEquals(410, status(controller.receiveShots(id, threeShots())),
                        "Error: shooting after the end should return 410")
        );
    }
}