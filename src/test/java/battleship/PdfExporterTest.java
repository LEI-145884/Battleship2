package battleship;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PdfExporter.
 * Author: 145883 - Kseniya Fedartsova
 * Cyclomatic Complexity for each method:
 * - exportMoves: 4 (loop over moves, loop over shots, new page when the page is full)
 */
class PdfExporterTest {

	/** Moves that fit on the first page: the title is at y=750, the first move at y=710, 20 points per move, new page below 50. */
	private static final int MOVES_ON_FIRST_PAGE = 34;

	@TempDir
	Path tempDir;

	private static IMove move(int number, IPosition... shots) {
		return new Move(number, List.of(shots), List.of());
	}

	private static List<IMove> moves(int count) {
		List<IMove> moves = new ArrayList<>();
		for (int i = 1; i <= count; i++)
			moves.add(move(i, new Position(0, i % Game.BOARD_SIZE), new Position(1, 1), new Position(9, 9)));
		return moves;
	}

	/** Exports the moves and returns the PDF file. */
	private File export(List<IMove> moves) throws IOException {
		File file = tempDir.resolve("moves.pdf").toFile();
		PdfExporter.exportMoves(moves, file.getPath());
		return file;
	}

	private static int pageCount(File pdf) throws IOException {
		try (PDDocument doc = Loader.loadPDF(pdf)) {
			return doc.getNumberOfPages();
		}
	}

	private static String text(File pdf) throws IOException {
		try (PDDocument doc = Loader.loadPDF(pdf)) {
			return new PDFTextStripper().getText(doc);
		}
	}

	private static String text(File pdf, int page) throws IOException {
		try (PDDocument doc = Loader.loadPDF(pdf)) {
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setStartPage(page);
			stripper.setEndPage(page);
			return stripper.getText(doc);
		}
	}

	@Test
	@DisplayName("exportMoves() creates a valid PDF file with the title")
	void createsPdfWithTitle() throws IOException {
		File pdf = export(moves(1));

		assertTrue(pdf.exists());
		assertTrue(pdf.length() > 0);
		assertTrue(text(pdf).contains("Battleship - Game Moves"));
	}

	@Test
	@DisplayName("exportMoves() with no moves creates one page with only the title")
	void noMoves() throws IOException {
		File pdf = export(List.of());

		assertEquals(1, pageCount(pdf));
		String text = text(pdf);
		assertTrue(text.contains("Battleship - Game Moves"));
		assertFalse(text.contains("Move "));
	}

	@Test
	@DisplayName("exportMoves() writes one line per move with its number and shots")
	void oneLinePerMove() throws IOException {
		List<IMove> moves = List.of(
				move(1, new Position('A', 1), new Position('B', 2), new Position('C', 3)),
				move(2, new Position('J', 10), new Position('E', 5), new Position('A', 1)));

		String text = text(export(moves));

		assertTrue(text.contains("Move 1: A1 B2 C3"), text);
		assertTrue(text.contains("Move 2: J10 E5 A1"), text);
		assertTrue(text.indexOf("Move 1:") < text.indexOf("Move 2:"), "moves should be in order");
	}

	@Test
	@DisplayName("exportMoves() writes a move without shots as just its number")
	void moveWithoutShots() throws IOException {
		String text = text(export(List.of(move(7))));

		assertTrue(text.contains("Move 7:"), text);
	}

	@Test
	@DisplayName("exportMoves() keeps a short game (33 moves) on a single page")
	void shortGameSinglePage() throws IOException {
		File pdf = export(moves(MOVES_ON_FIRST_PAGE - 1));

		assertEquals(1, pageCount(pdf));
	}

	@Test
	@DisplayName("exportMoves() puts " + MOVES_ON_FIRST_PAGE + " moves on the first page and the next ones on a new page")
	void newPage() throws IOException {
		File pdf = export(moves(MOVES_ON_FIRST_PAGE + 1));

		assertEquals(2, pageCount(pdf));
		String first = text(pdf, 1);
		String second = text(pdf, 2);
		assertTrue(first.contains("Move 1:"));
		assertTrue(first.contains("Move " + MOVES_ON_FIRST_PAGE + ":"));
		assertTrue(second.contains("Move " + (MOVES_ON_FIRST_PAGE + 1) + ":"));
		assertFalse(second.contains("Move " + MOVES_ON_FIRST_PAGE + ":"));
		assertFalse(second.contains("Battleship - Game Moves"), "the title is only on the first page");
	}

	@Test
	@DisplayName("PdfExporter can be instantiated (it only has static methods)")
	void constructor() {
		assertNotNull(new PdfExporter());
	}

	@Test
	@DisplayName("exportMoves() uses as many pages as needed for a long game")
	void manyPages() throws IOException {
		File pdf = export(moves(100));

		assertTrue(pageCount(pdf) >= 3, "100 moves need at least 3 pages");
		String text = text(pdf);
		for (int i = 1; i <= 100; i++)
			assertTrue(text.contains("Move " + i + ":"), "missing move " + i);
	}

	@Test
	@DisplayName("exportMoves() overwrites an existing file")
	void overwritesFile() throws IOException {
		File file = tempDir.resolve("moves.pdf").toFile();
		Files.writeString(file.toPath(), "not a pdf");

		PdfExporter.exportMoves(moves(2), file.getPath());

		assertTrue(text(file).contains("Move 2:"));
	}

	@Test
	@DisplayName("exportMoves() throws IOException when the file cannot be written")
	void invalidPath() {
		String path = tempDir.resolve("missing-folder").resolve("moves.pdf").toString();

		assertThrows(IOException.class, () -> PdfExporter.exportMoves(moves(1), path));
	}

	@Test
	@DisplayName("exportMoves() with a null list of moves throws NullPointerException")
	void nullMoves() {
		String path = tempDir.resolve("moves.pdf").toString();

		assertThrows(NullPointerException.class, () -> PdfExporter.exportMoves(null, path));
	}
}
