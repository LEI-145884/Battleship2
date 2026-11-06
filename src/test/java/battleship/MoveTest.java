package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoveTest {

    @Test
    @DisplayName("Test toString method representation")
    void testToString() {
        Move move = new Move(1, Collections.emptyList(), Collections.emptyList());
        String result = move.toString();
        assertTrue(result.contains("number=1"));
        assertTrue(result.contains("shots=0"));
        assertTrue(result.contains("results=0"));
    }

    @Test
    @DisplayName("Test getNumber returns correct value")
    void getNumber() {
        Move move = new Move(5, Collections.emptyList(), Collections.emptyList());
        assertEquals(5, move.getNumber());
    }

    @Test
    @DisplayName("Test getShots returns expected list of positions")
    void getShots() {
        List<IPosition> mockShots = Arrays.asList(new MockPosition(1, 2), new MockPosition(3, 4));
        Move move = new Move(1, mockShots, Collections.emptyList());
        assertEquals(2, move.getShots().size());
        assertEquals(mockShots, move.getShots());
    }

    @Test
    @DisplayName("Test getShotResults returns expected results")
    void getShotResults() {
        Move move = new Move(1, Collections.emptyList(), Collections.emptyList());
        assertNotNull(move.getShotResults());
    }

    @Test
    @DisplayName("Test processEnemyFire logic execution")
    void processEnemyFire() {
        Move move = new Move(1, Collections.emptyList(), Collections.emptyList());
        String jsonResponse = move.processEnemyFire(false);
        assertNotNull(jsonResponse);
        assertTrue(jsonResponse.contains("\"validShots\""));
    }

    // --- Klasy pomocnicze (Mocks) ---

    private static class MockPosition implements IPosition {
        private final int x, y;
        public MockPosition(int x, int y) { this.x = x; this.y = y; }
        @Override public int getRow() { return x; }
        @Override public int getColumn() { return y; }
        @Override public char getClassicRow() { return 'A'; }
        @Override public int getClassicColumn() { return 1; }
        @Override public boolean isInside() { return true; }
        @Override public boolean isAdjacentTo(IPosition other) { return false; }
        @Override public void occupy() {}
        @Override public void shoot() {}
        @Override public boolean isOccupied() { return false; }
        @Override public boolean isHit() { return false; }
        @Override public List<IPosition> adjacentPositions() { return Collections.emptyList(); }
    }

    private static class MockShip implements IShip {
        private final String category;
        public MockShip(String category) { this.category = category; }
        @Override public String getCategory() { return category; }
        @Override public Integer getSize() { return 2; }
        @Override public List<IPosition> getAdjacentPositions() { return Collections.emptyList(); }
        @Override public IPosition getPosition() { return null; }
        @Override public Compass getBearing() { return null; }
        @Override public boolean stillFloating() { return true; }
        @Override public int getTopMostPos() { return 0; }
        @Override public int getBottomMostPos() { return 0; }
        @Override public int getLeftMostPos() { return 0; }
        @Override public int getRightMostPos() { return 0; }
        @Override public boolean occupies(IPosition pos) { return false; }
        @Override public boolean tooCloseTo(IShip other) { return false; }
        @Override public boolean tooCloseTo(IPosition pos) { return false; }
        @Override public boolean isInsideBoard() { return true; }
        @Override public void shoot(IPosition pos) {}
        @Override public void sink() {}
        @Override public List<IPosition> getPositions() { return Collections.emptyList(); }
    }
}