package com.example.gestion_rh.service;

import com.example.gestion_rh.dto.request.PaiementRequest;
import com.example.gestion_rh.dto.response.PageResponse;
import com.example.gestion_rh.dto.response.PaiementCalculResponse;
import com.example.gestion_rh.dto.response.PaiementResponse;
import com.example.gestion_rh.exception.BusinessException;
import com.example.gestion_rh.exception.ResourceNotFoundException;
import com.example.gestion_rh.model.Absence;
import com.example.gestion_rh.model.Employe;
import com.example.gestion_rh.model.Notification;
import com.example.gestion_rh.model.Paiement;
import com.example.gestion_rh.model.SuiviTemps;
import com.example.gestion_rh.repository.AbsenceRepository;
import com.example.gestion_rh.repository.PaiementRepository;
import com.example.gestion_rh.repository.SuiviTempsRepository;
import com.example.gestion_rh.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final EmployeService employeService;
    private final NotificationService notificationService;
    private final HistoriqueActionService historiqueActionService;
    private final SuiviTempsRepository suiviTempsRepository;
    private final AbsenceRepository absenceRepository;

    /** Heures ouvrées mensuelles pour le taux horaire (défaut 173.33 ≈ 22 j × 7.88 h). */
    @Value("${app.paie.heures-mensuelles:173.33}")
    private BigDecimal heuresMensuelles;

    /** Majoration heures supplémentaires (1.25 = +25 %). */
    @Value("${app.paie.taux-hs:1.25}")
    private BigDecimal tauxHs;

    /** Heures retenues par jour d'absence non justifiée. */
    @Value("${app.paie.heures-par-jour:8}")
    private BigDecimal heuresParJour;

    public PageResponse<PaiementResponse> findAll(Pageable pageable) {
        List<PaiementResponse> all = findAll();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<PaiementResponse> slice = start >= all.size() ? List.of() : all.subList(start, end);
        int totalPages = pageable.getPageSize() == 0 ? 0
                : (int) Math.ceil((double) all.size() / pageable.getPageSize());
        return new PageResponse<>(slice, pageable.getPageNumber(), pageable.getPageSize(), all.size(), totalPages);
    }

    public List<PaiementResponse> findAll() {
        Integer idEntreprise = SecurityUtils.requireIdEntreprise();
        return paiementRepository.findByEntreprise(idEntreprise).stream()
                .map(this::toResponse)
                .toList();
    }

    public PaiementResponse findById(Integer id) {
        return toResponse(getOrThrow(id));
    }

    public List<PaiementResponse> findByEmploye(Integer idEmploye) {
        employeService.getOrThrow(idEmploye); // vérifie le tenant
        return paiementRepository.findHistoriqueByEmploye(idEmploye)
                .stream().map(this::toResponse).toList();
    }

    public List<PaiementResponse> findByMoisAnnee(int mois, int annee) {
        Integer idEntreprise = SecurityUtils.requireIdEntreprise();
        return paiementRepository.findByEntrepriseAndMoisAndAnnee(idEntreprise, mois, annee)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PaiementCalculResponse calculer(Integer idEmploye, Integer mois, Integer annee) {
        if (idEmploye == null || mois == null || annee == null) {
            throw new BusinessException("Employé, mois et année sont obligatoires");
        }
        Employe employe = employeService.getOrThrow(idEmploye);
        BigDecimal salaireBase = employe.getSalaireBase() != null
                ? employe.getSalaireBase()
                : BigDecimal.ZERO;

        YearMonth ym = YearMonth.of(annee, mois);
        LocalDate debut = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();

        BigDecimal heuresSupp = suiviTempsRepository
                .findByEmploye_IdEmployeAndDateTravailBetween(idEmploye, debut, fin)
                .stream()
                .filter(s -> s.getStatutSupp() == SuiviTemps.StatutSupp.Approuvées)
                .map(s -> s.getHeuresSupplementaires() != null ? s.getHeuresSupplementaires() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tauxHoraire = salaireBase
                .divide(heuresMensuelles, 4, RoundingMode.HALF_UP);
        BigDecimal heuresSuppMontant = heuresSupp
                .multiply(tauxHoraire)
                .multiply(tauxHs)
                .setScale(2, RoundingMode.HALF_UP);

        // Absences retenues : Sans_motif / Retard / Autre approuvées (Maladie non retenue)
        List<Absence> absences = absenceRepository
                .findByEmploye_IdEmployeAndDateAbsenceBetween(idEmploye, debut, fin)
                .stream()
                .filter(a -> a.getStatutAbsence() == Absence.StatutAbsence.Approuvée)
                .filter(a -> a.getTypeAbsence() != Absence.TypeAbsence.Maladie)
                .toList();

        long nbJoursRetenus = absences.size();
        BigDecimal heuresRetenues = heuresParJour.multiply(BigDecimal.valueOf(nbJoursRetenus));
        BigDecimal retenues = heuresRetenues
                .multiply(tauxHoraire)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalNet = salaireBase.add(heuresSuppMontant).subtract(retenues)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        String nom = employe.getPrenomEmploye() + " " + employe.getNomEmploye();
        return PaiementCalculResponse.builder()
                .idEmploye(idEmploye)
                .nomCompletEmploye(nom)
                .mois(mois)
                .annee(annee)
                .salaireBase(salaireBase)
                .heuresSupplementaires(heuresSupp.setScale(2, RoundingMode.HALF_UP))
                .heuresSuppMontant(heuresSuppMontant)
                .heuresRetenues(heuresRetenues.setScale(2, RoundingMode.HALF_UP))
                .retenues(retenues)
                .totalNetEstime(totalNet)
                .detailHs(heuresSupp + " h × " + tauxHoraire.setScale(2, RoundingMode.HALF_UP)
                        + " FCFA × " + tauxHs)
                .detailRetenues(nbJoursRetenus + " j abs. retenus × " + heuresParJour
                        + " h × " + tauxHoraire.setScale(2, RoundingMode.HALF_UP) + " FCFA")
                .build();
    }

    public PaiementResponse create(PaiementRequest request) {
        if (paiementRepository.existsByEmploye_IdEmployeAndMoisAndAnnee(
                request.getIdEmploye(), request.getMois(), request.getAnnee())) {
            throw new BusinessException("Un paiement existe déjà pour cet employé sur ce mois/année");
        }

        Employe employe = employeService.getOrThrow(request.getIdEmploye());

        // Si montants non fournis / à 0, recalcule automatiquement
        PaiementCalculResponse calc = calculer(request.getIdEmploye(), request.getMois(), request.getAnnee());
        BigDecimal salaire = request.getSalaireBase() != null
                ? request.getSalaireBase() : calc.getSalaireBase();
        BigDecimal hs = request.getHeuresSuppMontant() != null
                ? request.getHeuresSuppMontant() : calc.getHeuresSuppMontant();
        BigDecimal retenues = request.getRetenues() != null
                ? request.getRetenues() : calc.getRetenues();

        BigDecimal totalNet = salaire.add(hs).subtract(retenues).max(BigDecimal.ZERO);

        Paiement paiement = Paiement.builder()
                .employe(employe)
                .mois(request.getMois())
                .annee(request.getAnnee())
                .salaireBase(salaire)
                .heuresSuppMontant(hs)
                .retenues(retenues)
                .totalNet(totalNet)
                .statut(Paiement.StatutPaiement.En_attente)
                .build();

        Paiement saved = paiementRepository.save(paiement);

        try {
            notificationService.creerNotification(
                    employe,
                    saved,
                    "Votre bulletin de paie de " + getNomMois(request.getMois()) + " " + request.getAnnee() + " est disponible.",
                    Notification.TypeNotification.Paie
            );
        } catch (Exception ignored) {
            // bulletin créé même si notif échoue
        }

        return toResponse(saved);
    }

    public PaiementResponse update(Integer id, PaiementRequest request) {
        Paiement paiement = getOrThrow(id);
        if (paiement.getStatut() != Paiement.StatutPaiement.En_attente) {
            throw new BusinessException("Impossible de modifier un paiement déjà effectué ou validé");
        }

        paiement.setSalaireBase(request.getSalaireBase());
        paiement.setHeuresSuppMontant(request.getHeuresSuppMontant());
        paiement.setRetenues(request.getRetenues());
        paiement.setTotalNet(request.getSalaireBase()
                .add(request.getHeuresSuppMontant())
                .subtract(request.getRetenues())
                .max(BigDecimal.ZERO));

        return toResponse(paiementRepository.save(paiement));
    }

    public PaiementResponse effectuer(Integer id) {
        Paiement paiement = getOrThrow(id);
        if (paiement.getStatut() != Paiement.StatutPaiement.En_attente) {
            throw new BusinessException("Seuls les paiements en attente peuvent être marqués effectués");
        }
        paiement.setStatut(Paiement.StatutPaiement.Effectué);
        paiement.setDatePaiement(LocalDateTime.now());
        Paiement saved = paiementRepository.save(paiement);

        try {
            notificationService.creerNotification(
                    paiement.getEmploye(),
                    saved,
                    "Votre salaire de " + getNomMois(paiement.getMois()) + " " + paiement.getAnnee()
                            + " a été versé (" + paiement.getTotalNet()
                            + " FCFA). Merci de valider la réception dans votre espace.",
                    Notification.TypeNotification.Paie
            );
        } catch (Exception ignored) {
        }

        return toResponse(saved);
    }

    /** L'employé confirme la réception d'un paiement déjà effectué → visible dans l'historique DG. */
    public PaiementResponse validerParEmploye(Integer id, Integer idEmployeConnecte, String loginActeur) {
        Paiement paiement = getOrThrow(id);
        if (paiement.getEmploye() == null
                || !idEmployeConnecte.equals(paiement.getEmploye().getIdEmploye())) {
            throw new BusinessException("Vous ne pouvez valider que vos propres paiements");
        }
        if (paiement.getStatut() == Paiement.StatutPaiement.Validé) {
            throw new BusinessException("Ce paiement est déjà validé");
        }
        if (paiement.getStatut() != Paiement.StatutPaiement.Effectué) {
            throw new BusinessException("Le paiement doit d'abord être effectué par l'administration");
        }

        paiement.setStatut(Paiement.StatutPaiement.Validé);
        Paiement saved = paiementRepository.save(paiement);

        String nom = paiement.getEmploye().getPrenomEmploye() + " " + paiement.getEmploye().getNomEmploye();
        historiqueActionService.enregistrer(
                "PAIEMENT_VALIDE",
                "Paiement effectué et validé — " + nom + " — "
                        + getNomMois(paiement.getMois()) + " " + paiement.getAnnee()
                        + " — net " + paiement.getTotalNet() + " FCFA",
                loginActeur
        );

        return toResponse(saved);
    }

    public void delete(Integer id) {
        Paiement paiement = getOrThrow(id);
        if (paiement.getStatut() != Paiement.StatutPaiement.En_attente) {
            throw new BusinessException("Impossible de supprimer un paiement déjà effectué ou validé");
        }
        paiementRepository.delete(paiement);
    }

    private Paiement getOrThrow(Integer id) {
        Integer idEntreprise = SecurityUtils.requireIdEntreprise();
        return paiementRepository.findByIdPaiementAndEmploye_Entreprise_IdEntreprise(id, idEntreprise)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement introuvable avec l'id : " + id));
    }

    private String getNomMois(int mois) {
        String[] mois_noms = {"", "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        return mois >= 1 && mois <= 12 ? mois_noms[mois] : String.valueOf(mois);
    }

    public PaiementResponse toResponse(Paiement p) {
        String nomComplet = p.getEmploye().getPrenomEmploye() + " " + p.getEmploye().getNomEmploye();
        return PaiementResponse.builder()
                .idPaiement(p.getIdPaiement())
                .idEmploye(p.getEmploye().getIdEmploye())
                .nomCompletEmploye(nomComplet)
                .mois(p.getMois())
                .annee(p.getAnnee())
                .salaireBase(p.getSalaireBase())
                .heuresSuppMontant(p.getHeuresSuppMontant())
                .retenues(p.getRetenues())
                .totalNet(p.getTotalNet())
                .datePaiement(p.getDatePaiement())
                .statut(p.getStatut())
                .build();
    }
}
