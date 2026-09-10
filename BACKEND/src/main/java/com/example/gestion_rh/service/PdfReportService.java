package com.example.gestion_rh.service;

import com.example.gestion_rh.exception.BusinessException;
import com.example.gestion_rh.model.Employe;
import com.example.gestion_rh.model.SuiviTemps;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.repository.SuiviTempsRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfReportService {

    private final EmployeRepository employeRepository;
    private final SuiviTempsRepository suiviTempsRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public byte[] genererRapportHebdomadaire(LocalDate debut, LocalDate fin) {
        List<Employe> employes = employeRepository.findByStatutEmploye(Employe.StatutEmploye.Actif);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            document.add(new Paragraph("GRH Pro — Rapport hebdomadaire des pointages", titleFont));
            document.add(new Paragraph(
                    "Période du " + debut.format(DATE_FMT) + " au " + fin.format(DATE_FMT),
                    normalFont));
            document.add(Chunk.NEWLINE);

            if (employes.isEmpty()) {
                document.add(new Paragraph("Aucun employé actif.", normalFont));
            }

            for (Employe employe : employes) {
                List<SuiviTemps> suivis = suiviTempsRepository
                        .findByEmploye_IdEmployeAndDateTravailBetween(
                                employe.getIdEmploye(), debut, fin);

                document.add(new Paragraph(
                        employe.getPrenomEmploye() + " " + employe.getNomEmploye()
                                + " — " + employe.getPoste()
                                + (employe.getDepartement() != null
                                ? " (" + employe.getDepartement().getNomDepartement() + ")"
                                : ""),
                        headerFont));

                PdfPTable table = new PdfPTable(new float[]{2.2f, 1.5f, 1.5f, 1.5f, 1.5f, 2f});
                table.setWidthPercentage(100);
                table.setSpacingBefore(6);
                table.setSpacingAfter(14);

                addHeader(table, "Date", smallFont);
                addHeader(table, "Entrée", smallFont);
                addHeader(table, "Sortie", smallFont);
                addHeader(table, "Heures", smallFont);
                addHeader(table, "HS", smallFont);
                addHeader(table, "Statut", smallFont);

                if (suivis.isEmpty()) {
                    PdfPCell empty = new PdfPCell(new Phrase("Aucun pointage sur la période", smallFont));
                    empty.setColspan(6);
                    empty.setPadding(6);
                    table.addCell(empty);
                } else {
                    for (SuiviTemps s : suivis) {
                        table.addCell(cell(s.getDateTravail().format(DATE_FMT), smallFont));
                        table.addCell(cell(s.getHeuresDebut().format(TIME_FMT), smallFont));
                        table.addCell(cell(s.getHeuresFin().format(TIME_FMT), smallFont));
                        table.addCell(cell(String.valueOf(s.getHeuresTravaillees()), smallFont));
                        table.addCell(cell(String.valueOf(s.getHeuresSupplementaires()), smallFont));
                        table.addCell(cell(s.getStatutSupp().toString(), smallFont));
                    }
                }
                document.add(table);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("Impossible de générer le PDF : " + e.getMessage());
        }
    }

    private void addHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(230, 230, 230));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private PdfPCell cell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setPadding(4);
        return cell;
    }
}
