package battleship;

import javafx.application.Application;

public class Main {
    /**
     * Main.
     *
     * @param args the args
     */
    public static void main(String[] args) {
        System.out.println("***  Battleship  ***");

        System.out.println("*** Arrancar Battleship Gráfico ***");

        // 1. Criar uma frota aleatória e um jogo usando as tuas lógicas
        IFleet myFleet = Fleet.createRandom();
        Game game = new Game(myFleet);

        // 2. Simular uma rajada inimiga para vermos tiros vermelhos/brancos no ecrã
        game.randomEnemyFire();

        // 3. Injetar o jogo na interface gráfica
        GraphicBoard.currentGame = game;

        // 4. Iniciar o JavaFX de forma segura (sem dar o erro de módulos)
        Application.launch(GraphicBoard.class, args);
    }
}