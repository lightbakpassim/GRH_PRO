package com.example.gestion_rh.service;


import com.example.gestion_rh.dto.request.PaiementRequest;
import com.example.gestion_rh.dto.response.PaiementResponse;
import com.example.gestion_rh.exception.BusinessException;
import com.example.gestion_rh.exception.ResourceNotFoundException;
import com.example.gestion_rh.model.Employe;
import com.example.gestion_rh.model.Notification;
import com.example.gestion_rh.model.Paiement;
import com.example.gestion_rh.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final EmployeService employeService;
    private final NotificationService notificationService;

    public List<PaiementResponse> findAll() {
        return paiementRepository.findAll().stream().map(this::toResponse).toList();
    }

    public PaiementResponse findById(Integer id) {
        return toResponse(getOrThrow(id));
    }

    public List<PaiementResponse> findByEmploye(Integer idEmploye) {
        return paiementRepository.findHistoriqueByEmploye(idEmploye)
                .stream().map(this::toResponse).toList();
    }

    public List<PaiementResponse> findByMoisAnnee(int mois, int annee) {
        return paiementRepository.findByMoisAndAnnee(mois, annee)
                .stream().map(this::toResponse).toList();
    }

    public PaiementResponse create(PaiementRequest request) {
        // Vérification doublon : un seul paiement par employé par mois/année
        if (paiementRepository.existsByEmploye_IdEmployeAndMoisAndAnnee(
                request.getIdEmploye(), request.getMois(), request.getAnnee())) {
            throw new BusinessException("Un paiement existe déjà pour cet employé sur ce mois/année");
        }

        Employe employe = employeService.getOrThrow(request.getIdEmploye());

        Paiement paiement = Paiement.builder()
                .employe(employe)
                .mois(request.getMois())
                .annee(request.getAnnee())
                .salaireBase(request.getSalaireBase())
                .heuresSuppMontant(request.getHeuresSuppMontant())
                .retenues(request.getRetenues())
                .totalNet(BigDecimal.ZERO) // calculé par le trigger BDD
                .statut(Paiement.StatutPaiement.En_attente)
                .build();

        Paiement saved = paiementRepository.save(paiement);

        // Notification de création de bulletin
        notificationService.creerNotification(
                employe,
                saved,
                "Votre bulletin de paie de " + getNomMois(request.getMois()) + " " + request.getAnnee() + " est disponible.",
                Notification.TypeNotification.Paie
        );

        return toResponse(saved);
    }

    public PaiementResponse update(Integer id, PaiementRequest request) {
        Paiement paiement = getOrThrow(id);
        if (paiement.getStatut() == Paiement.StatutPaiement.Effectué) {
            throw new BusinessException("Impossible de modifier un paiement déjà effectué");
        }

        paiement.setSalaireBase(request.getSalaireBase());
        paiement.setHeuresSuppMontant(request.getHeuresSuppMontant());
        paiement.setRetenues(request.getRetenues());
        paiement.setTotalNet(BigDecimal.ZERO); // recalculé par le trigger BDD lors du UPDATE

        return toResponse(paiementRepository.save(paiement));
    }

    public PaiementResponse effectuer(Integer id) {
        Paiement paiement = getOrThrow(id);
        if (paiement.getStatut() == Paiement.StatutPaiement.Effectué) {
            throw new BusinessException("Ce paiement a déjà été effectué");
        }
        paiement.setStatut(Paiement.StatutPaiement.Effectué);
        paiement.setDatePaiement(LocalDateTime.now());
        Paiement saved = paiementRepository.save(paiement);

        notificationService.creerNotification(
                paiement.getEmploye(),
                saved,
                "Votre salaire de " + getNomMois(paiement.getMois()) + " " + paiement.getAnnee()
                        + " a été versé. Montant net : " + paiement.getTotalNet() + " FCFA.",
                Notification.TypeNotification.Paie
        );

        return toResponse(saved);
    }

    public void delete(Integer id) {
        Paiement paiement = getOrThrow(id);
        if (paiement.getStatut() == Paiement.StatutPaiement.Effectué) {
            throw new BusinessException("Impossible de supprimer un paiement déjà effectué");
        }
        paiementRepository.delete(paiement);
    }

    private Paiement getOrThrow(Integer id) {
        return paiementRepository.findById(id)
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
