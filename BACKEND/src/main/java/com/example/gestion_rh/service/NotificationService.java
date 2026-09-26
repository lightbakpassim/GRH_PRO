package com.example.gestion_rh.service;


import com.example.gestion_rh.dto.request.NotificationRequest;
import com.example.gestion_rh.dto.response.NotificationResponse;
import com.example.gestion_rh.exception.ResourceNotFoundException;
import com.example.gestion_rh.model.Employe;
import com.example.gestion_rh.model.Notification;
import com.example.gestion_rh.model.Paiement;
import com.example.gestion_rh.repository.NotificationRepository;
import com.example.gestion_rh.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final PaiementRepository paiementRepository;

    public List<NotificationResponse> findByEmploye(Integer idEmploye) {
        return notificationRepository.findByEmploye_IdEmployeOrderByDateEnvoiDesc(idEmploye)
                .stream().map(this::toResponse).toList();
    }

    public List<NotificationResponse> findNonLuesByEmploye(Integer idEmploye) {
        return notificationRepository.findByEmploye_IdEmployeAndLu(idEmploye, false)
                .stream().map(this::toResponse).toList();
    }

    public long countNonLues(Integer idEmploye) {
        return notificationRepository.countByEmploye_IdEmployeAndLu(idEmploye, false);
    }

    public NotificationResponse create(NotificationRequest request) {
        Employe employe = new Employe();
        employe.setIdEmploye(request.getIdEmploye());

        Paiement paiement = null;
        if (request.getIdPaiement() != null) {
            paiement = paiementRepository.findById(request.getIdPaiement())
                    .orElseThrow(() -> new ResourceNotFoundException("Paiement introuvable"));
        }

        Notification notif = Notification.builder()
                .employe(employe)
                .paiement(paiement)
                .message(request.getMessage())
                .typeNotification(request.getTypeNotification())
                .dateEnvoi(LocalDateTime.now())
                .lu(false)
                .build();

        return toResponse(notificationRepository.save(notif));
    }

    // Méthode interne utilisée par les autres services
    public void creerNotification(Employe employe, Paiement paiement, String message,
                                  Notification.TypeNotification type) {
        Notification notif = Notification.builder()
                .employe(employe)
                .paiement(paiement)
                .message(message)
                .typeNotification(type)
                .dateEnvoi(LocalDateTime.now())
                .lu(false)
                .build();
        notificationRepository.save(notif);
    }

    public int marquerCommeLue(Integer idNotification) {
        return notificationRepository.marquerCommeLue(idNotification);
    }

    public int marquerToutesCommeLues(Integer idEmploye) {
        return notificationRepository.marquerToutesCommeLues(idEmploye);
    }

    public void delete(Integer id) {
        Notification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable avec l'id : " + id));
        notificationRepository.delete(notif);
    }

    public NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .idNotification(n.getIdNotification())
                .idEmploye(n.getEmploye() != null ? n.getEmploye().getIdEmploye() : null)
                .idPaiement(n.getPaiement() != null ? n.getPaiement().getIdPaiement() : null)
                .message(n.getMessage())
                .typeNotification(n.getTypeNotification())
                .dateEnvoi(n.getDateEnvoi())
                .lu(n.getLu())
                .build();
    }
}
