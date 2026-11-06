package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

/**
 * Test class for Fleet.
 * Author: ${user.name}
 * Date: ${current_date}
 * Time: ${current_time}
 * Cyclomatic Complexity for each method:
 * - Constructor: 1
 * - addShip: 3
 * - getShips: 1
 * - getShipsLike: 2
 * - getFloatingShips: 2
 * - shipAt: 2
 * - isInsideBoard: 3
 * - colisionRisk: 2
 */
	public class FleetTest {

		private Fleet fleet;

		@BeforeEach
		void setUp() {
			fleet = new Fleet();
		}

		@AfterEach
		void tearDown() {
			fleet = null;
		}

		/**
		 * Test for the Fleet constructor.
		 * Cyclomatic Complexity: 1
		 */
		@Test
		@DisplayName("A new fleet is not null and has no ships")
		void testConstructor() {
			assertNotNull(fleet, "Error: Instance of Fleet should not be null.");
			assertTrue(fleet.getShips().isEmpty(), "Error: Fleet should be initialized with empty ships list.");
		}

		@Test
		@DisplayName("createRandom() builds a fleet with FLEET_SIZE ships")
		void testCreateRandom(){
				IFleet r = Fleet.createRandom();
				assertNotNull(r, "Error: createRandom should return a non-null fleet instance.");
				assertEquals(Fleet.FLEET_SIZE, r.getShips().size(), "Error: createRandom should create a fleet with the correct number of ships.");
		}

		/**
		 * Test for the addShip method (all conditions true).
		 * Cyclomatic Complexity: 3
		 */
		@Test
		@DisplayName("addShip() accepts a valid ship inside the board")
		void testAddShip1() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			assertTrue(fleet.addShip(ship), "Error: Valid ship should be added successfully.");
			assertEquals(1, fleet.getShips().size(), "Error: Fleet should contain one ship after addition.");
		}

		/**
		 * Test for the addShip method (fleet size limit reached).
		 */
		@Test
		@DisplayName("addShip() rejects an extra ship at (10,10) after filling column 0")
		void testAddShip2() {
			for (int i = 0; i < Fleet.FLEET_SIZE; i++) {
				fleet.addShip(new Barge(Compass.NORTH, new Position(i, 0)));
			}
			IShip anotherShip = new Barge(Compass.NORTH, new Position(10, 10));
			assertFalse(fleet.addShip(anotherShip), "Error: Should not add ship when fleet size limit is reached.");
		}

		/**
		 * Test for the addShip method (ship outside the board).
		 */
		@Test
		@DisplayName("addShip() rejects a ship outside the board")
		void testAddShip3() {
			IShip shipOutside = new Barge(Compass.NORTH, new Position(99, 99));
			assertFalse(fleet.addShip(shipOutside), "Error: Should not add ship outside the board.");
		}

		/**
		 * Test for the addShip method (collision risk).
		 */
		@Test
		@DisplayName("addShip() rejects a ship with collision risk")
		void testAddShip4() {
			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));  // Overlapping position
			fleet.addShip(ship1);
			assertFalse(fleet.addShip(ship2), "Error: Should not add ship with a collision risk.");
		}

		/**
		 * Test for the getShips method.
		 * Cyclomatic Complexity: 1
		 */
		@Test
		@DisplayName("getShips() returns the ships that were added")
		void testGetShips() {
			assertTrue(fleet.getShips().isEmpty(), "Error: Fleet's ships list should initially be empty.");
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);
			assertEquals(1, fleet.getShips().size(), "Error: Fleet should have size 1 after adding a ship.");
			assertEquals(ship, fleet.getShips().get(0), "Error: Fleet's first ship should match the added ship.");
		}

		/**
		 * Test for the getShipsLike method (ships of specific category).
		 * Cyclomatic Complexity: 2
		 */
		@Test
		@DisplayName("getShipsLike() returns only ships of the given category")
		void testGetShipsLike() {
			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Caravel(Compass.NORTH, new Position(2, 1));
			fleet.addShip(ship1);
			fleet.addShip(ship2);

			List<IShip> barges = fleet.getShipsLike("Barca");
			assertEquals(1, barges.size(), "Error: There should be exactly one ship of category 'Barca'.");
			assertEquals(ship1, barges.get(0), "Error: The ship of category 'Barca' does not match.");
		}

		/**
		 * Test for the getFloatingShips method.
		 * Cyclomatic Complexity: 2
		 */
		@Test
		@DisplayName("getFloatingShips() excludes ships that were sunk")
		void testGetFloatingShips() {
			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Caravel(Compass.NORTH, new Position(4, 4));
			fleet.addShip(ship1);
			fleet.addShip(ship2);

			List<IShip> floatingShips = fleet.getFloatingShips();
			assertEquals(2, floatingShips.size(), "Error: All ships should be floating initially.");

			ship1.getPositions().get(0).shoot();  // Sink ship1
			floatingShips = fleet.getFloatingShips();
			assertEquals(1, floatingShips.size(), "Error: Only one ship should be floating after sinking one.");
			assertEquals(ship2, floatingShips.get(0), "Error: The floating ship should match the expected result.");
		}

		/**
		 * Test for the shipAt method.
		 * Cyclomatic Complexity: 2
		 */
		@Test
		@DisplayName("shipAt() returns the ship at a position, or null if there is none")
		void testShipAt() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);

			assertEquals(ship, fleet.shipAt(new Position(1, 1)), "Error: Should return the correct ship at the position.");
			assertNull(fleet.shipAt(new Position(5, 5)), "Error: Should return null for empty positions in the fleet.");
		}


		/**
		 * Test for private method colisionRisk.
		 * Cyclomatic Complexity: 2
		 */
		@Test
		@DisplayName("colisionRisk() detects overlapping ships")
		void testColisionRisk() throws Exception {
			var method = Fleet.class.getDeclaredMethod("colisionRisk", IShip.class);
			method.setAccessible(true);

			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));  // Overlapping position
			fleet.addShip(ship1);

			assertTrue((Boolean) method.invoke(fleet, ship2), "Error: Overlapping ships should be at collision risk.");
			assertFalse((Boolean) method.invoke(fleet, new Barge(Compass.NORTH, new Position(5, 5))),
					"Error: Ships at non-overlapping positions should not have a collision risk.");
		}

		/**
		 * Test for the printStatus method.
		 * Cyclomatic Complexity: 1
		 */
		@Test
		@DisplayName("printStatus() does not throw")
		void testPrintStatus() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);
			assertDoesNotThrow(fleet::printStatus, "Error: printStatus should not throw any exceptions.");
		}

		// ------------------------------------------------------------------
		// Additional tests (145884 - Lukasz Swiecicki) to maximize coverage
		// ------------------------------------------------------------------

		/** Runs the action while capturing everything printed to System.out. */
		private static String captureOut(Runnable action) {
			PrintStream original = System.out;
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
			try {
				action.run();
			} finally {
				System.setOut(original);
			}
			return buffer.toString(StandardCharsets.UTF_8);
		}

		/** Sinks a ship by shooting every position it occupies. */
		private static void sink(IShip ship) {
			for (IPosition p : ship.getPositions())
				ship.shoot(p);
		}

		@Test
		@DisplayName("addShip() rejects a ship when the fleet already has more than FLEET_SIZE ships")
		void testAddShipWhenFleetIsFull() {
			for (int i = 0; i <= Fleet.FLEET_SIZE; i++)
				fleet.getShips().add(new Barge(Compass.NORTH, new Position(0, 0)));
			assertEquals(Fleet.FLEET_SIZE + 1, fleet.getShips().size());

			assertFalse(fleet.addShip(new Barge(Compass.NORTH, new Position(9, 9))),
					"Error: Should not add a ship when the fleet is over its size limit.");
			assertEquals(Fleet.FLEET_SIZE + 1, fleet.getShips().size());
		}

		@Test
		@DisplayName("addShip() with a null ship fails the assertion")
		void testAddShipNull() {
			assertThrows(AssertionError.class, () -> fleet.addShip(null));
		}

		@Test
		@DisplayName("getShipsLike() returns an empty list when no ship matches the category")
		void testGetShipsLikeNoMatch() {
			fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
			fleet.addShip(new Caravel(Compass.NORTH, new Position(5, 5)));

			assertEquals(2, fleet.getShips().size());
			assertTrue(fleet.getShipsLike("Galeao").isEmpty());
			assertEquals(1, fleet.getShipsLike("Caravela").size());
		}

		@Test
		@DisplayName("getShipsLike() with a null category fails the assertion")
		void testGetShipsLikeNull() {
			assertThrows(AssertionError.class, () -> fleet.getShipsLike(null));
		}

		@Test
		@DisplayName("getSunkShips() is empty while every ship is still floating")
		void testGetSunkShipsNoneSunk() {
			fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
			assertTrue(fleet.getSunkShips().isEmpty());
		}

		@Test
		@DisplayName("getSunkShips() returns only the ships that were sunk")
		void testGetSunkShips() {
			IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
			IShip caravel = new Caravel(Compass.NORTH, new Position(5, 5));
			fleet.addShip(barge);
			fleet.addShip(caravel);

			sink(caravel);

			assertEquals(List.of(caravel), fleet.getSunkShips());
			assertEquals(List.of(barge), fleet.getFloatingShips());
		}

		@Test
		@DisplayName("colisionRisk() with a null ship fails the assertion")
		void testColisionRiskNull() throws Exception {
			var method = Fleet.class.getDeclaredMethod("colisionRisk", IShip.class);
			method.setAccessible(true);

			var thrown = assertThrows(java.lang.reflect.InvocationTargetException.class,
					() -> method.invoke(fleet, (IShip) null));
			assertInstanceOf(AssertionError.class, thrown.getCause());
		}

		@Test
		@DisplayName("shipAt() with a null position fails the assertion")
		void testShipAtNull() {
			assertThrows(AssertionError.class, () -> fleet.shipAt(null));
		}

		@Test
		@DisplayName("printStatus() prints the number of floating and sunk ships")
		void testPrintStatusOutput() {
			IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(barge);
			fleet.addShip(new Caravel(Compass.NORTH, new Position(5, 5)));
			sink(barge);

			String out = captureOut(fleet::printStatus);
			assertTrue(out.contains("Estado da Frota: 1 a flutuar, 1 afundados!"), out);
		}

		@Test
		@DisplayName("printShips() prints every ship of the given list, one per line")
		void testPrintShips() {
			IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
			IShip caravel = new Caravel(Compass.NORTH, new Position(5, 5));

			String out = captureOut(() -> fleet.printShips(List.of(barge, caravel)));
			assertEquals(barge + System.lineSeparator() + caravel + System.lineSeparator(), out);
		}

		@Test
		@DisplayName("printShips() prints nothing for an empty list")
		void testPrintShipsEmpty() {
			assertEquals("", captureOut(() -> fleet.printShips(new ArrayList<>())));
		}

		@Test
		@DisplayName("printShips() with a null list fails the assertion")
		void testPrintShipsNull() {
			assertThrows(AssertionError.class, () -> fleet.printShips(null));
		}

		@Test
		@DisplayName("printShipsByCategory() prints only the ships of that category")
		void testPrintShipsByCategory() {
			IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
			IShip caravel = new Caravel(Compass.NORTH, new Position(5, 5));
			fleet.addShip(barge);
			fleet.addShip(caravel);

			String out = captureOut(() -> fleet.printShipsByCategory("Barca"));
			assertEquals(barge + System.lineSeparator(), out);
		}

		@Test
		@DisplayName("printShipsByCategory() with a null category fails the assertion")
		void testPrintShipsByCategoryNull() {
			assertThrows(AssertionError.class, () -> fleet.printShipsByCategory(null));
		}

		@Test
		@DisplayName("printFloatingShips() prints only the ships still floating")
		void testPrintFloatingShips() {
			IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
			IShip caravel = new Caravel(Compass.NORTH, new Position(5, 5));
			fleet.addShip(barge);
			fleet.addShip(caravel);
			sink(barge);

			String out = captureOut(fleet::printFloatingShips);
			assertEquals(caravel + System.lineSeparator(), out);
		}

		@Test
		@DisplayName("printAllShips() prints every ship of the fleet")
		void testPrintAllShips() {
			IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
			IShip caravel = new Caravel(Compass.NORTH, new Position(5, 5));
			fleet.addShip(barge);
			fleet.addShip(caravel);

			String out = captureOut(fleet::printAllShips);
			assertEquals(barge + System.lineSeparator() + caravel + System.lineSeparator(), out);
		}
	}