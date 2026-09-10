package com.example.gestion_rh.service;

import com.example.gestion_rh.dto.request.DemandeCongeRequest;
import com.example.gestion_rh.dto.response.DemandeCongeResponse;
import com.example.gestion_rh.dto.response.PageResponse;
import com.example.gestion_rh.exception.BusinessException;
import com.example.gestion_rh.exception.ResourceNotFoundException;
import com.example.gestion_rh.model.DemandeConge;
import com.example.gestion_rh.model.Employe;
import com.example.gestion_rh.model.Utilisateur;
import com.example.gestion_rh.repository.DemandeCongeRepository;
import com.example.gestion_rh.repository.UtilisateurRepository;
import com.example.gestion_rh.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DemandeCongeService {

    private final DemandeCongeRepository demandeCongeRepository;
    private final EmployeService employeService;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;

    public PageResponse<DemandeCongeResponse> findAll(Pageable pageable) {
        return PageResponse.from(demandeCongeRepository.findAll(pageable).map(this::toResponse));
    }

    public List<DemandeCongeResponse> findAll() {
        return demandeCongeRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<DemandeCongeResponse> findByEmploye(Integer idEmploye) {
        return demandeCongeRepository.findByEmploye_IdEmploye(idEmploye)
                .stream().map(this::toResponse).toList();
    }

    public List<DemandeCongeResponse> findEnAttente() {
        return demandeCongeRepository.findByStatutConge(DemandeConge.StatutConge.En_attente)
                .stream().map(this::toResponse).toList();
    }

    public DemandeCongeResponse findById(Integer id, Utilisateur connecte) {
        DemandeConge demande = getOrThrow(id);
        SecurityUtils.assertOwnsEmployeResource(connecte, demande.getEmploye().getIdEmploye());
        return toResponse(demande);
    }

    public DemandeCongeResponse create(DemandeCongeRequest request, Utilisateur connecte) {
        if (request.getDateFin().isBefore(request.getDateDebut())) {
            throw new BusinessException("La date de fin doit être après ou égale à la date de début");
        }

        Integer idEmploye = SecurityUtils.resolveTargetEmployeId(connecte, request.getIdEmploye());
        Employe employe = employeService.getOrThrow(idEmploye);
        Utilisateur utilisateur = utilisateurRepository.getReferenceById(connecte.getIdUtilisateur());

        // Vérification chevauchement de congés
        boolean chevauchement = demandeCongeRepository.existsChevauchement(
                idEmploye, request.getDateDebut(), request.getDateFin());
        if (chevauchement) {
            throw new BusinessException("Il existe déjà une demande de congé sur cette période");
        }

        DemandeConge demande = DemandeConge.builder()
                .employe(employe)
                .utilisateur(utilisateur)
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .motifConge(request.getMotifConge())
                .nbJours((int) ChronoUnit.DAYS.between(request.getDateDebut(), request.getDateFin()) + 1)
                .statutConge(DemandeConge.StatutConge.En_attente)
                .dateDemande(LocalDateTime.now())
                .build();

        DemandeConge saved = demandeCongeRepository.save(demande);

        try {
            notificationService.creerNotification(
                    employe,
                    null,
                    "Votre demande de congé du " + request.getDateDebut() + " au " + request.getDateFin() + " a été soumise.",
                    com.example.gestion_rh.model.Notification.TypeNotification.Congés
            );
        } catch (Exception ignored) {
            // La demande reste valide même si la notif échoue
        }

        return toResponse(saved);
    }

    public DemandeCongeResponse approuver(Integer id) {
        DemandeConge demande = getOrThrow(id);
        if (demande.getStatutConge() != DemandeConge.StatutConge.En_attente) {
            throw new BusinessException("Seules les demandes en attente peuvent être approuvées");
        }
        demande.setStatutConge(DemandeConge.StatutConge.Approuvée);
        demande.setDateApprobation(LocalDateTime.now());
        DemandeConge saved = demandeCongeRepository.save(demande);

        notificationService.creerNotification(
                demande.getEmploye(), null,
                "Votre demande de congé du " + demande.getDateDebut() + " au " + demande.getDateFin() + " a été approuvée.",
                com.example.gestion_rh.model.Notification.TypeNotification.Congés
        );

        return toResponse(saved);
    }

    public DemandeCongeResponse refuser(Integer id) {
        DemandeConge demande = getOrThrow(id);
        if (demande.getStatutConge() != DemandeConge.StatutConge.En_attente) {
            throw new BusinessException("Seules les demandes en attente peuvent être refusées");
        }
        demande.setStatutConge(DemandeConge.StatutConge.Refusée);
        demande.setDateApprobation(LocalDateTime.now());
        DemandeConge saved = demandeCongeRepository.save(demande);

        notificationService.creerNotification(
                demande.getEmploye(), null,
                "Votre demande de congé du " + demande.getDateDebut() + " au " + demande.getDateFin() + " a été refusée.",
                com.example.gestion_rh.model.Notification.TypeNotification.Congés
        );

        return toResponse(saved);
    }

    public void delete(Integer id, Utilisateur connecte) {
        DemandeConge demande = getOrThrow(id);
        SecurityUtils.assertOwnsEmployeResource(connecte, demande.getEmploye().getIdEmploye());
        if (demande.getStatutConge() == DemandeConge.StatutConge.Approuvée) {
            throw new BusinessException("Impossible de supprimer une demande déjà approuvée");
        }
        demandeCongeRepository.delete(demande);
    }

    private DemandeConge getOrThrow(Integer id) {
        return demandeCongeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de congé introuvable avec l'id : " + id));
    }

    public DemandeCongeResponse toResponse(DemandeConge d) {
        String nomComplet = d.getEmploye().getPrenomEmploye() + " " + d.getEmploye().getNomEmploye();
        return DemandeCongeResponse.builder()
                .idConge(d.getIdConge())
                .idEmploye(d.getEmploye().getIdEmploye())
                .nomCompletEmploye(nomComplet)
                .dateDebut(d.getDateDebut())
                .dateFin(d.getDateFin())
                .motifConge(d.getMotifConge())
                .nbJours(d.getNbJours())
                .statutConge(d.getStatutConge())
                .dateDemande(d.getDateDemande())
                .dateApprobation(d.getDateApprobation())
                .build();
    }
}
