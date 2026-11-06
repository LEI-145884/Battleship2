package battleship;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Game.
 * Author: britoeabreu
 * Date: 2024-03-19
 * Time: 15:30
 * Extended to maximize coverage by: 145884 - Lukasz Swiecicki
 * Cyclomatic Complexity for each method:
 * - Game (constructor): 1
 * - printBoard (+ printShips, printBoardShots, printBoardFrame): 4 + 4 + 6 + 5
 * - jsonShots: 3
 * - randomEnemyFire: 9
 * - readEnemyFire: 6
 * - fireShots: 3
 * - fireSingleShot: 6
 * - getRepeatedShots / getInvalidShots / getHits / getSunkShips / getRemainingShips: 1
 * - repeatedShot: 3
 * - printMyBoard / printAlienBoard / over: 1
 */
public class GameTest {

	/** Number of repetitions for tests that depend on the random choices of randomEnemyFire(). */
	private static final int RANDOM_REPETITIONS = 30;

	private Game game;

	private PrintStream originalOut;
	private ByteArrayOutputStream out;

	@BeforeEach
	void setUp() {
		originalOut = System.out;
		out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));

		game = new Game(new Fleet()); // Assuming Fleet is a concrete implementation of IFleet
	}

	@AfterEach
	void tearDown() {
		System.setOut(originalOut);
		game = null;
	}

	// ------------------------------------------------------------------
	// Helpers
	// ------------------------------------------------------------------

	/** Everything printed to System.out since the last reset. */
	private String output() {
		return out.toString(StandardCharsets.UTF_8);
	}

	/**
	 * Returns the board character printed for (row, column) in the last board of the output.
	 * A board row is printed as " A | c0 c1 ... c9 |".
	 */
	private char cell(int row, int column) {
		String prefix = " " + (char) ('A' + row) + " |";
		String[] lines = output().split("\\R");
		for (int i = lines.length - 1; i >= 0; i--)
			if (lines[i].startsWith(prefix))
				return lines[i].charAt(prefix.length() + 2 * column + 1);
		throw new AssertionError("Board row " + prefix + " not found in output:\n" + output());
	}

	/** Fires (as the enemy) at every board position except the given ones, three shots per move. */
	private static void fireEverywhereExcept(Game target, Set<IPosition> keep) {
		List<IPosition> toShoot = new ArrayList<>();
		for (int r = 0; r < Game.BOARD_SIZE; r++)
			for (int c = 0; c < Game.BOARD_SIZE; c++) {
				Position p = new Position(r, c);
				if (!keep.contains(p))
					toShoot.add(p);
			}
		for (int i = 0; i < toShoot.size(); i += Game.NUMBER_SHOTS) {
			List<IPosition> move = new ArrayList<>();
			for (int k = 0; k < Game.NUMBER_SHOTS; k++)
				move.add(toShoot.get(Math.min(i + k, toShoot.size() - 1))); // pad the last move by repeating
			target.fireShots(move);
		}
	}

	private static List<IPosition> lastShots(Game target) {
		List<IMove> moves = target.getAlienMoves();
		return moves.get(moves.size() - 1).getShots();
	}

	private static List<Map<String, Object>> parseJson(String json) throws Exception {
		return new ObjectMapper().readValue(json, new TypeReference<>() {});
	}

	// ------------------------------------------------------------------
	// Original tests
	// ------------------------------------------------------------------

	@Test
	@DisplayName("A new game has no moves and all counters at zero")
	void constructor() {
		assertNotNull(game, "Game instance should not be null after construction.");
		assertNotNull(game.getAlienMoves(), "Shots list should not be null after initialization.");
		assertTrue(game.getAlienMoves().isEmpty(), "Shots list should be empty upon initialization.");
		assertEquals(0, game.getInvalidShots(), "Invalid shots count should be zero upon initialization.");
		assertEquals(0, game.getRepeatedShots(), "Repeated shots count should be zero upon initialization.");
		assertEquals(0, game.getHits(), "Hits count should be zero upon initialization.");
		assertEquals(0, game.getSunkShips(), "Sunk ships count should be zero upon initialization.");
	}

	@Test
	@DisplayName("fireSingleShot() counts a shot outside the board as invalid")
	void fire2() {
		Position invalidPosition = new Position(-1, 5);
		game.fireSingleShot(invalidPosition, false);
		assertEquals(1, game.getInvalidShots(), "Invalid shots counter should increase for an invalid shot.");
	}

	@Test
	@DisplayName("fireSingleShot() counts a shot flagged as repeated")
	void fire3() {
		Position position = new Position(2, 3);
		game.fireSingleShot(position, false);
		game.fireSingleShot(position, true);
		assertEquals(1, game.getRepeatedShots(), "Repeated shots counter should increase for a repeated shot.");
	}

	@Test
	@DisplayName("repeatedShot() is true for a position fired in a previous move")
	void repeatedShot1() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		Position position = new Position(2, 3);
		assertTrue(game.repeatedShot(position), "Position (2,3) should be marked as repeated after firing.");
	}

	@Test
	@DisplayName("repeatedShot() is false before any move")
	void repeatedShot2() {
		Position position = new Position(2, 3);
		assertFalse(game.repeatedShot(position), "Position (2,3) should not be marked as repeated before firing.");
	}

	@Test
	@DisplayName("fireShots() adds one move to the alien moves")
	void getAlienMoves() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		assertEquals(1, game.getAlienMoves().size(), "Shots list should contain one shot after firing once.");
	}

	@Test
	@DisplayName("getRemainingShips() counts only the ships still floating")
	void getRemainingShips() {
		IFleet fleet = game.getMyFleet();
		Ship ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		Ship ship2 = new Frigate(Compass.EAST, new Position(5, 5));

		fleet.addShip(ship1);
		assertEquals(1, game.getRemainingShips(), "Just one ship was created!");
		fleet.addShip(ship2);
		assertEquals(2, game.getRemainingShips(), "Two ships were created!");
		ship2.sink();
		assertEquals(1, game.getRemainingShips(), "Remaining ships count should be 1 after sinking one of two ships.");
	}

	// ------------------------------------------------------------------
	// Getters
	// ------------------------------------------------------------------

	@Nested
	@DisplayName("Getters")
	class Getters {

		@Test
		@DisplayName("getMyFleet() returns the fleet given to the constructor")
		void myFleet() {
			IFleet fleet = new Fleet();
			assertSame(fleet, new Game(fleet).getMyFleet());
		}

		@Test
		@DisplayName("getAlienFleet() and getMyMoves() start empty")
		void alienFleetAndMyMoves() {
			assertNotNull(game.getAlienFleet());
			assertTrue(game.getAlienFleet().getShips().isEmpty());
			assertNotNull(game.getMyMoves());
			assertTrue(game.getMyMoves().isEmpty());
		}
	}

	// ------------------------------------------------------------------
	// fireSingleShot / repeatedShot
	// ------------------------------------------------------------------

	@Nested
	@DisplayName("fireSingleShot()")
	class FireSingleShot {

		@Test
		@DisplayName("a shot outside the board returns an invalid result")
		void invalidResult() {
			IGame.ShotResult result = game.fireSingleShot(new Position(10, 0), false);
			assertEquals(new IGame.ShotResult(false, false, null, false), result);
			assertEquals(1, game.getInvalidShots());
		}

		@Test
		@DisplayName("a shot on water returns a valid miss and changes no counter")
		void miss() {
			IGame.ShotResult result = game.fireSingleShot(new Position(4, 4), false);
			assertEquals(new IGame.ShotResult(true, false, null, false), result);
			assertEquals(0, game.getHits());
			assertEquals(0, game.getRepeatedShots());
			assertEquals(0, game.getInvalidShots());
		}

		@Test
		@DisplayName("a position already shot in a previous move counts as repeated")
		void repeatedFromPreviousMove() {
			game.fireShots(List.of(new Position(0, 0), new Position(0, 1), new Position(0, 2)));

			IGame.ShotResult result = game.fireSingleShot(new Position(0, 1), false);
			assertEquals(new IGame.ShotResult(true, true, null, false), result);
			assertEquals(1, game.getRepeatedShots());
		}

		@Test
		@DisplayName("hitting a ship that keeps floating counts a hit but no sink")
		void hitWithoutSinking() {
			Caravel caravel = new Caravel(Compass.NORTH, new Position(5, 5));
			game.getMyFleet().addShip(caravel);
			IPosition target = caravel.getPositions().get(0);

			IGame.ShotResult result = game.fireSingleShot(new Position(target.getRow(), target.getColumn()), false);

			assertTrue(result.valid());
			assertFalse(result.repeated());
			assertSame(caravel, result.ship());
			assertFalse(result.sunk());
			assertEquals(1, game.getHits());
			assertEquals(0, game.getSunkShips());
			assertTrue(caravel.stillFloating());
		}

		@Test
		@DisplayName("hitting the last floating position of a ship sinks it")
		void hitAndSink() {
			Barge barge = new Barge(Compass.NORTH, new Position(3, 3));
			game.getMyFleet().addShip(barge);

			IGame.ShotResult result = game.fireSingleShot(new Position(3, 3), false);

			assertEquals(new IGame.ShotResult(true, false, barge, true), result);
			assertEquals(1, game.getHits());
			assertEquals(1, game.getSunkShips());
			assertEquals(0, game.getRemainingShips());
		}

		@Test
		@DisplayName("a null position fails the assertion")
		void nullPosition() {
			assertThrows(AssertionError.class, () -> game.fireSingleShot(null, false));
		}
	}

	@Nested
	@DisplayName("repeatedShot()")
	class RepeatedShot {

		@Test
		@DisplayName("is false for a position not fired in any previous move")
		void notInPreviousMoves() {
			game.fireShots(List.of(new Position(0, 0), new Position(0, 1), new Position(0, 2)));
			assertFalse(game.repeatedShot(new Position(9, 9)));
		}

		@Test
		@DisplayName("a null position fails the assertion")
		void nullPosition() {
			assertThrows(AssertionError.class, () -> game.repeatedShot(null));
		}
	}

	// ------------------------------------------------------------------
	// fireShots
	// ------------------------------------------------------------------

	@Nested
	@DisplayName("fireShots()")
	class FireShots {

		@Test
		@DisplayName("rejects a move with fewer than NUMBER_SHOTS shots")
		void tooFewShots() {
			assertThrows(IllegalArgumentException.class,
					() -> game.fireShots(List.of(new Position(0, 0), new Position(0, 1))));
			assertTrue(game.getAlienMoves().isEmpty());
		}

		@Test
		@DisplayName("rejects a move with more than NUMBER_SHOTS shots")
		void tooManyShots() {
			assertThrows(IllegalArgumentException.class, () -> game.fireShots(List.of(
					new Position(0, 0), new Position(0, 1), new Position(0, 2), new Position(0, 3))));
		}

		@Test
		@DisplayName("a position repeated inside the same move counts as a repeated shot")
		void repeatedInsideSameMove() {
			game.fireShots(List.of(new Position(1, 1), new Position(1, 1), new Position(1, 2)));
			assertEquals(1, game.getRepeatedShots());
			assertEquals(List.of(false, true, false),
					game.getAlienMoves().get(0).getShotResults().stream().map(IGame.ShotResult::repeated).toList());
		}

		@Test
		@DisplayName("numbers the moves 1, 2, ... and prints the time of each one")
		void numbersMovesAndPrintsTime() {
			game.fireShots(List.of(new Position(0, 0), new Position(0, 1), new Position(0, 2)));
			game.fireShots(List.of(new Position(1, 0), new Position(1, 1), new Position(1, 2)));

			assertEquals(1, game.getAlienMoves().get(0).getNumber());
			assertEquals(2, game.getAlienMoves().get(1).getNumber());
			assertTrue(output().contains("Rajada 1 demorou "), output());
			assertTrue(output().contains("Rajada 2 demorou "), output());
		}

		@Test
		@DisplayName("a null list fails the assertion")
		void nullList() {
			assertThrows(AssertionError.class, () -> game.fireShots(null));
		}
	}

	// ------------------------------------------------------------------
	// readEnemyFire
	// ------------------------------------------------------------------

	@Nested
	@DisplayName("readEnemyFire()")
	class ReadEnemyFire {

		private List<IPosition> read(String line) {
			game.readEnemyFire(new Scanner(line + "\n"));
			return lastShots(game);
		}

		@Test
		@DisplayName("reads three positions written as single tokens (A1 B2 C3)")
		void singleTokens() {
			assertEquals(List.of(new Position('A', 1), new Position('B', 2), new Position('C', 3)),
					read("A1 B2 C3"));
		}

		@Test
		@DisplayName("reads three positions with letter and number separated (A 1 B 2 C 3)")
		void separatedTokens() {
			assertEquals(List.of(new Position('A', 1), new Position('B', 2), new Position('C', 3)),
					read("A 1 B 2 C 3"));
		}

		@Test
		@DisplayName("accepts lowercase letters and mixed formats")
		void lowercaseAndMixed() {
			assertEquals(List.of(new Position('A', 1), new Position('J', 10), new Position('E', 5)),
					read("  a 1 j10 E 5  "));
		}

		@Test
		@DisplayName("ignores positions after the first NUMBER_SHOTS")
		void ignoresExtraPositions() {
			assertEquals(List.of(new Position('A', 1), new Position('B', 2), new Position('C', 3)),
					read("A1 B2 C3 D4"));
		}

		@Test
		@DisplayName("returns the JSON of the shots and registers one move")
		void returnsJson() throws Exception {
			String json = game.readEnemyFire(new Scanner("A1 B2 C3\n"));

			assertEquals(List.of(Map.of("row", "A", "column", 1), Map.of("row", "B", "column", 2),
					Map.of("row", "C", "column", 3)), parseJson(json));
			assertEquals(1, game.getAlienMoves().size());
		}

		@Test
		@DisplayName("a letter not followed by a number is rejected")
		void incompletePosition() {
			IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
					() -> game.readEnemyFire(new Scanner("A B2 C3\n")));
			assertTrue(e.getMessage().contains("'A'"), e.getMessage());
			assertTrue(game.getAlienMoves().isEmpty());
		}

		@Test
		@DisplayName("fewer than NUMBER_SHOTS positions are rejected")
		void tooFewPositions() {
			assertThrows(IllegalArgumentException.class, () -> game.readEnemyFire(new Scanner("A1 B2\n")));
			assertTrue(game.getAlienMoves().isEmpty());
		}

		@Test
		@DisplayName("a null scanner fails the assertion")
		void nullScanner() {
			assertThrows(AssertionError.class, () -> game.readEnemyFire(null));
		}
	}

	// ------------------------------------------------------------------
	// randomEnemyFire
	// ------------------------------------------------------------------

	@Nested
	@DisplayName("randomEnemyFire()")
	class RandomEnemyFire {

		@Test
		@DisplayName("fires NUMBER_SHOTS distinct positions inside the board and returns their JSON")
		void firesDistinctShots() throws Exception {
			String json = game.randomEnemyFire();

			List<IPosition> shots = lastShots(game);
			assertEquals(Game.NUMBER_SHOTS, shots.size());
			assertEquals(Game.NUMBER_SHOTS, new HashSet<>(shots).size());
			shots.forEach(s -> assertTrue(s.isInside(), s + " should be inside the board"));
			assertEquals(Game.NUMBER_SHOTS, parseJson(json).size());
			assertTrue(output().contains("rajada "), output());
		}

		@Test
		@DisplayName("never repeats positions of previous moves or next to sunk ships")
		void avoidsPreviousShotsAndSunkShipNeighbours() {
			for (int i = 0; i < RANDOM_REPETITIONS; i++) {
				Game g = new Game(new Fleet());
				Barge barge = new Barge(Compass.NORTH, new Position(0, 0));
				g.getMyFleet().addShip(barge);

				// keep free: the 3 neighbours of the barge + 3 other positions; (0,0) is shot and the barge sinks
				Set<IPosition> neighbours = Set.of(new Position(0, 1), new Position(1, 0), new Position(1, 1));
				Set<IPosition> expected = Set.of(new Position(9, 7), new Position(9, 8), new Position(9, 9));
				Set<IPosition> keep = new HashSet<>(neighbours);
				keep.addAll(expected);
				fireEverywhereExcept(g, keep);
				assertFalse(barge.stillFloating());

				g.randomEnemyFire();

				assertEquals(expected, new HashSet<>(lastShots(g)));
			}
		}

		@Test
		@DisplayName("with only two free positions fires both and repeats one of them")
		void twoFreePositions() {
			Set<IPosition> free = Set.of(new Position(4, 4), new Position(8, 2));
			for (int i = 0; i < RANDOM_REPETITIONS; i++) {
				Game g = new Game(new Fleet());
				fireEverywhereExcept(g, free);

				g.randomEnemyFire();

				List<IPosition> shots = lastShots(g);
				assertEquals(Game.NUMBER_SHOTS, shots.size());
				assertEquals(free, new HashSet<>(shots));
			}
		}

		@Test
		@DisplayName("with only one free position fires it NUMBER_SHOTS times")
		void oneFreePosition() {
			Position free = new Position(6, 6);
			fireEverywhereExcept(game, Set.of(free));
			int repeatedBefore = game.getRepeatedShots();

			game.randomEnemyFire();

			assertEquals(List.of(free, free, free), lastShots(game));
			assertEquals(repeatedBefore + 2, game.getRepeatedShots());
		}
	}

	// ------------------------------------------------------------------
	// jsonShots
	// ------------------------------------------------------------------

	@Nested
	@DisplayName("jsonShots()")
	class JsonShots {

		@Test
		@DisplayName("serializes each shot with its classic row and column")
		void serializesShots() throws Exception {
			String json = Game.jsonShots(List.of(new Position(0, 0), new Position(9, 9)));

			assertEquals(List.of(Map.of("row", "A", "column", 1), Map.of("row", "J", "column", 10)), parseJson(json));
			assertTrue(output().contains(json));
		}

		@Test
		@DisplayName("serializes an empty list as an empty JSON array")
		void emptyList() throws Exception {
			assertTrue(parseJson(Game.jsonShots(List.of())).isEmpty());
		}

		@Test
		@DisplayName("a null list fails the assertion")
		void nullList() {
			assertThrows(AssertionError.class, () -> Game.jsonShots(null));
		}
	}

	// ------------------------------------------------------------------
	// printBoard / printMyBoard / printAlienBoard / over
	// ------------------------------------------------------------------

	@Nested
	@DisplayName("Board printing")
	class BoardPrinting {

		@Test
		@DisplayName("an empty board shows the column header, the row labels and only water")
		void emptyBoard() {
			game.printAlienBoard(false, false);

			String text = output();
			assertTrue(text.contains("     1 2 3 4 5 6 7 8 9 10"), text);
			assertTrue(text.contains("   +-" + "--".repeat(Game.BOARD_SIZE) + "+"), text);
			assertTrue(text.contains(" J | . . . . . . . . . . |"), text);
			assertFalse(text.contains("LEGENDA"));
		}

		@Test
		@DisplayName("the legend is printed only when requested")
		void legend() {
			game.printAlienBoard(false, true);

			assertTrue(output().contains("LEGENDA"));
			assertTrue(output().contains("'#'->navio"));
			assertTrue(output().contains("'*'->Tiro certeiro"));
		}

		@Test
		@DisplayName("floating ships are drawn with '#' and their neighbours stay as water")
		void floatingShip() {
			game.getMyFleet().addShip(new Barge(Compass.NORTH, new Position(4, 4)));

			game.printMyBoard(false, false);

			assertEquals('#', cell(4, 4));
			assertEquals('.', cell(4, 5));
			assertEquals('.', cell(3, 3));
		}

		@Test
		@DisplayName("sunk ships have their neighbours drawn with '-'")
		void sunkShipNeighbours() {
			Barge barge = new Barge(Compass.NORTH, new Position(4, 4));
			game.getMyFleet().addShip(barge);
			barge.sink();

			game.printMyBoard(false, false);

			assertEquals('#', cell(4, 4));
			for (IPosition adj : barge.getAdjacentPositions())
				assertEquals('-', cell(adj.getRow(), adj.getColumn()), "neighbour " + adj);
		}

		@Test
		@DisplayName("shots are drawn: '*' on ships, 'o' on water and on neighbours of sunk ships")
		void shotsAreDrawn() {
			Barge sunk = new Barge(Compass.NORTH, new Position(1, 1));
			Barge floating = new Barge(Compass.NORTH, new Position(7, 7));
			game.getMyFleet().addShip(sunk);
			game.getMyFleet().addShip(floating);

			// hit and sink (1,1); shoot a neighbour (0,0); one shot outside the board
			game.fireShots(List.of(new Position(1, 1), new Position(0, 0), new Position(-1, 3)));
			// shoot water far from any ship
			game.fireShots(List.of(new Position(9, 0), new Position(9, 1), new Position(9, 2)));

			game.printMyBoard(true, false);

			assertEquals('*', cell(1, 1));
			assertEquals('o', cell(0, 0));
			assertEquals('-', cell(2, 2));
			assertEquals('o', cell(9, 0));
			assertEquals('#', cell(7, 7));
			assertEquals('.', cell(5, 5));
		}

		@Test
		@DisplayName("shots are not drawn when show_shots is false")
		void shotsHidden() {
			game.getMyFleet().addShip(new Barge(Compass.NORTH, new Position(7, 7)));
			game.fireShots(List.of(new Position(9, 0), new Position(9, 1), new Position(7, 7)));

			game.printMyBoard(false, false);

			assertEquals('.', cell(9, 0));
			assertEquals('#', cell(7, 7));
		}

		@Test
		@DisplayName("printBoard() with a null fleet or null moves fails the assertion")
		void nullArguments() {
			assertThrows(AssertionError.class, () -> Game.printBoard(null, List.of(), false, false));
			assertThrows(AssertionError.class, () -> Game.printBoard(new Fleet(), null, false, false));
		}

		@Test
		@DisplayName("over() prints the total game time and the farewell message")
		void over() {
			game.over();

			assertTrue(output().contains("Tempo total de jogo: "), output());
			assertTrue(output().contains("Java Sparrow"), output());
		}
	}
}
