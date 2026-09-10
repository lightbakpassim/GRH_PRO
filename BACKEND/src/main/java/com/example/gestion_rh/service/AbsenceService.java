package com.example.gestion_rh.service;


import com.example.gestion_rh.dto.request.AbsenceRequest;
import com.example.gestion_rh.dto.response.AbsenceResponse;
import com.example.gestion_rh.exception.BusinessException;
import com.example.gestion_rh.exception.ResourceNotFoundException;
import com.example.gestion_rh.model.Absence;
import com.example.gestion_rh.model.Employe;
import com.example.gestion_rh.model.Notification;
import com.example.gestion_rh.model.Utilisateur;
import com.example.gestion_rh.repository.AbsenceRepository;
import com.example.gestion_rh.repository.UtilisateurRepository;
import com.example.gestion_rh.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final EmployeService employeService;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;

    public List<AbsenceResponse> findAll() {
        return absenceRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<AbsenceResponse> findByEmploye(Integer idEmploye) {
        return absenceRepository.findByEmploye_IdEmploye(idEmploye)
                .stream().map(this::toResponse).toList();
    }

    public List<AbsenceResponse> findEnAttente() {
        return absenceRepository.findByStatutAbsence(Absence.StatutAbsence.En_attente)
                .stream().map(this::toResponse).toList();
    }

    public AbsenceResponse findById(Integer id, Utilisateur connecte) {
        Absence absence = getOrThrow(id);
        SecurityUtils.assertOwnsEmployeResource(connecte, absence.getEmploye().getIdEmploye());
        return toResponse(absence);
    }

    public AbsenceResponse create(AbsenceRequest request, Utilisateur connecte) {
        Integer idEmploye = SecurityUtils.resolveTargetEmployeId(connecte, request.getIdEmploye());
        Employe employe = employeService.getOrThrow(idEmploye);
        Utilisateur utilisateur = utilisateurRepository.getReferenceById(connecte.getIdUtilisateur());

        Absence absence = Absence.builder()
                .employe(employe)
                .utilisateur(utilisateur)
                .dateAbsence(request.getDateAbsence())
                .motifAbsence(request.getMotifAbsence())
                .typeAbsence(request.getTypeAbsence())
                .statutAbsence(Absence.StatutAbsence.En_attente)
                .dateDemande(LocalDateTime.now())
                .build();

        Absence saved = absenceRepository.save(absence);

        notificationService.creerNotification(
                employe, null,
                "Votre absence du " + request.getDateAbsence() + " a été enregistrée et est en attente de validation.",
                Notification.TypeNotification.Avertissement
        );

        return toResponse(saved);
    }

    public AbsenceResponse approuver(Integer id) {
        Absence absence = getOrThrow(id);
        if (absence.getStatutAbsence() != Absence.StatutAbsence.En_attente) {
            throw new BusinessException("Seules les absences en attente peuvent être approuvées");
        }
        absence.setStatutAbsence(Absence.StatutAbsence.Approuvée);
        absence.setDateApprobation(LocalDateTime.now());
        Absence saved = absenceRepository.save(absence);

        notificationService.creerNotification(
                absence.getEmploye(), null,
                "Votre absence du " + absence.getDateAbsence() + " a été approuvée.",
                Notification.TypeNotification.Avertissement
        );

        return toResponse(saved);
    }

    public AbsenceResponse refuser(Integer id) {
        Absence absence = getOrThrow(id);
        if (absence.getStatutAbsence() != Absence.StatutAbsence.En_attente) {
            throw new BusinessException("Seules les absences en attente peuvent être refusées");
        }
        absence.setStatutAbsence(Absence.StatutAbsence.Refusée);
        absence.setDateApprobation(LocalDateTime.now());
        Absence saved = absenceRepository.save(absence);

        notificationService.creerNotification(
                absence.getEmploye(), null,
                "Votre absence du " + absence.getDateAbsence() + " a été refusée.",
                Notification.TypeNotification.Avertissement
        );

        return toResponse(saved);
    }

    public void delete(Integer id) {
        absenceRepository.delete(getOrThrow(id));
    }

    private Absence getOrThrow(Integer id) {
        return absenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Absence introuvable avec l'id : " + id));
    }

    public AbsenceResponse toResponse(Absence a) {
        String nomComplet = a.getEmploye().getPrenomEmploye() + " " + a.getEmploye().getNomEmploye();
        return AbsenceResponse.builder()
                .idAbsence(a.getIdAbsence())
                .idEmploye(a.getEmploye().getIdEmploye())
                .nomCompletEmploye(nomComplet)
                .dateAbsence(a.getDateAbsence())
                .motifAbsence(a.getMotifAbsence())
                .statutAbsence(a.getStatutAbsence())
                .typeAbsence(a.getTypeAbsence())
                .dateDemande(a.getDateDemande())
                .dateApprobation(a.getDateApprobation())
                .build();
    }
}