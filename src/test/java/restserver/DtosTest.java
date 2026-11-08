package restserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import restserver.Dtos.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Dtos.
 * Author: ${user.name}
 * Date: 2026-11-06 15:25
 * Cyclomatic Complexity:
 * - Dtos constructor: 1
 * - ErrorResponse constructor: 1
 * (remaining nested classes are plain data holders with implicit constructors)
 */
class DtosTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        mapper = null;
    }

    @Test
    @DisplayName("Dtos constructor: outer class can be instantiated")
    void dtosConstructor() {
        assertNotNull(new Dtos(), "Error: expected an instance but got null");
    }

    @Test
    @DisplayName("ErrorResponse constructor: stores the message")
    void errorResponseConstructor() {
        ErrorResponse e = new ErrorResponse("Game not found: abc");
        assertEquals("Game not found: abc", e.error, "Error: message was not stored");
    }

    @Test
    @DisplayName("RegistrationRequest: defaults are null and fields are assignable")
    void registrationRequest() {
        RegistrationRequest r = new RegistrationRequest();
        assertAll(
                () -> assertNull(r.playerName, "Error: playerName should default to null"),
                () -> assertNull(r.callbackUrl, "Error: callbackUrl should default to null")
        );
        r.playerName = "Alice";
        r.callbackUrl = "http://h:1";
        assertAll(
                () -> assertEquals("Alice", r.playerName, "Error: playerName not stored"),
                () -> assertEquals("http://h:1", r.callbackUrl, "Error: callbackUrl not stored")
        );
    }

    @Test
    @DisplayName("RegistrationResponse: fields are assignable")
    void registrationResponse() {
        RegistrationResponse r = new RegistrationResponse();
        r.gameId = "g";
        r.status = "REGISTERED";
        r.shotsPerTurn = 3;
        r.message = "hi";
        assertAll(
                () -> assertEquals("g", r.gameId, "Error: gameId not stored"),
                () -> assertEquals("REGISTERED", r.status, "Error: status not stored"),
                () -> assertEquals(3, r.shotsPerTurn, "Error: shotsPerTurn not stored"),
                () -> assertEquals("hi", r.message, "Error: message not stored")
        );
    }

    @Test
    @DisplayName("ShotPosition and ShotRequest: hold positions")
    void shotRequest() {
        ShotPosition p = new ShotPosition();
        p.row = "B";
        p.column = 3;
        ShotRequest req = new ShotRequest();
        assertNull(req.shots, "Error: shots should default to null");
        req.shots = new ArrayList<>(List.of(p));
        assertAll(
                () -> assertEquals(1, req.shots.size(), "Error: expected 1 shot"),
                () -> assertEquals("B", req.shots.get(0).row, "Error: wrong row"),
                () -> assertEquals(3, req.shots.get(0).column, "Error: wrong column")
        );
    }

    @Test
    @DisplayName("ShotResult and ShotResponse: hold results")
    void shotResponse() {
        ShotResult r = new ShotResult();
        r.row = "G";
        r.column = 7;
        r.outcome = "HIT";
        r.shipType = "caravela";
        ShotResponse resp = new ShotResponse();
        resp.results = List.of(r);
        resp.shipsRemaining = 8;
        resp.gameStatus = "ONGOING";
        assertAll(
                () -> assertNull(resp.winner, "Error: winner should default to null"),
                () -> assertEquals("HIT", resp.results.get(0).outcome, "Error: wrong outcome"),
                () -> assertEquals("caravela", resp.results.get(0).shipType, "Error: wrong shipType"),
                () -> assertEquals(8, resp.shipsRemaining, "Error: wrong shipsRemaining"),
                () -> assertEquals("ONGOING", resp.gameStatus, "Error: wrong gameStatus")
        );
    }

    @Test
    @DisplayName("JSON: ShotRequest survives a serialize/deserialize round trip")
    void jsonRoundTrip() throws Exception {
        ShotPosition p = new ShotPosition();
        p.row = "D";
        p.column = 10;
        ShotRequest req = new ShotRequest();
        req.shots = List.of(p);
        String json = mapper.writeValueAsString(req);
        ShotRequest back = mapper.readValue(json, ShotRequest.class);
        assertAll(
                () -> assertEquals(1, back.shots.size(), "Error: expected 1 shot after round trip"),
                () -> assertEquals("D", back.shots.get(0).row, "Error: row lost in JSON"),
                () -> assertEquals(10, back.shots.get(0).column, "Error: column lost in JSON")
        );
    }

    @Test
    @DisplayName("JSON: ErrorResponse serializes the error field")
    void jsonErrorResponse() throws Exception {
        String json = mapper.writeValueAsString(new ErrorResponse("boom"));
        assertTrue(json.contains("\"error\":\"boom\""), "Error: expected error field in JSON but got " + json);
    }
}