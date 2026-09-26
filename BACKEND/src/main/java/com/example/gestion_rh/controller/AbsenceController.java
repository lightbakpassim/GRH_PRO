package com.example.gestion_rh.controller;


import com.example.gestion_rh.dto.request.AbsenceRequest;
import com.example.gestion_rh.dto.response.AbsenceResponse;
import com.example.gestion_rh.service.AbsenceService;
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
@RequestMapping("/api/absences")
@RequiredArgsConstructor
public class AbsenceController {

    private final AbsenceService absenceService;

    /**
     * GET /api/absences
     * Admin : toutes les absences. Filtre optionnel par employé.
     */
    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<AbsenceResponse>> findAll(
            @RequestParam(required = false) Integer idEmploye) {
        if (idEmploye != null) {
            return ResponseEntity.ok(absenceService.findByEmploye(idEmploye));
        }
        return ResponseEntity.ok(absenceService.findAll());
    }

    /**
     * GET /api/absences/en-attente
     * Liste les absences en attente de validation (Admin)
     */
    @GetMapping("/en-attente")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<AbsenceResponse>> findEnAttente() {
        return ResponseEntity.ok(absenceService.findEnAttente());
    }

    /**
     * GET /api/absences/{id}
     * Retourne une absence par son id
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','Employé')")
    public ResponseEntity<AbsenceResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(absenceService.findById(id));
    }

    /**
     * GET /api/absences/mes-absences
     * L'employé connecté consulte ses propres absences
     */
    @GetMapping("/mes-absences")
    @PreAuthorize("hasAnyRole('Admin','Employé')")
    public ResponseEntity<List<AbsenceResponse>> mesAbsences(
            @AuthenticationPrincipal UserDetails userDetails) {
        com.example.gestion_rh.model.Utilisateur u = (com.example.gestion_rh.model.Utilisateur) userDetails;
        return ResponseEntity.ok(absenceService.findByEmploye(u.getEmploye().getIdEmploye()));
    }

    /**
     * POST /api/absences
     * Déclare une absence
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('Admin','Employé')")
    public ResponseEntity<AbsenceResponse> create(
            @Valid @RequestBody AbsenceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(absenceService.create(request, userDetails.getUsername()));
    }

    /**
     * PATCH /api/absences/{id}/approuver
     * Approuve une absence (Admin)
     */
    @PatchMapping("/{id}/approuver")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<AbsenceResponse> approuver(@PathVariable Integer id) {
        return ResponseEntity.ok(absenceService.approuver(id));
    }

    /**
     * PATCH /api/absences/{id}/refuser
     * Refuse une absence (Admin)
     */
    @PatchMapping("/{id}/refuser")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<AbsenceResponse> refuser(@PathVariable Integer id) {
        return ResponseEntity.ok(absenceService.refuser(id));
    }

    /**
     * DELETE /api/absences/{id}
     * Supprime une absence (Admin)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        absenceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
