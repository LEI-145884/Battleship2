# Refactoring Opportunities

Code smells found in BattleShip2 with Qodana for JVM (the quality-gate workflows in
`.github/workflows/qodana-smell-*.yaml`), the IntelliJ inspections and direct code review,
and the refactoring (as named in the IntelliJ *Refactor* menu) chosen to remove each one.
Each group member applies their refactorings in the branch `Refactoring_<number>`,
running the full unit-test suite before and after every refactoring.

| Local | Cheiro no código | Refabricação | Nº alun@ |
|-------|------------------|--------------|----------|
| `Game` (`printBoard`, `printShips`, `printBoardShots`, `printBoardFrame` and the board markers) | Large Class (Qodana: *Overly complex class* `Game`, cyclomatic complexity = 67; *too many methods*, method count = 22) | Move Members (to a new class `BoardPrinter`) | 145884 |
| `Game::randomEnemyFire` | Duplicated Code (two identical loops that pick random distinct shots) | Extract Method | 145884 |
| `Game::printBoard`, `IGame::printMyBoard`, `IGame::printAlienBoard` | Mysterious Name (snake_case parameters `show_shots`, `show_legend`) | Rename | 145884 |
| `Game` (fields `countInvalidShots`, `countRepeatedShots`, `countHits`, `countSinks`) | Unnecessary boxing (`Integer` counters returned as `int`) | Type Migration | 145884 |
| `Game::getRemainingShips` | Redundant temporary variable (`floatingShips` used only once) | Inline Variable | 145884 |
| `Fleet::addShip` | Complex conditional (three conditions in one `if`) | Introduce Variable | 145884 |
| `Fleet::createRandom` | Magic values (literal array of ship types inside the method) | Introduce Constant | 145884 |
