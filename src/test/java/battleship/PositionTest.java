package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test class for Position.
 * Author: britoeabreu
 * Date: 2024-03-19 15:30
 * Extended to maximize coverage by: 145883 - Kseniya Fedartsova
 * Cyclomatic Complexity for each method:
 * - Constructor: 1
 * - getRow: 1
 * - getColumn: 1
 * - isValid: 4
 * - isAdjacentTo: 4
 * - isOccupied: 1
 * - isHit: 1
 * - occupy: 1
 * - shoot: 1
 * - equals: 3
 * - hashCode: 1
 * - toString: 1
 */
public class PositionTest {
	private Position position;

	@BeforeEach
	void setUp() {
		position = new Position(2, 3);
	//	position = new Position('C', 4);
	}

	@AfterEach
	void tearDown() {
		position = null;
	}

	@Test
	@DisplayName("Position(row, column) stores the coordinates and starts free and not hit")
	void constructor() {
		Position pos = new Position(1, 1);
		assertNotNull(pos, "Failed to create Position: object is null");
		assertEquals(1, pos.getRow(), "Failed to set row: expected 1 but got " + pos.getRow());
		assertEquals(1, pos.getColumn(), "Failed to set column: expected 1 but got " + pos.getColumn());
		assertFalse(pos.isOccupied(), "New position should not be occupied");
		assertFalse(pos.isHit(), "New position should not be hit");
	}

	@Test
	@DisplayName("getRow() returns the row")
	void getRow() {
		assertEquals(2, position.getRow(), "Failed to get row: expected 2 but got " + position.getRow());
	}

	@Test
	@DisplayName("getColumn() returns the column")
	void getColumn() {
		assertEquals(3, position.getColumn(), "Failed to get column: expected 3 but got " + position.getColumn());
	}

	@Test
	@DisplayName("getClassicRow() returns the row as a letter (2 -> C)")
	void getClassicRow() {
		assertEquals('C', position.getClassicRow(), "Failed to get row: expected 2 but got " + position.getRow());
	}

	@Test
	@DisplayName("getClassicColumn() returns the 1-based column (3 -> 4)")
	void getClassicColumn() {
		assertEquals(4, position.getClassicColumn(), "Failed to get classic column: expected 4 but got " + position.getClassicColumn());
	}

	@Test
	@DisplayName("isInside() is true for (0,0)")
	void isValid1() {
		position = new Position(0, 0);
		assertTrue(position.isInside(), "Position (0,0) should be valid");
	}

	@Test
	@DisplayName("isInside() is false for a negative row")
	void isValid2() {
		position = new Position(-1, 5);
		assertFalse(position.isInside(), "Position with negative row should be invalid");
	}

	@Test
	@DisplayName("isInside() is false for a negative column")
	void isValid3() {
		position = new Position(5, -1);
		assertFalse(position.isInside(), "Position with negative column should be invalid");
	}

	@Test
	@DisplayName("isInside() is false for row == BOARD_SIZE")
	void isValid4() {
		position = new Position(Game.BOARD_SIZE, 5);
		assertFalse(position.isInside(), "Position with row >= BOARD_SIZE should be invalid");
	}

	@Test
	@DisplayName("isInside() is false for column == BOARD_SIZE")
	void isValid5() {
		position = new Position(5, Game.BOARD_SIZE);
		assertFalse(position.isInside(), "Position with column >= BOARD_SIZE should be invalid");
	}

	@Test
	@DisplayName("isAdjacentTo() detects a horizontal neighbour")
	void isAdjacentTo1() {
		Position other = new Position(2, 4);
		assertTrue(position.isAdjacentTo(other), "Failed to detect horizontally adjacent position");
	}

	@Test
	@DisplayName("isAdjacentTo() detects a vertical neighbour")
	void isAdjacentTo2() {
		Position other = new Position(3, 3);
		assertTrue(position.isAdjacentTo(other), "Failed to detect vertically adjacent position");
	}

