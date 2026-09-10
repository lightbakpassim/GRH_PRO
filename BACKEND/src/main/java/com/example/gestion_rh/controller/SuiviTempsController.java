package com.example.gestion_rh.controller;


import com.example.gestion_rh.dto.request.SuiviTempsRequest;
import com.example.gestion_rh.dto.response.PageResponse;
import com.example.gestion_rh.dto.response.SuiviTempsResponse;
import com.example.gestion_rh.model.Utilisateur;
import com.example.gestion_rh.security.SecurityUtils;
import com.example.gestion_rh.service.SuiviTempsService;
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
@RequestMapping("/api/suivi-temps")
@RequiredArgsConstructor
public class SuiviTempsController {

    private final SuiviTempsService suiviTempsService;

    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) Integer idEmploye,
            @RequestParam(required = false) Integer mois,
            @RequestParam(required = false) Integer annee,
            @PageableDefault(size = 20) Pageable pageable) {

        if (idEmploye != null && mois != null && annee != null) {
            return ResponseEntity.ok(PageResponse.ofList(
                    suiviTempsService.findByEmployeAndMois(idEmploye, mois, annee)));
        }
        if (idEmploye != null) {
            return ResponseEntity.ok(PageResponse.ofList(suiviTempsService.findByEmploye(idEmploye)));
        }
        return ResponseEntity.ok(suiviTempsService.findAll(pageable));
    }

    @GetMapping("/en-attente")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<SuiviTempsResponse>> findEnAttente() {
        return ResponseEntity.ok(suiviTempsService.findEnAttente());
    }

    @GetMapping("/mon-suivi")
    @PreAuthorize("hasAnyRole('Admin','Employe')")
    public ResponseEntity<List<SuiviTempsResponse>> monSuivi(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer mois,
            @RequestParam(required = false) Integer annee) {

        Utilisateur u = (Utilisateur) userDetails;
        Integer id = SecurityUtils.requireIdEmploye(u);

        if (mois != null && annee != null) {
            return ResponseEntity.ok(suiviTempsService.findByEmployeAndMois(id, mois, annee));
        }
        return ResponseEntity.ok(suiviTempsService.findByEmploye(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin','Employe')")
    public ResponseEntity<SuiviTempsResponse> create(
            @Valid @RequestBody SuiviTempsRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(suiviTempsService.create(request, (Utilisateur) userDetails));
    }

    @PatchMapping("/{id}/approuver")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<SuiviTempsResponse> approuver(@PathVariable Integer id) {
        return ResponseEntity.ok(suiviTempsService.approuver(id));
    }

    @PatchMapping("/{id}/refuser")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<SuiviTempsResponse> refuser(@PathVariable Integer id) {
        return ResponseEntity.ok(suiviTempsService.refuser(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        suiviTempsService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
