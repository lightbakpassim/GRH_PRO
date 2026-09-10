package com.example.gestion_rh.service;

import com.example.gestion_rh.dto.response.HistoriqueActionResponse;
import com.example.gestion_rh.dto.response.RapportResponse;
import com.example.gestion_rh.exception.BusinessException;
import com.example.gestion_rh.exception.ResourceNotFoundException;
import com.example.gestion_rh.model.Entreprise;
import com.example.gestion_rh.model.Rapport;
import com.example.gestion_rh.model.Utilisateur;
import com.example.gestion_rh.repository.EntrepriseRepository;
import com.example.gestion_rh.repository.HistoriqueActionRepository;
import com.example.gestion_rh.repository.RapportRepository;
import com.example.gestion_rh.repository.UtilisateurRepository;
import com.example.gestion_rh.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RapportService {

    public static final ZoneId ZONE_RAPPORT = ZoneId.of("Africa/Lome");
    private static final LocalTime HEURE_GENERATION = LocalTime.of(22, 0);

    private final RapportRepository rapportRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final PdfReportService pdfReportService;
    private final HistoriqueActionService historiqueActionService;
    private final HistoriqueActionRepository historiqueActionRepository;
    private final EmailService emailService;

    /** Génération manuelle RH — uniquement pour l'entreprise du compte, semaine terminée. */
    public Rapport genererPourEntrepriseCourante() {
        assertSemaineTerminee();
        Integer idEntreprise = SecurityUtils.requireIdEntreprise();
        Entreprise entreprise = entrepriseRepository.findById(idEntreprise)
                .orElseThrow(() -> new ResourceNotFoundException("Entreprise introuvable"));
        LocalDate[] periode = periodeSemaineCouranteOuAchevee();
        return genererEtLivrerPourEntreprise(entreprise, periode[0], periode[1], false);
    }

    /** Job vendredi 22h : un PDF par entreprise active, livré à son DG. */
    public List<Rapport> genererTousLesRapportsHebdomadaires() {
        LocalDate[] periode = periodeSemaineLundiVendredi(LocalDate.now(ZONE_RAPPORT));
        List<Rapport> generes = new ArrayList<>();
        for (Entreprise e : entrepriseRepository.findAll()) {
            if (e.getStatut() != Entreprise.StatutEntreprise.Actif) {
                continue;
            }
            try {
                generes.add(genererEtLivrerPourEntreprise(e, periode[0], periode[1], true));
            } catch (Exception ex) {
                log.error("Rapport hebdo échoué pour entreprise {} : {}",
                        e.getNomEntreprise(), ex.getMessage(), ex);
            }
        }
        return generes;
    }

    public Rapport genererEtLivrerPourEntreprise(Entreprise entreprise,
                                                 LocalDate debut,
                                                 LocalDate fin,
                                                 boolean ignoreDoublon) {
        Utilisateur dg = utilisateurRepository.findByEntreprise_IdEntreprise(entreprise.getIdEntreprise())
                .stream()
                .filter(u -> u.getRole() == Utilisateur.Role.DG
                        && u.getStatutUtilisateur() == Utilisateur.StatutUtilisateur.Actif)
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "Aucun DG actif pour « " + entreprise.getNomEntreprise() + " »"));

        boolean deja = rapportRepository.existsByEntreprise_IdEntrepriseAndPeriodeDebutAndPeriodeFinAndTypeRapport(
                entreprise.getIdEntreprise(), debut, fin, Rapport.TypeRapport.HEBDOMADAIRE);
        if (deja && !ignoreDoublon) {
            throw new BusinessException("Un rapport existe déjà pour cette période");
        }
        if (deja) {
            log.info("Rapport déjà présent pour {} [{} → {}] — ignoré",
                    entreprise.getNomEntreprise(), debut, fin);
            return rapportRepository.findByEntreprise_IdEntrepriseOrderByDateGenerationDesc(
                            entreprise.getIdEntreprise()).stream()
                    .filter(r -> Objects.equals(r.getPeriodeDebut(), debut)
                            && Objects.equals(r.getPeriodeFin(), fin))
                    .findFirst()
                    .orElseThrow();
        }

        byte[] pdf = pdfReportService.genererRapportHebdomadaire(entreprise, debut, fin);
        String slug = entreprise.getNomEntreprise()
                .replaceAll("[^a-zA-Z0-9_-]", "_")
                .toLowerCase();
        String nomFichier = "rapport-hebdo-" + slug + "-" + debut + "_" + fin + ".pdf";
        String titre = "Rapport hebdomadaire — " + entreprise.getNomEntreprise() + " — "
                + debut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + " – " + fin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        Rapport rapport = rapportRepository.save(Rapport.builder()
                .titre(titre)
                .typeRapport(Rapport.TypeRapport.HEBDOMADAIRE)
                .periodeDebut(debut)
                .periodeFin(fin)
                .dateGeneration(LocalDateTime.now())
                .destinataire(dg)
                .entreprise(entreprise)
                .fichierPdf(pdf)
                .nomFichier(nomFichier)
                .lu(false)
                .build());

        historiqueActionService.enregistrer(
                "RAPPORT_HEBDO",
                "Rapport " + titre + " livré au DG (" + dg.getLogin() + ")",
                "SYSTEME",
                entreprise);

        try {
            emailService.envoyerRapportPdf(
                    dg.getLogin(),
                    entreprise.getNomEntreprise(),
                    titre,
                    nomFichier,
                    pdf);
        } catch (Exception ex) {
            log.warn("Email rapport non envoyé à {} : {}", dg.getLogin(), ex.getMessage());
        }

        return rapport;
    }

    public Map<String, Object> statutGeneration() {
        ZonedDateTime now = ZonedDateTime.now(ZONE_RAPPORT);
        boolean autorise = semaineTerminee(now);
        LocalDate[] periode = periodeSemaineCouranteOuAchevee();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("planification", "Vendredi 22:00 Africa/Lome");
        map.put("fuseau", ZONE_RAPPORT.getId());
        map.put("maintenant", now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        map.put("semaineTerminee", autorise);
        map.put("generationAutorisee", autorise);
        map.put("message", autorise
                ? "La semaine est terminée — génération possible"
                : "La semaine n'est pas terminée — génération disponible vendredi à partir de 22h");
        map.put("periodeCibleDebut", periode[0].toString());
        map.put("periodeCibleFin", periode[1].toString());
        map.put("livraison", "Espace DG + email (si MAIL_ENABLED)");
        return map;
    }

    public boolean semaineTerminee() {
        return semaineTerminee(ZonedDateTime.now(ZONE_RAPPORT));
    }

    private boolean semaineTerminee(ZonedDateTime now) {
        DayOfWeek dow = now.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return true;
        }
        if (dow == DayOfWeek.FRIDAY) {
            return !now.toLocalTime().isBefore(HEURE_GENERATION);
        }
        return false;
    }

    private void assertSemaineTerminee() {
        if (!semaineTerminee()) {
            throw new BusinessException(
                    "Impossible de générer le rapport : la semaine n'est pas encore terminée "
                            + "(disponible vendredi à partir de 22h, fuseau Africa/Lome)");
        }
    }

    /** Lundi–vendredi de la semaine courante (si ven.≥22h / week-end) ou dernière semaine achevée. */
    private LocalDate[] periodeSemaineCouranteOuAchevee() {
        LocalDate today = LocalDate.now(ZONE_RAPPORT);
        DayOfWeek dow = today.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY
                || (dow == DayOfWeek.FRIDAY && semaineTerminee())) {
            return periodeSemaineLundiVendredi(today);
        }
        LocalDate lastFriday = today.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
        return periodeSemaineLundiVendredi(lastFriday);
    }

    private LocalDate[] periodeSemaineLundiVendredi(LocalDate ref) {
        LocalDate debut = ref.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate fin = debut.plusDays(4);
        return new LocalDate[]{debut, fin};
    }

    @Transactional(readOnly = true)
    public List<RapportResponse> listerPourUtilisateur(Utilisateur utilisateur) {
        Integer idEntreprise = utilisateur.getEntreprise() != null
                ? utilisateur.getEntreprise().getIdEntreprise() : null;
        if (idEntreprise == null) {
            return List.of();
        }
        List<Rapport> rapports;
        if (utilisateur.getRole() == Utilisateur.Role.DG) {
            rapports = rapportRepository.findByDestinataireOrderByDateGenerationDesc(utilisateur);
        } else {
            rapports = rapportRepository.findByEntreprise_IdEntrepriseOrderByDateGenerationDesc(idEntreprise);
        }
        return rapports.stream()
                .filter(r -> r.getEntreprise() != null
                        && Objects.equals(r.getEntreprise().getIdEntreprise(), idEntreprise))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Rapport getPdf(Integer id, Utilisateur utilisateur) {
        Rapport rapport = rapportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rapport introuvable"));
        assertAccesRapport(rapport, utilisateur);
        return rapport;
    }

    public void marquerLu(Integer id, Utilisateur utilisateur) {
        Rapport rapport = getPdf(id, utilisateur);
        rapport.setLu(true);
        rapportRepository.save(rapport);
    }

    private void assertAccesRapport(Rapport rapport, Utilisateur utilisateur) {
        Integer idEntrepriseUser = utilisateur.getEntreprise() != null
                ? utilisateur.getEntreprise().getIdEntreprise() : null;
        Integer idEntrepriseRapport = rapport.getEntreprise() != null
                ? rapport.getEntreprise().getIdEntreprise() : null;

        if (idEntrepriseUser == null || idEntrepriseRapport == null) {
            throw new AccessDeniedException("Accès refusé à ce rapport");
        }
        if (!idEntrepriseRapport.equals(idEntrepriseUser)) {
            throw new AccessDeniedException("Accès refusé — rapport d'une autre entreprise");
        }
        if (utilisateur.getRole() == Utilisateur.Role.DG
                && !rapport.getDestinataire().getIdUtilisateur().equals(utilisateur.getIdUtilisateur())) {
            throw new AccessDeniedException("Accès refusé à ce rapport");
        }
        if (utilisateur.getRole() != Utilisateur.Role.Admin
                && utilisateur.getRole() != Utilisateur.Role.DG) {
            throw new AccessDeniedException("Accès refusé à ce rapport");
        }
    }

    @Transactional(readOnly = true)
    public List<HistoriqueActionResponse> historique() {
        Integer idEntreprise = SecurityUtils.requireIdEntreprise();
        return historiqueActionRepository
                .findByEntreprise_IdEntrepriseOrderByDateActionDesc(idEntreprise).stream()
                .map(h -> HistoriqueActionResponse.builder()
                        .idHistorique(h.getIdHistorique())
                        .action(h.getAction())
                        .detail(h.getDetail())
                        .acteurLogin(h.getActeurLogin())
                        .dateAction(h.getDateAction())
                        .build())
                .toList();
    }

    @Transactional
    public int viderHistorique() {
        Integer idEntreprise = SecurityUtils.requireIdEntreprise();
        return historiqueActionRepository.deleteByEntrepriseId(idEntreprise);
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
