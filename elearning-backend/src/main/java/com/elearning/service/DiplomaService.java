package com.elearning.service;

import com.elearning.exception.BadRequestException;
import com.elearning.model.StudyLevel;
import com.elearning.model.User;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class DiplomaService {

    private final UserService userService;

    public byte[] generateDiploma() {
        User student = userService.getCurrentUser();

        if (student.getStudyLevel() != StudyLevel.GRADUATED) {
            throw new BadRequestException("Vous devez avoir passé les 3 niveaux pour obtenir un diplôme.");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("ATTESTATION DE RÉUSSITE")
                    .setFontSize(24)
                    .setBold()
                    .setFontColor(ColorConstants.BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(40));

            document.add(new Paragraph("Ceci certifie que")
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(student.getPrenom() + " " + student.getNom())
                    .setFontSize(22)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            document.add(new Paragraph("a suivi et validé avec succès l'ensemble des trois niveaux de formation sur notre plateforme E-Learning. Félicitations pour cette belle réussite académique et bonne continuation !")
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setMarginBottom(40));

            document.add(new Paragraph("Fait le " + java.time.LocalDate.now().toString())
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.RIGHT));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du diplôme", e);
        }
    }
}
