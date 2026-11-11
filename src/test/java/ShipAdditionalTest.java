package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional tests for class Ship (methods not covered by ShipTest).
 * Author: Mafalda Costa (122714)
 * Date: 2026-11-11
 * Cyclomatic Complexity:
 *   - buildShip(): 7
 *   - isInsideBoard(): 4
 *   - getAdjacentPositions(): 5
 *   - sink(): 2
 *   - getPosition(): 1
 *   - toString(): 1
 */
@DisplayName("Additional tests for the Ship class")
class ShipAdditionalTest {

    // ------------------------------------------------------------ buildShip

    @Test
    @DisplayName("buildShip: 'barca' creates a Barge")
    void buildShipBarge() {
        Ship s = Ship.buildShip("barca", Compass.NORTH, new Position(5, 5));
        assertTrue(s instanceof Barge, "Error: expected a Barge.");
    }

    @Test
    @DisplayName("buildShip: 'caravela' creates a Caravel")
    void buildShipCaravel() {
        Ship s = Ship.buildShip("caravela", Compass.NORTH, new Position(5, 5));
        assertTrue(s instanceof Caravel, "Error: expected a Caravel.");
    }

    @Test
    @DisplayName("buildShip: 'nau' creates a Carrack")
    void buildShipCarrack() {
        Ship s = Ship.buildShip("nau", Compass.NORTH, new Position(5, 5));
        assertTrue(s instanceof Carrack, "Error: expected a Carrack.");
    }

    @Test
    @DisplayName("buildShip: 'fragata' creates a Frigate")
    void buildShipFrigate() {
        Ship s = Ship.buildShip("fragata", Compass.NORTH, new Position(5, 5));
        assertTrue(s instanceof Frigate, "Error: expected a Frigate.");
    }

    @Test
    @DisplayName("buildShip: 'galeao' creates a Galleon")
    void buildShipGalleon() {
        Ship s = Ship.buildShip("galeao", Compass.NORTH, new Position(5, 5));
        assertTrue(s instanceof Galleon, "Error: expected a Galleon.");
    }

    @Test
    @DisplayName("buildShip: unknown kind returns null")
    void buildShipUnknown() {
        assertNull(Ship.buildShip("submarino", Compass.NORTH, new Position(5, 5)),
                "Error: an unknown kind should return null.");
    }

    // -------------------------------------------------------- isInsideBoard

    @Test
    @DisplayName("isInsideBoard: ship inside the board (both corners)")
    void isInsideBoard1() {
        assertAll("inside",
                () -> assertTrue(new Barge(Compass.NORTH, new Position(0, 0)).isInsideBoard(),
                        "Error: (0,0) is inside the board."),
                () -> assertTrue(new Barge(Compass.NORTH,
                                new Position(Game.BOARD_SIZE - 1, Game.BOARD_SIZE - 1)).isInsideBoard(),
                        "Error: the last cell is inside the board."));
    }

    @Test
    @DisplayName("isInsideBoard: ship too far to the left")
    void isInsideBoard2() {
        assertFalse(new Barge(Compass.NORTH, new Position(5, -1)).isInsideBoard(),
                "Error: column -1 is outside the board.");
    }

    @Test
    @DisplayName("isInsideBoard: ship too far to the right")
    void isInsideBoard3() {
        assertFalse(new Barge(Compass.NORTH, new Position(5, Game.BOARD_SIZE)).isInsideBoard(),
                "Error: column BOARD_SIZE is outside the board.");
    }

    @Test
    @DisplayName("isInsideBoard: ship too far up")
    void isInsideBoard4() {
        assertFalse(new Barge(Compass.NORTH, new Position(-1, 5)).isInsideBoard(),
                "Error: row -1 is outside the board.");
    }

    @Test
    @DisplayName("isInsideBoard: ship too far down")
    void isInsideBoard5() {
        assertFalse(new Barge(Compass.NORTH, new Position(Game.BOARD_SIZE, 5)).isInsideBoard(),
                "Error: row BOARD_SIZE is outside the board.");
    }

    // ---------------------------------------------------- getAdjacentPositions

    @Test
    @DisplayName("getAdjacentPositions: ship with two positions has no repeated or own cells")
    void getAdjacentPositionsCaravel() {
        Caravel caravel = new Caravel(Compass.NORTH, new Position(5, 5)); // occupies (5,5) and (6,5)
        List<IPosition> adjacent = caravel.getAdjacentPositions();
        assertAll("adjacent positions",
                () -> assertEquals(10, adjacent.size(), "Error: a 2x1 ship in the middle has 10 neighbours."),
                () -> assertFalse(adjacent.contains(new Position(5, 5)), "Error: own cell (5,5) is not adjacent."),
                () -> assertFalse(adjacent.contains(new Position(6, 5)), "Error: own cell (6,5) is not adjacent."),
                () -> assertTrue(adjacent.contains(new Position(4, 4)), "Error: (4,4) should be adjacent."),
                () -> assertTrue(adjacent.contains(new Position(7, 6)), "Error: (7,6) should be adjacent."));
    }

    // ----------------------------------------------------------------- sink

    @Test
    @DisplayName("sink: hits every position and the ship stops floating")
    void sink() {
        Ship ship = new Frigate(Compass.NORTH, new Position(2, 2));
        assertTrue(ship.stillFloating(), "Error: a new ship should be floating.");

        ship.sink();

        assertFalse(ship.stillFloating(), "Error: a sunk ship should not be floating.");
        ship.getPositions().forEach(p -> assertTrue(p.isHit(), "Error: every position should be hit."));
    }

    // ---------------------------------------------- getPosition / toString

    @Test
    @DisplayName("getPosition: returns the position given to the constructor")
    void getPosition() {
        Position p = new Position(5, 5);
        Ship ship = new Barge(Compass.NORTH, p);
        assertSame(p, ship.getPosition(), "Error: getPosition should return the initial position.");
    }

    @Test
    @DisplayName("toString: has the format [category bearing position]")
    void testToString() {
        String s = new Barge(Compass.NORTH, new Position(5, 5)).toString();
        assertTrue(s.startsWith("[Barca ") && s.endsWith("]"),
                "Error: unexpected toString: " + s);
    }
}