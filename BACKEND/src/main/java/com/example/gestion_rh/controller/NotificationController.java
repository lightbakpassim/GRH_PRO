package com.example.gestion_rh.controller;


import com.example.gestion_rh.dto.request.NotificationRequest;
import com.example.gestion_rh.dto.response.NotificationResponse;
import com.example.gestion_rh.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * GET /api/notifications/mes-notifications
     * L'utilisateur connecté récupère toutes ses notifications (triées par date desc)
     */
    @GetMapping("/mes-notifications")
    @PreAuthorize("hasAnyRole('Admin','Employé')")
    public ResponseEntity<List<NotificationResponse>> mesNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        com.example.gestion_rh.model.Utilisateur u = (com.example.gestion_rh.model.Utilisateur) userDetails;
        return ResponseEntity.ok(
                notificationService.findByEmploye(u.getEmploye().getIdEmploye()));
    }

    /**
     * GET /api/notifications/mes-notifications/non-lues
     * Récupère uniquement les notifications non lues de l'utilisateur connecté
     */
    @GetMapping("/mes-notifications/non-lues")
    @PreAuthorize("hasAnyRole('Admin','Employé')")
    public ResponseEntity<List<NotificationResponse>> mesNotificationsNonLues(
            @AuthenticationPrincipal UserDetails userDetails) {
        com.example.gestion_rh.model.Utilisateur u = (com.example.gestion_rh.model.Utilisateur) userDetails;
        return ResponseEntity.ok(
                notificationService.findNonLuesByEmploye(u.getEmploye().getIdEmploye()));
    }

    /**
     * GET /api/notifications/mes-notifications/count
     * Retourne le nombre de notifications non lues (utile pour le badge dans le frontend)
     */
    @GetMapping("/mes-notifications/count")
    @PreAuthorize("hasAnyRole('Admin','Employé')")
    public ResponseEntity<Map<String, Long>> countNonLues(
            @AuthenticationPrincipal UserDetails userDetails) {
        com.example.gestion_rh.model.Utilisateur u = (com.example.gestion_rh.model.Utilisateur) userDetails;
        long count = notificationService.countNonLues(u.getEmploye().getIdEmploye());
        return ResponseEntity.ok(Map.of("nonLues", count));
    }

    /**
     * POST /api/notifications
     * Crée manuellement une notification (Admin uniquement)
     */
    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<NotificationResponse> create(
            @Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.create(request));
    }

    /**
     * PATCH /api/notifications/{id}/lire
     * Marque une notification spécifique comme lue
     */
    @PatchMapping("/{id}/lire")
    @PreAuthorize("hasAnyRole('Admin','Employé')")
    public ResponseEntity<Map<String, Integer>> marquerCommeLue(@PathVariable Integer id) {
        int updated = notificationService.marquerCommeLue(id);
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    /**
     * PATCH /api/notifications/lire-tout
     * Marque toutes les notifications de l'utilisateur connecté comme lues
     */
    @PatchMapping("/lire-tout")
    @PreAuthorize("hasAnyRole('Admin','Employé')")
    public ResponseEntity<Map<String, Integer>> marquerToutesCommeLues(
            @AuthenticationPrincipal UserDetails userDetails) {
        com.example.gestion_rh.model.Utilisateur u = (com.example.gestion_rh.model.Utilisateur) userDetails;
        int updated = notificationService.marquerToutesCommeLues(u.getEmploye().getIdEmploye());
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    /**
     * DELETE /api/notifications/{id}
     * Supprime une notification (Admin)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
