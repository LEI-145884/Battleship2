package battleship;

import javafx.application.Application;

public class Main {
	public static void main(String[] args) {
		System.out.println("*** Arrancar Battleship Gráfico ***");

		// 1. Executar o terminal do jogo numa thread separada!
		new Thread(() -> {
			try {
				// Dar um pequeno delay (1,5 seg) para a janela gráfica abrir primeiro
				Thread.sleep(1500);
				Tasks.menu();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();

		// 2. Iniciar o JavaFX na thread principal (esta chamada bloqueia a thread)
		Application.launch(GraphicBoard.class, args);
	}
}