package com.example.gestion_rh.service;

import com.example.gestion_rh.exception.BusinessException;
import com.example.gestion_rh.model.DemandeConge;
import com.example.gestion_rh.model.Employe;
import com.example.gestion_rh.model.Entreprise;
import com.example.gestion_rh.model.HistoriqueAction;
import com.example.gestion_rh.model.Paiement;
import com.example.gestion_rh.model.SuiviTemps;
import com.example.gestion_rh.repository.DemandeCongeRepository;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.repository.HistoriqueActionRepository;
import com.example.gestion_rh.repository.PaiementRepository;
import com.example.gestion_rh.repository.SuiviTempsRepository;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfReportService {

    private final EmployeRepository employeRepository;
    private final SuiviTempsRepository suiviTempsRepository;
    private final HistoriqueActionRepository historiqueActionRepository;
    private final DemandeCongeRepository demandeCongeRepository;
    private final PaiementRepository paiementRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * PDF strictement limité à l'entreprise fournie (aucune donnée cross-tenant).
     */
    public byte[] genererRapportHebdomadaire(Entreprise entreprise, LocalDate debut, LocalDate fin) {
        if (entreprise == null || entreprise.getIdEntreprise() == null) {
            throw new BusinessException("Entreprise obligatoire pour générer un rapport");
        }
        Integer idEntreprise = entreprise.getIdEntreprise();
        List<Employe> employes = employeRepository
                .findByEntreprise_IdEntrepriseAndStatutEmploye(idEntreprise, Employe.StatutEmploye.Actif);

        LocalDateTime debutDt = debut.atStartOfDay();
        LocalDateTime finExclu = fin.plusDays(1).atStartOfDay();

        List<HistoriqueAction> actions = historiqueActionRepository
                .findByEntreprise_IdEntrepriseAndDateActionBetweenOrderByDateActionAsc(
                        idEntreprise, debutDt, finExclu);
        List<DemandeConge> conges = demandeCongeRepository
                .findByEntrepriseAndDateDemandeBetween(idEntreprise, debutDt, finExclu);
        List<Paiement> paiements = paiementRepository
                .findByEntrepriseAndDatePaiementBetween(idEntreprise, debutDt, finExclu);

        int totalPointages = 0;
        java.math.BigDecimal totalHeures = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalHs = java.math.BigDecimal.ZERO;
        for (Employe emp : employes) {
            List<SuiviTemps> suivis = suiviTempsRepository
                    .findByEmploye_IdEmployeAndDateTravailBetween(emp.getIdEmploye(), debut, fin);
            totalPointages += suivis.size();
            for (SuiviTemps s : suivis) {
                if (s.getHeuresTravaillees() != null) {
                    totalHeures = totalHeures.add(s.getHeuresTravaillees());
                }
                if (s.getHeuresSupplementaires() != null) {
                    totalHs = totalHs.add(s.getHeuresSupplementaires());
                }
            }
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            document.add(new Paragraph("GRH Pro — Rapport hebdomadaire d'activité", titleFont));
            document.add(new Paragraph("Entreprise : " + entreprise.getNomEntreprise(), headerFont));
            document.add(new Paragraph(
                    "Période du " + debut.format(DATE_FMT) + " au " + fin.format(DATE_FMT),
                    normalFont));
            document.add(new Paragraph(
                    "Généré le " + LocalDateTime.now().format(DT_FMT) + " — données exclusives à cette organisation",
                    smallFont));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("1. Synthèse de la semaine", sectionFont));
            document.add(Chunk.NEWLINE);
            PdfPTable synth = new PdfPTable(new float[]{3f, 1.5f});
            synth.setWidthPercentage(50);
            addRow(synth, "Employés actifs", String.valueOf(employes.size()), smallFont);
            addRow(synth, "Pointages", String.valueOf(totalPointages), smallFont);
            addRow(synth, "Heures travaillées", totalHeures.toPlainString(), smallFont);
            addRow(synth, "Heures supplémentaires", totalHs.toPlainString(), smallFont);
            addRow(synth, "Demandes de congé", String.valueOf(conges.size()), smallFont);
            addRow(synth, "Paiements enregistrés", String.valueOf(paiements.size()), smallFont);
            addRow(synth, "Actions tracées", String.valueOf(actions.size()), smallFont);
            document.add(synth);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("2. Activités réalisées (historique)", sectionFont));
            document.add(Chunk.NEWLINE);
            PdfPTable actTable = new PdfPTable(new float[]{2.2f, 2.5f, 2f, 5f});
            actTable.setWidthPercentage(100);
            actTable.setSpacingAfter(12);
            addHeader(actTable, "Date", smallFont);
            addHeader(actTable, "Action", smallFont);
            addHeader(actTable, "Acteur", smallFont);
            addHeader(actTable, "Détail", smallFont);
            if (actions.isEmpty()) {
                PdfPCell empty = new PdfPCell(new Phrase("Aucune activité enregistrée sur la période", smallFont));
                empty.setColspan(4);
                empty.setPadding(6);
                actTable.addCell(empty);
            } else {
                for (HistoriqueAction h : actions) {
                    actTable.addCell(cell(h.getDateAction() != null ? h.getDateAction().format(DT_FMT) : "", smallFont));
                    actTable.addCell(cell(h.getAction(), smallFont));
                    actTable.addCell(cell(h.getActeurLogin(), smallFont));
                    actTable.addCell(cell(h.getDetail(), smallFont));
                }
            }
            document.add(actTable);

            document.add(new Paragraph("3. Congés demandés", sectionFont));
            document.add(Chunk.NEWLINE);
            PdfPTable congTable = new PdfPTable(new float[]{3f, 2f, 2f, 1.2f, 2f});
            congTable.setWidthPercentage(100);
            congTable.setSpacingAfter(12);
            addHeader(congTable, "Employé", smallFont);
            addHeader(congTable, "Début", smallFont);
            addHeader(congTable, "Fin", smallFont);
            addHeader(congTable, "Jours", smallFont);
            addHeader(congTable, "Statut", smallFont);
            if (conges.isEmpty()) {
                PdfPCell empty = new PdfPCell(new Phrase("Aucune demande de congé sur la période", smallFont));
                empty.setColspan(5);
                empty.setPadding(6);
                congTable.addCell(empty);
            } else {
                for (DemandeConge d : conges) {
                    Employe emp = d.getEmploye();
                    String nom = emp != null
                            ? emp.getPrenomEmploye() + " " + emp.getNomEmploye() : "—";
                    congTable.addCell(cell(nom, smallFont));
                    congTable.addCell(cell(d.getDateDebut() != null ? d.getDateDebut().format(DATE_FMT) : "", smallFont));
                    congTable.addCell(cell(d.getDateFin() != null ? d.getDateFin().format(DATE_FMT) : "", smallFont));
                    congTable.addCell(cell(String.valueOf(d.getNbJours()), smallFont));
                    congTable.addCell(cell(d.getStatutConge() != null ? d.getStatutConge().toString() : "", smallFont));
                }
            }
            document.add(congTable);

            document.add(new Paragraph("4. Paiements de la période", sectionFont));
            document.add(Chunk.NEWLINE);
            PdfPTable payTable = new PdfPTable(new float[]{3f, 1.5f, 1.5f, 2f, 2f});
            payTable.setWidthPercentage(100);
            payTable.setSpacingAfter(12);
            addHeader(payTable, "Employé", smallFont);
            addHeader(payTable, "Mois", smallFont);
            addHeader(payTable, "Année", smallFont);
            addHeader(payTable, "Net", smallFont);
            addHeader(payTable, "Statut", smallFont);
            if (paiements.isEmpty()) {
                PdfPCell empty = new PdfPCell(new Phrase("Aucun paiement sur la période", smallFont));
                empty.setColspan(5);
                empty.setPadding(6);
                payTable.addCell(empty);
            } else {
                for (Paiement p : paiements) {
                    Employe emp = p.getEmploye();
                    String nom = emp != null
                            ? emp.getPrenomEmploye() + " " + emp.getNomEmploye() : "—";
                    payTable.addCell(cell(nom, smallFont));
                    payTable.addCell(cell(String.valueOf(p.getMois()), smallFont));
                    payTable.addCell(cell(String.valueOf(p.getAnnee()), smallFont));
                    payTable.addCell(cell(p.getTotalNet() != null ? p.getTotalNet().toPlainString() : "", smallFont));
                    payTable.addCell(cell(p.getStatut() != null ? p.getStatut().toString() : "", smallFont));
                }
            }
            document.add(payTable);

            document.add(new Paragraph("5. Pointages par employé", sectionFont));
            document.add(Chunk.NEWLINE);

            if (employes.isEmpty()) {
                document.add(new Paragraph("Aucun employé actif dans cette entreprise.", normalFont));
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
                addHeader(table, "Statut HS", smallFont);

                if (suivis.isEmpty()) {
                    PdfPCell empty = new PdfPCell(new Phrase("Aucun pointage sur la période", smallFont));
                    empty.setColspan(6);
                    empty.setPadding(6);
                    table.addCell(empty);
                } else {
                    for (SuiviTemps s : suivis) {
                        table.addCell(cell(s.getDateTravail().format(DATE_FMT), smallFont));
                        table.addCell(cell(s.getHeuresDebut() != null ? s.getHeuresDebut().format(TIME_FMT) : "", smallFont));
                        table.addCell(cell(s.getHeuresFin() != null ? s.getHeuresFin().format(TIME_FMT) : "", smallFont));
                        table.addCell(cell(String.valueOf(s.getHeuresTravaillees()), smallFont));
                        table.addCell(cell(String.valueOf(s.getHeuresSupplementaires()), smallFont));
                        table.addCell(cell(s.getStatutSupp() != null ? s.getStatutSupp().toString() : "", smallFont));
                    }
                }
                document.add(table);
            }

            document.close();
            return baos.toByteArray();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Impossible de générer le PDF : " + e.getMessage());
        }
    }

    private void addRow(PdfPTable table, String label, String value, Font font) {
        table.addCell(cell(label, font));
        table.addCell(cell(value, font));
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