	@Test
	@DisplayName("isAdjacentTo() detects a diagonal neighbour")
	void isAdjacentTo3() {
		Position other = new Position(3, 4);
		assertTrue(position.isAdjacentTo(other), "Failed to detect diagonally adjacent position");
	}

	@Test
	@DisplayName("isAdjacentTo() is false for a position two rows away")
	void isAdjacentTo4() {
		Position other = new Position(4, 5);
		assertFalse(position.isAdjacentTo(other), "Non-adjacent position incorrectly identified as adjacent");
	}

	@Test
	@DisplayName("isAdjacentTo(null) throws NullPointerException")
	void isAdjacentToWithNull() {
		assertThrows(NullPointerException.class, () -> position.isAdjacentTo(null),
				"isAdjacentTo should throw NullPointerException for null input");
	}

	@Test
	@DisplayName("occupy() marks the position as occupied")
	void isOccupied() {
		assertFalse(position.isOccupied(), "New position should not be occupied");
		position.occupy();
		assertTrue(position.isOccupied(), "Position should be occupied after occupy()");
	}

	@Test
	@DisplayName("shoot() marks the position as hit")
	void isHit() {
		assertFalse(position.isHit(), "New position should not be hit");
		position.shoot();
		assertTrue(position.isHit(), "Position should be hit after shoot()");
	}

	@Test
	@DisplayName("equals() is true for a position with the same coordinates")
	void equals1() {
		Position same = new Position(2, 3);
		assertTrue(position.equals(same), "Equal positions not identified as equal");
	}

	@Test
	@DisplayName("equals() is false for null")
	void equals2() {
		assertFalse(position.equals(null), "Position should not equal null");
	}

	@Test
	@DisplayName("equals() is false for an object that is not a position")
	void equals3() {
		Object other = new Object();
		assertFalse(position.equals(other), "Position should not equal non-Position object");
	}

	@Test
	@DisplayName("equals() is false for the same row and a different column")
	void equals4() {
		Position other = new Position(2, 4);
		assertFalse(position.equals(other), "Positions with the same row but different column should not be equal");
	}

	@Test
	@DisplayName("equals() is true for the same instance")
	void equals5() {
		assertTrue(position.equals(position), "A position should be equal to itself");
	}

	@Test
	@DisplayName("hashCode() is the same for two equal new positions")
	void hashCodeConsistency() {
		Position same = new Position(2, 3);
		assertEquals(position.hashCode(), same.hashCode(),
				"Hash codes not consistent for equal positions");
	}

	@Test
	@DisplayName("toString() returns the classic notation (C4)")
	void toStringFormat() {
//		String expected = "Row = C, Column = 4";
		String expected = "C4";
		assertEquals(expected, position.toString(),
				"Incorrect string representation: expected '" + expected +
						"' but got '" + position.toString() + "'");
	}

	// ------------------------------------------------------------------
	// Additional tests (145883 - Kseniya Fedartsova) to maximize coverage
	// ------------------------------------------------------------------

	@Nested
	@DisplayName("Position(classicRow, classicColumn)")
	class ClassicConstructor {

		@Test
		@DisplayName("converts the classic notation C4 to row 2, column 3")
		void upperCase() {
			Position pos = new Position('C', 4);
			assertEquals(2, pos.getRow());
			assertEquals(3, pos.getColumn());
			assertEquals('C', pos.getClassicRow());
			assertEquals(4, pos.getClassicColumn());
			assertFalse(pos.isOccupied());
			assertFalse(pos.isHit());
			assertEquals(position, pos);
		}

		@Test
		@DisplayName("accepts a lowercase letter (a1 is row 0, column 0)")
		void lowerCase() {
			Position pos = new Position('a', 1);
			assertEquals(0, pos.getRow());
			assertEquals(0, pos.getColumn());
		}

