package com.example.gestion_rh.controller;


import com.example.gestion_rh.dto.request.PaiementRequest;
import com.example.gestion_rh.dto.response.PaiementResponse;
import com.example.gestion_rh.service.PaiementService;
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
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;

    /**
     * GET /api/paiements
     * Liste tous les paiements. Filtre optionnel par mois/année ou par employé.
     */
    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<PaiementResponse>> findAll(
            @RequestParam(required = false) Integer idEmploye,
            @RequestParam(required = false) Integer mois,
            @RequestParam(required = false) Integer annee) {

        if (idEmploye != null) {
            return ResponseEntity.ok(paiementService.findByEmploye(idEmploye));
        }
        if (mois != null && annee != null) {
            return ResponseEntity.ok(paiementService.findByMoisAnnee(mois, annee));
        }
        return ResponseEntity.ok(paiementService.findAll());
    }

    /**
     * GET /api/paiements/{id}
     * Retourne un paiement par son id
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PaiementResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(paiementService.findById(id));
    }

    /**
     * GET /api/paiements/mes-bulletins
     * L'employé connecté consulte ses propres bulletins de paie
     */
    @GetMapping("/mes-bulletins")
    @PreAuthorize("hasAnyRole('Admin','Employé')")
    public ResponseEntity<List<PaiementResponse>> mesBulletins(
            @AuthenticationPrincipal UserDetails userDetails) {
        com.example.gestion_rh.model.Utilisateur u = (com.example.gestion_rh.model.Utilisateur) userDetails;
        return ResponseEntity.ok(paiementService.findByEmploye(u.getEmploye().getIdEmploye()));
    }

    /**
     * POST /api/paiements
     * Génère un bulletin de paie pour un employé (Admin)
     */
    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PaiementResponse> create(@Valid @RequestBody PaiementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paiementService.create(request));
    }

    /**
     * PUT /api/paiements/{id}
     * Met à jour un bulletin de paie (Admin, uniquement si statut En attente)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PaiementResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody PaiementRequest request) {
        return ResponseEntity.ok(paiementService.update(id, request));
    }

    /**
     * PATCH /api/paiements/{id}/effectuer
     * Marque un paiement comme effectué (Admin)
     */
    @PatchMapping("/{id}/effectuer")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PaiementResponse> effectuer(@PathVariable Integer id) {
        return ResponseEntity.ok(paiementService.effectuer(id));
    }

    /**
     * DELETE /api/paiements/{id}
     * Supprime un paiement (Admin, uniquement si statut En attente)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        paiementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}