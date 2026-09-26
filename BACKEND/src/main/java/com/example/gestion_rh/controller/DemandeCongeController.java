package com.example.gestion_rh.controller;


import com.example.gestion_rh.dto.request.DemandeCongeRequest;
import com.example.gestion_rh.dto.response.DemandeCongeResponse;
import com.example.gestion_rh.service.DemandeCongeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conges")
@RequiredArgsConstructor
public class DemandeCongeController {

    private final DemandeCongeService demandeCongeService;

    /**
     * GET /api/conges
     * Admin : toutes les demandes. Filtre optionnel par employé.
     */
    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<DemandeCongeResponse>> findAll(
            @RequestParam(required = false) Integer idEmploye) {
        if (idEmploye != null) {
            return ResponseEntity.ok(demandeCongeService.findByEmploye(idEmploye));
        }
        return ResponseEntity.ok(demandeCongeService.findAll());
    }

    /**
     * GET /api/conges/en-attente
     * Liste les demandes de congé en attente (Admin)
     */
    @GetMapping("/en-attente")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<DemandeCongeResponse>> findEnAttente() {
        return ResponseEntity.ok(demandeCongeService.findEnAttente());
    }

    /**
     * GET /api/conges/{id}
     * Retourne une demande de congé par son id
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','Employé')")
    public ResponseEntity<DemandeCongeResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(demandeCongeService.findById(id));
    }

    /**
     * GET /api/conges/mes-conges
     * L'employé connecté consulte ses propres demandes de congé
     */
    @GetMapping("/mes-conges")
    @PreAuthorize("hasAnyRole('Admin','Employé')")
    public ResponseEntity<List<DemandeCongeResponse>> mesConges(
            @AuthenticationPrincipal UserDetails userDetails) {
        com.example.gestion_rh.model.Utilisateur u = (com.example.gestion_rh.model.Utilisateur) userDetails;
        return ResponseEntity.ok(demandeCongeService.findByEmploye(u.getEmploye().getIdEmploye()));
    }

    /**
     * POST /api/conges
     * Soumet une demande de congé
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('Admin','Employé')")
    public ResponseEntity<DemandeCongeResponse> create(
            @Valid @RequestBody DemandeCongeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(demandeCongeService.create(request, userDetails.getUsername()));
    }

    /**
     * PATCH /api/conges/{id}/approuver
     * Approuve une demande de congé (Admin)
     */
    @PatchMapping("/{id}/approuver")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<DemandeCongeResponse> approuver(@PathVariable Integer id) {
        return ResponseEntity.ok(demandeCongeService.approuver(id));
    }

    /**
     * PATCH /api/conges/{id}/refuser
     * Refuse une demande de congé (Admin)
     */
    @PatchMapping("/{id}/refuser")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<DemandeCongeResponse> refuser(@PathVariable Integer id) {
        return ResponseEntity.ok(demandeCongeService.refuser(id));
    }

    /**
     * DELETE /api/conges/{id}
     * Supprime une demande de congé (non approuvée uniquement)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','Employé')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        demandeCongeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}