package battleship;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class GraphicBoard extends Application {

    // Variável que vai receber o jogo atual antes de abrir a janela
    public static IGame currentGame;

    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();

        // 1. Criar a grelha inicial (tudo água)
        Rectangle[][] cells = new Rectangle[Game.BOARD_SIZE][Game.BOARD_SIZE];
        for (int r = 0; r < Game.BOARD_SIZE; r++) {
            for (int c = 0; c < Game.BOARD_SIZE; c++) {
                Rectangle rect = new Rectangle(40, 40);
                rect.setFill(Color.DEEPSKYBLUE); // Cor da água
                rect.setStroke(Color.BLACK);
                cells[r][c] = rect;
                grid.add(rect, c, r); // Coluna, Linha
            }
        }

        if (currentGame != null) {
            IFleet fleet = currentGame.getMyFleet();

            // 2. Pintar os Navios
            for (IShip ship : fleet.getShips()) {
                for (IPosition pos : ship.getPositions()) {
                    if (pos.isInside()) {
                        cells[pos.getRow()][pos.getColumn()].setFill(Color.DARKGRAY);
                    }
                }
                // Se o navio afundou, pintar a área adjacente
                if (!ship.stillFloating()) {
                    for (IPosition adj : ship.getAdjacentPositions()) {
                        if (adj.isInside() && cells[adj.getRow()][adj.getColumn()].getFill() == Color.DEEPSKYBLUE) {
                            cells[adj.getRow()][adj.getColumn()].setFill(Color.LIGHTCYAN);
                        }
                    }
                }
            }

            // 3. Pintar os Tiros Inimigos (Marcas de impacto)
            for (IMove move : currentGame.getAlienMoves()) {
                for (IPosition shot : move.getShots()) {
                    if (shot.isInside()) {
                        int r = shot.getRow();
                        int c = shot.getColumn();

                        IShip hitShip = fleet.shipAt(shot);
                        if (hitShip != null) {
                            cells[r][c].setFill(Color.RED); // Tiro certeiro num navio
                        } else {
                            cells[r][c].setFill(Color.WHITE); // Tiro falhado (na água)
                        }
                    }
                }
            }
        }

        Scene scene = new Scene(grid);
        primaryStage.setTitle("Battleship - Tabuleiro Gráfico");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}