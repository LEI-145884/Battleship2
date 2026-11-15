# Refactoring Opportunities

| Local | Cheiro no código | Refabricação | Nº alun@ |
|-------|------------------|--------------|----------|
| `Game` (`printBoard`, `printShips`, `printBoardShots`, `printBoardFrame` and the board markers) | Large Class (Qodana: *Overly complex class* `Game`, cyclomatic complexity = 67; *too many methods*, method count = 22) | Move Members (to a new class `BoardPrinter`) | 145884 |
| `Game::randomEnemyFire` | Duplicated Code (two identical loops that pick random distinct shots) | Extract Method | 145884 |
| `Game::printBoard`, `IGame::printMyBoard`, `IGame::printAlienBoard` | Mysterious Name (snake_case parameters `show_shots`, `show_legend`) | Rename | 145884 |
| `Game` (fields `countInvalidShots`, `countRepeatedShots`, `countHits`, `countSinks`) | Unnecessary boxing (`Integer` counters returned as `int`) | Type Migration | 145884 |
| `Game::getRemainingShips` | Redundant temporary variable (`floatingShips` used only once) | Inline Variable | 145884 |
| `Fleet::addShip` | Complex conditional (three conditions in one `if`) | Introduce Variable | 145884 |
| Move::processEnemyFire (building of the verbose summary message) | Long Method (Qodana: Overly complex method, cyclomatic complexity = 37; too long, 77 statements) | Extract Method | 145883 |
| Move::processEnemyFire (loop over sunkBoatsCount) | Redundant temporary variable (boatName used only once) | Inline Variable | 145883 |
| PdfExporter::exportMoves | Magic numbers (750, 50, 40, 20, 18, 12 for page layout and font sizes); Qodana: method too long, 32 statements | Introduce Constant | 145883 |
| Position::isAdjacentTo | Complex expression (two Math.abs(...) <= 1 comparisons in one return) | Introduce Variable | 145883 |
| Compass (field c and constructor parameter c) | Mysterious Name | Rename | 145883 |
| `CallbackClient::sendShots` | Mysterious Name (variable `m2b` is a protocol code, not a meaningful name) | Rename | 122708 |
| `GameController::receiveShots` | Magic number (`10` as the maximum column in the position validation) | Introduce Constant | 122708 |
| `GameController::evaluateShots` | Long Method (conversion of each shot result to a DTO is mixed into the loop) | Extract Method | 122708 |
| `AiShotStrategy::getHitButNotSunkPositions` | Duplicated expression (`move.getShotResults()` evaluated three times) | Introduce Variable | 122708 |
| `GameController::receiveShots` | Redundant temporary variable (`callbackOk` used only once) | Inline Variable | 122708 |
| `AiShotStrategy::getHitButNotSunkPositions` | Unused parameter (`alreadyShot` is never read) | Change Signature | 122708 |
| `Dtos.ErrorResponse` | Data Class / Public field (`error` is public) | Encapsulate Fields | 122708 |