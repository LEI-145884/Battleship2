package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.EnumSet;
import java.util.Set;

/**
 * Test class for Compass.
 * Author: britoeabreu
 * Date: 2023-10-10
 * Time: 15:30
 * Extended to maximize coverage by: 145883 - Kseniya Fedartsova
 * Cyclomatic Complexity for each method:
 * - Constructor: 1
 * - getDirection: 1
 * - toString: 1
 * - charToCompass: 5
 * - randomBearing: 1
 */
public class CompassTest {

	private Compass compass;

	@BeforeEach
	void setUp() {
		compass = Compass.NORTH; // Example instance for testing
	}

	@AfterEach
	void tearDown() {
		compass = null;
	}

	/**
	 * Test for the Compass constructor.
	 * Cyclomatic Complexity: 1
	 */
	@Test
	@DisplayName("Compass constants are not null")
	void constructor() {
		assertNotNull(compass, "Error: Compass instance should not be null.");
	}

	/**
	 * Test for the getDirection method.
	 * Cyclomatic Complexity: 1
	 */
	@Test
	@DisplayName("getDirection() returns the direction letter of each bearing")
	void getDirection() {
		assertEquals('n', Compass.NORTH.getDirection(), "Error: Direction for NORTH should be 'n'.");
		assertEquals('s', Compass.SOUTH.getDirection(), "Error: Direction for SOUTH should be 's'.");
		assertEquals('e', Compass.EAST.getDirection(), "Error: Direction for EAST should be 'e'.");
		assertEquals('o', Compass.WEST.getDirection(), "Error: Direction for WEST should be 'o'.");
	}

	/**
	 * Test for the toString method.
	 * Cyclomatic Complexity: 1
	 */
	@Test
	@DisplayName("toString() returns the direction letter as a string")
	void toStringTest() {
		assertEquals("n", Compass.NORTH.toString(), "Error: String representation for NORTH should be 'n'.");
		assertEquals("s", Compass.SOUTH.toString(), "Error: String representation for SOUTH should be 's'.");
		assertEquals("e", Compass.EAST.toString(), "Error: String representation for EAST should be 'e'.");
		assertEquals("o", Compass.WEST.toString(), "Error: String representation for WEST should be 'o'.");
	}

	/**
	 * Test for the charToCompass method (all conditions true).
	 * Cyclomatic Complexity: 4
	 */
	@Test
	@DisplayName("charToCompass() maps n, s, e, o to the four bearings")
	void charToCompass1() {
		assertEquals(Compass.NORTH, Compass.charToCompass('n'), "Error: 'n' should map to Compass.NORTH.");
		assertEquals(Compass.SOUTH, Compass.charToCompass('s'), "Error: 's' should map to Compass.SOUTH.");
		assertEquals(Compass.EAST, Compass.charToCompass('e'), "Error: 'e' should map to Compass.EAST.");
		assertEquals(Compass.WEST, Compass.charToCompass('o'), "Error: 'o' should map to Compass.WEST.");
	}

	/**
	 * Test for the charToCompass method (invalid input).
	 */
	@Test
	@DisplayName("charToCompass() returns null for an unknown letter")
	void charToCompass2() {
		assertNull(Compass.charToCompass('x'), "Error: 'x' should map to null.");
	}

	/**
	 * Test for the charToCompass method (null input).
	 */
	@Test
	@DisplayName("charToCompass() returns null for the null character")
	void charToCompass3() {
		assertNull(Compass.charToCompass('\0'), "Error: Null character should map to null.");
	}

	/**
	 * Test for the randomBearing method.
	 * Cyclomatic Complexity: 1
	 */
	@Test
	@DisplayName("randomBearing() always returns one of the four bearings and eventually all of them")
	void randomBearing() {
		Set<Compass> seen = EnumSet.noneOf(Compass.class);
		for (int i = 0; i < 1000 && seen.size() < Compass.values().length; i++) {
			Compass bearing = Compass.randomBearing();
			assertNotNull(bearing, "Error: randomBearing should never return null.");
			seen.add(bearing);
		}
		assertEquals(EnumSet.allOf(Compass.class), seen, "Error: randomBearing should be able to return every bearing.");
	}

	/**
	 * Test for the values / valueOf methods generated for the enum.
	 */
	@Test
	@DisplayName("valueOf() returns the constant with the given name")
	void valueOf() {
		assertEquals(4, Compass.values().length);
		assertSame(Compass.WEST, Compass.valueOf("WEST"));
		assertThrows(IllegalArgumentException.class, () -> Compass.valueOf("UP"));
	}
}