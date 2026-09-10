package com.example.gestion_rh.controller;


import com.example.gestion_rh.dto.request.PaiementRequest;
import com.example.gestion_rh.dto.response.PageResponse;
import com.example.gestion_rh.dto.response.PaiementResponse;
import com.example.gestion_rh.model.Utilisateur;
import com.example.gestion_rh.security.SecurityUtils;
import com.example.gestion_rh.service.PaiementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PageResponse<PaiementResponse>> findAll(
            @RequestParam(required = false) Integer idEmploye,
            @RequestParam(required = false) Integer mois,
            @RequestParam(required = false) Integer annee,
            @PageableDefault(size = 20) Pageable pageable) {

        if (idEmploye != null) {
            return ResponseEntity.ok(PageResponse.ofList(paiementService.findByEmploye(idEmploye)));
        }
        if (mois != null && annee != null) {
            return ResponseEntity.ok(PageResponse.ofList(paiementService.findByMoisAnnee(mois, annee)));
        }
        return ResponseEntity.ok(paiementService.findAll(pageable));
    }

    @GetMapping("/mes-bulletins")
    @PreAuthorize("hasAnyRole('Admin','Employe')")
    public ResponseEntity<List<PaiementResponse>> mesBulletins(
            @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur u = (Utilisateur) userDetails;
        return ResponseEntity.ok(paiementService.findByEmploye(SecurityUtils.requireIdEmploye(u)));
    }

    /** Préremplit salaire / HS / retenues pour un employé + période. */
    @GetMapping("/calculer")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> calculer(
            @RequestParam Integer idEmploye,
            @RequestParam Integer mois,
            @RequestParam Integer annee) {
        return ResponseEntity.ok(paiementService.calculer(idEmploye, mois, annee));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PaiementResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(paiementService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PaiementResponse> create(@Valid @RequestBody PaiementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paiementService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PaiementResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody PaiementRequest request) {
        return ResponseEntity.ok(paiementService.update(id, request));
    }

    @PatchMapping("/{id}/effectuer")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PaiementResponse> effectuer(@PathVariable Integer id) {
        return ResponseEntity.ok(paiementService.effectuer(id));
    }

    @PatchMapping("/{id}/valider")
    @PreAuthorize("hasRole('Employe')")
    public ResponseEntity<PaiementResponse> valider(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur u = (Utilisateur) userDetails;
        Integer idEmploye = SecurityUtils.requireIdEmploye(u);
        return ResponseEntity.ok(paiementService.validerParEmploye(id, idEmploye, u.getLogin()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        paiementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
