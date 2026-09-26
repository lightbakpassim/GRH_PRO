package com.example.gestion_rh.service;

import com.example.gestion_rh.dto.response.HistoriqueActionResponse;
import com.example.gestion_rh.dto.response.RapportResponse;
import com.example.gestion_rh.exception.BusinessException;
import com.example.gestion_rh.exception.ResourceNotFoundException;
import com.example.gestion_rh.model.Rapport;
import com.example.gestion_rh.model.Utilisateur;
import com.example.gestion_rh.repository.HistoriqueActionRepository;
import com.example.gestion_rh.repository.RapportRepository;
import com.example.gestion_rh.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RapportService {

    private final RapportRepository rapportRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PdfReportService pdfReportService;
    private final HistoriqueActionService historiqueActionService;
    private final HistoriqueActionRepository historiqueActionRepository;

    public Rapport genererEtLivrerRapportHebdomadaire() {
        Utilisateur dg = utilisateurRepository.findAll().stream()
                .filter(u -> u.getRole() == Utilisateur.Role.DG
                        && u.getStatutUtilisateur() == Utilisateur.StatutUtilisateur.Actif)
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "Aucun compte DG actif — impossible de livrer le rapport"));

        LocalDate fin = LocalDate.now();
        LocalDate debut = fin.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if (fin.getDayOfWeek() == DayOfWeek.FRIDAY || fin.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue()) {
            debut = fin.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            // Semaine lun-ven : si on est vendredi, période = lundi→vendredi
            fin = debut.plusDays(4);
            if (LocalDate.now().isBefore(fin)) {
                fin = LocalDate.now();
            }
        } else {
            // Hors vendredi (test manuel) : dernière semaine complète lun-ven
            fin = LocalDate.now().with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
            debut = fin.minusDays(4);
        }

        byte[] pdf = pdfReportService.genererRapportHebdomadaire(debut, fin);
        String nomFichier = "rapport-hebdo-" + debut + "_" + fin + ".pdf";
        String titre = "Rapport hebdomadaire " + debut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + " – " + fin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        Rapport rapport = rapportRepository.save(Rapport.builder()
                .titre(titre)
                .typeRapport(Rapport.TypeRapport.HEBDOMADAIRE)
                .periodeDebut(debut)
                .periodeFin(fin)
                .dateGeneration(LocalDateTime.now())
                .destinataire(dg)
                .fichierPdf(pdf)
                .nomFichier(nomFichier)
                .lu(false)
                .build());

        historiqueActionService.enregistrer(
                "RAPPORT_HEBDO",
                "Rapport " + titre + " livré au DG (" + dg.getLogin() + ")",
                "SYSTEME");

        return rapport;
    }

    @Transactional(readOnly = true)
    public List<RapportResponse> listerPourUtilisateur(Utilisateur utilisateur) {
        List<Rapport> rapports = utilisateur.getRole() == Utilisateur.Role.Admin
                ? rapportRepository.findAllByOrderByDateGenerationDesc()
                : rapportRepository.findByDestinataireOrderByDateGenerationDesc(utilisateur);
        return rapports.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Rapport getPdf(Integer id, Utilisateur utilisateur) {
        Rapport rapport = rapportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rapport introuvable"));
        if (utilisateur.getRole() != Utilisateur.Role.Admin
                && !rapport.getDestinataire().getIdUtilisateur().equals(utilisateur.getIdUtilisateur())) {
            throw new AccessDeniedException("Accès refusé à ce rapport");
        }
        return rapport;
    }

    public void marquerLu(Integer id, Utilisateur utilisateur) {
        Rapport rapport = getPdf(id, utilisateur);
        rapport.setLu(true);
        rapportRepository.save(rapport);
    }

    @Transactional(readOnly = true)
    public List<HistoriqueActionResponse> historique() {
        return historiqueActionRepository.findAllByOrderByDateActionDesc().stream()
                .map(h -> HistoriqueActionResponse.builder()
                        .idHistorique(h.getIdHistorique())
                        .action(h.getAction())
                        .detail(h.getDetail())
                        .acteurLogin(h.getActeurLogin())
                        .dateAction(h.getDateAction())
                        .build())
                .toList();
    }

    private RapportResponse toResponse(Rapport r) {
        return RapportResponse.builder()
                .idRapport(r.getIdRapport())
                .titre(r.getTitre())
                .typeRapport(r.getTypeRapport().name())
                .periodeDebut(r.getPeriodeDebut())
                .periodeFin(r.getPeriodeFin())
                .dateGeneration(r.getDateGeneration())
                .nomFichier(r.getNomFichier())
                .lu(r.getLu())
                .build();
    }
}
