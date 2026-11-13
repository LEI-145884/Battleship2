package battleship;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.util.List;

public class PdfExporter {

    public static final int INITIAL_Y = 750;
    public static final int LEFT_MARGIN = 50;
    public static final int BOTTOM_MARGIN = 50;
    public static final int TITLE_FONT_SIZE = 18;
    public static final int TITLE_SPACING = 40;
    public static final int BODY_FONT_SIZE = 12;
    public static final int ROW_HEIGHT = 20;

    public static void exportMoves(List<IMove> moves, String fileName) throws IOException {

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            float y = INITIAL_Y;

            content.beginText();
            content.setFont(boldFont, TITLE_FONT_SIZE);
            content.newLineAtOffset(LEFT_MARGIN, y);
            content.showText("Battleship - Game Moves");
            content.endText();

            y -= TITLE_SPACING;

            for (IMove move : moves) {

                StringBuilder line = new StringBuilder();
                line.append("Move ").append(move.getNumber()).append(": ");

                for (IPosition shot : move.getShots()) {
                    line.append(shot.toString()).append(" ");
                }

                content.beginText();
                content.setFont(font, BODY_FONT_SIZE);
                content.newLineAtOffset(LEFT_MARGIN, y);
                content.showText(line.toString());
                content.endText();

                y -= ROW_HEIGHT;

                if (y < BOTTOM_MARGIN) {
                    content.close();

                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);

                    content = new PDPageContentStream(document, page);
                    y = INITIAL_Y;
                }
            }

            content.close();

            document.save(fileName);
        }
    }
}