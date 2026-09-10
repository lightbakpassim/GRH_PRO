package com.example.gestion_rh.controller;


import com.example.gestion_rh.dto.request.NotificationRequest;
import com.example.gestion_rh.dto.response.NotificationResponse;
import com.example.gestion_rh.model.Utilisateur;
import com.example.gestion_rh.security.SecurityUtils;
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

    @GetMapping("/mes-notifications")
    @PreAuthorize("hasAnyRole('Admin','Employe')")
    public ResponseEntity<List<NotificationResponse>> mesNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur u = (Utilisateur) userDetails;
        return ResponseEntity.ok(
                notificationService.findByEmploye(SecurityUtils.requireIdEmploye(u)));
    }

    @GetMapping("/mes-notifications/non-lues")
    @PreAuthorize("hasAnyRole('Admin','Employe')")
    public ResponseEntity<List<NotificationResponse>> mesNotificationsNonLues(
            @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur u = (Utilisateur) userDetails;
        return ResponseEntity.ok(
                notificationService.findNonLuesByEmploye(SecurityUtils.requireIdEmploye(u)));
    }

    @GetMapping("/mes-notifications/count")
    @PreAuthorize("hasAnyRole('Admin','Employe')")
    public ResponseEntity<Map<String, Long>> countNonLues(
            @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur u = (Utilisateur) userDetails;
        long count = notificationService.countNonLues(SecurityUtils.requireIdEmploye(u));
        return ResponseEntity.ok(Map.of("nonLues", count));
    }

    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<NotificationResponse> create(
            @Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.create(request));
    }

    @PatchMapping("/{id}/lire")
    @PreAuthorize("hasAnyRole('Admin','Employe')")
    public ResponseEntity<Map<String, Integer>> marquerCommeLue(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        int updated = notificationService.marquerCommeLue(id, (Utilisateur) userDetails);
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    @PatchMapping("/lire-tout")
    @PreAuthorize("hasAnyRole('Admin','Employe')")
    public ResponseEntity<Map<String, Integer>> marquerToutesCommeLues(
            @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur u = (Utilisateur) userDetails;
        int updated = notificationService.marquerToutesCommeLues(SecurityUtils.requireIdEmploye(u));
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
