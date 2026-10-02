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

    public static void exportMoves(List<IMove> moves, String fileName) throws IOException {

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            float y = 750;

            content.beginText();
            content.setFont(boldFont, 18);
            content.newLineAtOffset(50, y);
            content.showText("Battleship - Game Moves");
            content.endText();

            y -= 40;

            for (IMove move : moves) {

                StringBuilder line = new StringBuilder();
                line.append("Move ").append(move.getNumber()).append(": ");

                for (IPosition shot : move.getShots()) {
                    line.append(shot.toString()).append(" ");
                }

                content.beginText();
                content.setFont(font, 12);
                content.newLineAtOffset(50, y);
                content.showText(line.toString());
                content.endText();

                y -= 20;

                if (y < 50) {
                    content.close();

                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);

                    content = new PDPageContentStream(document, page);
                    y = 750;
                }
            }

            content.close();

            document.save(fileName);
        }
    }
}