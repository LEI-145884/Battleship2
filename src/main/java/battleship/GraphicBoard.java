package battleship;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class GraphicBoard extends Application {

    public static IGame currentGame;
    // Tornar a grelha estática para podermos atualizá-la de fora
    private static GridPane grid = new GridPane();

    @Override
    public void start(Stage primaryStage) {
        drawBoard(); // Desenhar o estado inicial

        Scene scene = new Scene(grid);
        primaryStage.setTitle("Battleship - Tabuleiro Gráfico");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Se o utilizador fechar a janela no 'X', o terminal também encerra
        primaryStage.setOnCloseRequest(e -> System.exit(0));
    }

    // Novo método para redesenhar o tabuleiro inteiro!
    public static void drawBoard() {
        // Platform.runLater garante que as alterações visuais ocorrem na thread do JavaFX
        Platform.runLater(() -> {
            grid.getChildren().clear(); // Limpar a grelha anterior

            // 1. Criar a grelha (tudo água)
            Rectangle[][] cells = new Rectangle[Game.BOARD_SIZE][Game.BOARD_SIZE];
            for (int r = 0; r < Game.BOARD_SIZE; r++) {
                for (int c = 0; c < Game.BOARD_SIZE; c++) {
                    Rectangle rect = new Rectangle(40, 40);
                    rect.setFill(Color.DEEPSKYBLUE);
                    rect.setStroke(Color.BLACK);
                    cells[r][c] = rect;
                    grid.add(rect, c, r);
                }
            }

            if (currentGame != null) {
                IFleet fleet = currentGame.getMyFleet();

                // 2. Pintar os Navios e áreas afundadas
                for (IShip ship : fleet.getShips()) {
                    for (IPosition pos : ship.getPositions()) {
                        if (pos.isInside()) {
                            cells[pos.getRow()][pos.getColumn()].setFill(Color.DARKGRAY);
                        }
                    }
                    if (!ship.stillFloating()) {
                        for (IPosition adj : ship.getAdjacentPositions()) {
                            if (adj.isInside() && cells[adj.getRow()][adj.getColumn()].getFill() == Color.DEEPSKYBLUE) {
                                cells[adj.getRow()][adj.getColumn()].setFill(Color.LIGHTCYAN);
                            }
                        }
                    }
                }

                // 3. Pintar os Tiros (Marcas de impacto)
                for (IMove move : currentGame.getAlienMoves()) {
                    for (IPosition shot : move.getShots()) {
                        if (shot.isInside()) {
                            int r = shot.getRow();
                            int c = shot.getColumn();
                            IShip hitShip = fleet.shipAt(shot);
                            if (hitShip != null) {
                                cells[r][c].setFill(Color.RED); // Tiro certeiro
                            } else {
                                cells[r][c].setFill(Color.WHITE); // Tiro na água
                            }
                        }
                    }
                }
            }
        });
    }
}