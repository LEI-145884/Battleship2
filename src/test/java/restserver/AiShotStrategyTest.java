package restserver;

import battleship.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class AiShotStrategy.
 * Author: ${user.name}
 * Date: 2026-11-06 15:25
 * Cyclomatic Complexity:
 * - computeShots(): 15 (approx.; paths below cover every branch)
 * - getHitButNotSunkPositions(): 8 (private, covered through computeShots)
 * - buildHuntPool(): 5 (private, covered through computeShots)
 */
class AiShotStrategyTest {

    private AiShotStrategy strategy;
    private IGame game;

    @BeforeEach
    void setUp() {
        strategy = new AiShotStrategy();
        game = new Game(Fleet.createRandom());
    }

    @AfterEach
    void tearDown() {
        strategy = null;
        game = null;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /** A non-null IShip stand-in: the strategy only checks ship() != null. */
    private static IShip fakeShip() {
        return (IShip) Proxy.newProxyInstance(IShip.class.getClassLoader(), new Class<?>[]{IShip.class},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        switch (method.getName()) {
                            case "hashCode": return System.identityHashCode(proxy);
                            case "equals": return proxy == args[0];
                            default: return "fakeShip";
                        }
                    }
                    return null;
                });
    }

    private static IGame.ShotResult res(boolean valid, boolean repeated, IShip ship, boolean sunk) {
        return new IGame.ShotResult(valid, repeated, ship, sunk);
    }

    private void addMove(List<IPosition> shots, List<IGame.ShotResult> results) {
        game.getAlienMoves().add(new Move(game.getAlienMoves().size() + 1,
                new ArrayList<>(shots), new ArrayList<>(results)));
    }

    /** Registers the positions as already shot (MISS), NUMBER_SHOTS per move. */
    private void addBulkMisses(List<IPosition> positions) {
        for (int i = 0; i < positions.size(); i += Game.NUMBER_SHOTS) {
            List<IPosition> chunk = positions.subList(i, Math.min(i + Game.NUMBER_SHOTS, positions.size()));
            List<IGame.ShotResult> results = new ArrayList<>();
            for (int k = 0; k < chunk.size(); k++) {
                results.add(res(true, false, null, false));
            }
            addMove(chunk, results);
        }
    }

    /** Parity used by the hunt phase: (rowIndex + colIndex) % 2 == 0. */
    private static boolean isCheckerboard(IPosition p) {
        return ((p.getClassicRow() - 'A') + (p.getClassicColumn() - 1)) % 2 == 0;
    }

    private void assertHuntOnly(List<IGame.ShotResult> results) {
        IPosition hit = new Position(4, 4);
        addMove(List.of(hit), results);
        List<IPosition> shots = strategy.computeShots(game);
        assertAll(
                () -> assertEquals(Game.NUMBER_SHOTS, shots.size(), "Error: wrong number of shots"),
                () -> assertTrue(shots.stream().allMatch(AiShotStrategyTest::isCheckerboard),
                        "Error: expected hunt (checkerboard) shots only, but a neighbour was targeted: " + shots)
        );
    }

    // ── computeShots: HUNT phase ─────────────────────────────────────────────

    @Test
    @DisplayName("computeShots1: no previous moves -> NUMBER_SHOTS distinct checkerboard shots")
    void computeShots1() {
        List<IPosition> shots = strategy.computeShots(game);
        assertAll(
                () -> assertEquals(Game.NUMBER_SHOTS, shots.size(), "Error: wrong number of shots"),
                () -> assertEquals(Game.NUMBER_SHOTS, new HashSet<>(shots).size(), "Error: shots must be distinct"),
                () -> assertTrue(shots.stream().allMatch(AiShotStrategyTest::isCheckerboard),
                        "Error: hunt shots must be on the checkerboard but got " + shots)
        );
    }

    // ── computeShots: TARGET phase ───────────────────────────────────────────

    @Test
    @DisplayName("computeShots2: a live hit makes the AI shoot at its neighbours")
    void computeShots2() {
        IPosition hit = new Position(4, 4);
        addMove(List.of(hit), List.of(res(true, false, fakeShip(), false)));
        Set<IPosition> neighbours = new HashSet<>(hit.adjacentPositions());
        List<IPosition> shots = strategy.computeShots(game);
        assertAll(
                () -> assertEquals(Game.NUMBER_SHOTS, shots.size(), "Error: wrong number of shots"),
                () -> assertTrue(neighbours.containsAll(shots),
                        "Error: expected only neighbours of the hit " + neighbours + " but got " + shots)
        );
    }

    @Test
    @DisplayName("computeShots3: hit in a corner has fewer neighbours, rest is filled by hunt")
    void computeShots3() {
        IPosition hit = new Position(0, 0);
        addMove(List.of(hit), List.of(res(true, false, fakeShip(), false)));
        List<IPosition> shots = strategy.computeShots(game);
        assertAll(
                () -> assertEquals(Game.NUMBER_SHOTS, shots.size(), "Error: wrong number of shots"),
                () -> assertEquals(Game.NUMBER_SHOTS, new HashSet<>(shots).size(), "Error: shots must be distinct"),
                () -> assertFalse(shots.contains(hit), "Error: must not repeat the hit cell")
        );
    }

    @Test
    @DisplayName("computeShots4: neighbours already shot are skipped (falls back to hunt)")
    void computeShots4() {
        IPosition hit = new Position(4, 4);
        List<IPosition> neighbours = new ArrayList<>(hit.adjacentPositions());
        List<IPosition> shots1 = new ArrayList<>();
        shots1.add(hit);
        shots1.addAll(neighbours);
        List<IGame.ShotResult> results = new ArrayList<>();
        results.add(res(true, false, fakeShip(), false));
        for (int i = 0; i < neighbours.size(); i++) {
            results.add(res(true, false, null, false));
        }
        addMove(shots1, results);

        List<IPosition> shots = strategy.computeShots(game);
        assertAll(
                () -> assertEquals(Game.NUMBER_SHOTS, shots.size(), "Error: wrong number of shots"),
                () -> assertTrue(shots.stream().noneMatch(shots1::contains),
                        "Error: AI repeated an already shot cell: " + shots),
                () -> assertTrue(shots.stream().allMatch(AiShotStrategyTest::isCheckerboard),
                        "Error: expected hunt shots only but got " + shots)
        );
    }

    @Test
    @DisplayName("computeShots5: several hits with shared neighbour -> distinct shots, limited to NUMBER_SHOTS")
    void computeShots5() {
        IPosition h1 = new Position(4, 4);
        IPosition h2 = new Position(4, 6); // shares neighbour (4,5) with h1
        addMove(List.of(h1), List.of(res(true, false, fakeShip(), false)));
        addMove(List.of(h2), List.of(res(true, false, fakeShip(), false)));
        Set<IPosition> allowed = new HashSet<>(h1.adjacentPositions());
        allowed.addAll(h2.adjacentPositions());
        List<IPosition> shots = strategy.computeShots(game);
        assertAll(
                () -> assertEquals(Game.NUMBER_SHOTS, shots.size(), "Error: wrong number of shots"),
                () -> assertEquals(Game.NUMBER_SHOTS, new HashSet<>(shots).size(), "Error: shots must be distinct"),
                () -> assertTrue(allowed.containsAll(shots), "Error: expected only neighbours of hits but got " + shots)
        );
    }

    // ── getHitButNotSunkPositions: each condition that rejects a hit ────────

    @Test
    @DisplayName("computeShots6: move without results is not a hit")
    void computeShots6() {
        assertHuntOnly(Collections.emptyList());
    }

    @Test
    @DisplayName("computeShots7: results list shorter than shots list is ignored")
    void computeShots7() {
        IPosition a = new Position(4, 4);
        IPosition b = new Position(6, 6);
        addMove(List.of(a, b), List.of(res(true, false, null, false)));
        List<IPosition> shots = strategy.computeShots(game);
        assertAll(
                () -> assertEquals(Game.NUMBER_SHOTS, shots.size(), "Error: wrong number of shots"),
                () -> assertTrue(shots.stream().allMatch(AiShotStrategyTest::isCheckerboard),
                        "Error: expected hunt shots only but got " + shots)
        );
    }

    @Test
    @DisplayName("computeShots8: invalid result is not a live hit")
    void computeShots8() {
        assertHuntOnly(List.of(res(false, false, fakeShip(), false)));
    }

    @Test
    @DisplayName("computeShots9: repeated result is not a live hit")
    void computeShots9() {
        assertHuntOnly(List.of(res(true, true, fakeShip(), false)));
    }

    @Test
    @DisplayName("computeShots10: miss (no ship) is not a live hit")
    void computeShots10() {
        assertHuntOnly(List.of(res(true, false, null, false)));
    }

    @Test
    @DisplayName("computeShots11: sunk ship is not a live hit")
    void computeShots11() {
        assertHuntOnly(List.of(res(true, false, fakeShip(), true)));
    }

    // ── fallback branch ──────────────────────────────────────────────────────

    @Test
    @DisplayName("computeShots12: hunt pool exhausted -> fallback fills remaining shots with any unseen cell")
    void computeShots12() {
        List<IPosition> checkerboard = new ArrayList<>();
        for (int r = 0; r < Game.BOARD_SIZE; r++) {
            for (int c = 0; c < Game.BOARD_SIZE; c++) {
                if ((r + c) % 2 == 0) {
                    checkerboard.add(new Position(r, c));
                }
            }
        }
        checkerboard.remove(checkerboard.size() - 1); // leave exactly one checkerboard cell free
        addBulkMisses(checkerboard);
        Set<IPosition> shotBefore = new HashSet<>(checkerboard);

        List<IPosition> shots = strategy.computeShots(game);
        assertAll(
                () -> assertEquals(Game.NUMBER_SHOTS, shots.size(), "Error: wrong number of shots"),
                () -> assertEquals(Game.NUMBER_SHOTS, new HashSet<>(shots).size(), "Error: shots must be distinct"),
                () -> assertTrue(shots.stream().noneMatch(shotBefore::contains),
                        "Error: AI repeated an already shot cell: " + shots)
        );
    }

    @Test
    @DisplayName("computeShots13: whole board already shot -> no shots available")
    void computeShots13() {
        List<IPosition> all = new ArrayList<>();
        for (int r = 0; r < Game.BOARD_SIZE; r++) {
            for (int c = 0; c < Game.BOARD_SIZE; c++) {
                all.add(new Position(r, c));
            }
        }
        addBulkMisses(all);
        List<IPosition> shots = strategy.computeShots(game);
        assertTrue(shots.isEmpty(), "Error: expected no shots on a fully shot board but got " + shots);
    }
}