		@Test
		@DisplayName("J10 is the last position inside the board")
		void lastPosition() {
			Position pos = new Position('J', 10);
			assertEquals(Game.BOARD_SIZE - 1, pos.getRow());
			assertEquals(Game.BOARD_SIZE - 1, pos.getColumn());
			assertTrue(pos.isInside());
			assertEquals("J10", pos.toString());
		}

		@Test
		@DisplayName("K1 and A11 are outside the board")
		void outside() {
			assertFalse(new Position('K', 1).isInside());
			assertFalse(new Position('A', 11).isInside());
		}
	}

	@Test
	@DisplayName("randomPosition() always returns a free, not hit position inside the board")
	void randomPosition() {
		for (int i = 0; i < 1000; i++) {
			Position pos = Position.randomPosition();
			assertTrue(pos.isInside(), pos + " should be inside the board");
			assertFalse(pos.isOccupied());
			assertFalse(pos.isHit());
		}
	}

	@Test
	@DisplayName("isAdjacentTo() is false for the same row but a column two away")
	void isAdjacentToSameRowFarColumn() {
		assertFalse(position.isAdjacentTo(new Position(2, 5)));
		assertFalse(position.isAdjacentTo(new Position(2, 1)));
	}

	@Test
	@DisplayName("isAdjacentTo() is true for the position itself")
	void isAdjacentToItself() {
		assertTrue(position.isAdjacentTo(new Position(2, 3)));
	}

	@Nested
	@DisplayName("adjacentPositions()")
	class AdjacentPositions {

		@Test
		@DisplayName("returns the 8 neighbours of a position in the middle of the board")
		void middle() {
			List<IPosition> adjacents = position.adjacentPositions();

			Set<IPosition> expected = Set.of(
					new Position(1, 2), new Position(1, 3), new Position(1, 4),
					new Position(2, 2), new Position(2, 4),
					new Position(3, 2), new Position(3, 3), new Position(3, 4));
			assertEquals(8, adjacents.size());
			assertEquals(expected, new HashSet<>(adjacents));
			assertFalse(adjacents.contains(position));
		}

		@Test
		@DisplayName("returns only the 3 neighbours inside the board for the corner A1")
		void corner() {
			List<IPosition> adjacents = new Position(0, 0).adjacentPositions();

			assertEquals(Set.of(new Position(0, 1), new Position(1, 0), new Position(1, 1)), new HashSet<>(adjacents));
			assertEquals(3, adjacents.size());
		}

		@Test
		@DisplayName("returns only the 3 neighbours inside the board for the corner J10")
		void oppositeCorner() {
			int last = Game.BOARD_SIZE - 1;
			List<IPosition> adjacents = new Position(last, last).adjacentPositions();

			assertEquals(Set.of(new Position(last - 1, last), new Position(last, last - 1),
					new Position(last - 1, last - 1)), new HashSet<>(adjacents));
		}

		@Test
		@DisplayName("returns the 5 neighbours of a position on the edge of the board")
		void edge() {
			List<IPosition> adjacents = new Position(0, 5).adjacentPositions();

			assertEquals(5, adjacents.size());
			adjacents.forEach(p -> assertTrue(p.isInside(), p + " should be inside the board"));
			adjacents.forEach(p -> assertTrue(new Position(0, 5).isAdjacentTo(p)));
		}

		@Test
		@DisplayName("returns new free positions, even if this position is occupied and hit")
		void neighboursAreFresh() {
			position.occupy();
			position.shoot();

			for (IPosition p : position.adjacentPositions()) {
				assertFalse(p.isOccupied());
				assertFalse(p.isHit());
			}
		}
	}

	@Test
	@DisplayName("equals() is false for the same column and a different row")
	void equalsDifferentRow() {
		assertFalse(position.equals(new Position(3, 3)));
	}

	@Test
	@DisplayName("equals() compares only coordinates, also with other IPosition objects")
	void equalsOtherIPosition() {
		IPosition sameCoordinates = new Position(2, 3);
		sameCoordinates.occupy();
		sameCoordinates.shoot();

		assertTrue(position.equals(sameCoordinates));
	}
}