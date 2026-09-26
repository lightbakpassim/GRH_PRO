package com.example.gestion_rh.controller;


import com.example.gestion_rh.dto.request.SuiviTempsRequest;
import com.example.gestion_rh.dto.response.SuiviTempsResponse;
import com.example.gestion_rh.service.SuiviTempsService;
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
@RequestMapping("/api/suivi-temps")
@RequiredArgsConstructor
public class SuiviTempsController {

    private final SuiviTempsService suiviTempsService;

    /**
     * GET /api/suivi-temps
     * Admin : tous les suivis. Filtre optionnel par employé ou par mois/année.
     */
    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<SuiviTempsResponse>> findAll(
            @RequestParam(required = false) Integer idEmploye,
            @RequestParam(required = false) Integer mois,
            @RequestParam(required = false) Integer annee) {

        if (idEmploye != null && mois != null && annee != null) {
            return ResponseEntity.ok(suiviTempsService.findByEmployeAndMois(idEmploye, mois, annee));
        }
        if (idEmploye != null) {
            return ResponseEntity.ok(suiviTempsService.findByEmploye(idEmploye));
        }
        return ResponseEntity.ok(suiviTempsService.findAll());
    }

    /**
     * GET /api/suivi-temps/en-attente
     * Liste les heures supplémentaires en attente d'approbation (Admin)
     */
    @GetMapping("/en-attente")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<SuiviTempsResponse>> findEnAttente() {
        return ResponseEntity.ok(suiviTempsService.findEnAttente());
    }

    /**
     * GET /api/suivi-temps/mon-suivi
     * L'employé connecté consulte ses propres suivis de temps
     */
    @GetMapping("/mon-suivi")
    @PreAuthorize("hasAnyRole('Admin','Employé')")
    public ResponseEntity<List<SuiviTempsResponse>> monSuivi(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer mois,
            @RequestParam(required = false) Integer annee) {

        // Récupération de l'id employé depuis le principal
        com.example.gestion_rh.model.Utilisateur u = (com.example.gestion_rh.model.Utilisateur) userDetails;
        Integer idEmploye = u.getEmploye().getIdEmploye();

        if (mois != null && annee != null) {
            return ResponseEntity.ok(suiviTempsService.findByEmployeAndMois(idEmploye, mois, annee));
        }
        return ResponseEntity.ok(suiviTempsService.findByEmploye(idEmploye));
    }

    /**
     * POST /api/suivi-temps
     * Enregistre une entrée de suivi de temps
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('Admin','Employé')")
    public ResponseEntity<SuiviTempsResponse> create(
            @Valid @RequestBody SuiviTempsRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(suiviTempsService.create(request, userDetails.getUsername()));
    }

    /**
     * PATCH /api/suivi-temps/{id}/approuver
     * Approuve les heures supplémentaires d'un suivi (Admin)
     */
    @PatchMapping("/{id}/approuver")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<SuiviTempsResponse> approuver(@PathVariable Integer id) {
        return ResponseEntity.ok(suiviTempsService.approuver(id));
    }

    /**
     * PATCH /api/suivi-temps/{id}/refuser
     * Refuse les heures supplémentaires d'un suivi (Admin)
     */
    @PatchMapping("/{id}/refuser")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<SuiviTempsResponse> refuser(@PathVariable Integer id) {
        return ResponseEntity.ok(suiviTempsService.refuser(id));
    }

    /**
     * DELETE /api/suivi-temps/{id}
     * Supprime un suivi de temps (Admin)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        suiviTempsService.delete(id);
        return ResponseEntity.noContent().build();
    }
}