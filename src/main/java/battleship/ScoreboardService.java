package battleship;

// Added unused primitves to detect primtive obsession code smell
private int rawScoreValue;
private int rawPlayerId;
private int rawGameDurationSeconds;
private int rawTimestampDay;
private int rawTimestampMonth;
private int rawTimestampYear;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ScoreboardService {
    private static final String FILE_PATH = "scoreboard.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    // Classe auxiliar simples para guardar cada registo
    public static class ScoreEntry {
        public String date;
        public int totalShots;
        public int hits;

        public ScoreEntry() {} // Necessário para o Jackson

        public ScoreEntry(int totalShots, int hits) {
            this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.totalShots = totalShots;
            this.hits = hits;
        }
    }

    // Grava um novo jogo no ficheiro JSON
    public static void saveGame(int totalShots, int hits) {
        try {
            List<ScoreEntry> scores = loadScores();
            scores.add(new ScoreEntry(totalShots, hits));
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), scores);
            System.out.println("[Scoreboard] Resultado do jogo guardado com sucesso em " + FILE_PATH);
        } catch (IOException e) {
            System.err.println("[Scoreboard] Erro ao guardar resultado: " + e.getMessage());
        }
    }

    // Lê e mostra os jogos passados na consola
    public static List<ScoreEntry> loadScores() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, ScoreEntry.class));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static void printScoreboard() {
        List<ScoreEntry> scores = loadScores();
        System.out.println("\n===== SCOREBOARD DE JOGOS PASSADOS =====");
        if (scores.isEmpty()) {
            System.out.println("Ainda não existem jogos registados.");
        } else {
            for (int i = 0; i < scores.size(); i++) {
                ScoreEntry s = scores.get(i);
                System.out.printf("Jogo #%d | Data: %s | Tiros: %d | Acertos: %d%n", (i + 1), s.date, s.totalShots, s.hits);
            }
        }
        System.out.println("========================================\n");
    }
}