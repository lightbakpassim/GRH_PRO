package com.example.gestion_rh.controller;


import com.example.gestion_rh.dto.request.DemandeCongeRequest;
import com.example.gestion_rh.dto.response.DemandeCongeResponse;
import com.example.gestion_rh.dto.response.PageResponse;
import com.example.gestion_rh.model.Utilisateur;
import com.example.gestion_rh.security.SecurityUtils;
import com.example.gestion_rh.service.DemandeCongeService;
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
@RequestMapping("/api/conges")
@RequiredArgsConstructor
public class DemandeCongeController {

    private final DemandeCongeService demandeCongeService;

    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PageResponse<DemandeCongeResponse>> findAll(
            @RequestParam(required = false) Integer idEmploye,
            @PageableDefault(size = 20) Pageable pageable) {
        if (idEmploye != null) {
            return ResponseEntity.ok(PageResponse.ofList(demandeCongeService.findByEmploye(idEmploye)));
        }
        return ResponseEntity.ok(demandeCongeService.findAll(pageable));
    }

    @GetMapping("/en-attente")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<DemandeCongeResponse>> findEnAttente() {
        return ResponseEntity.ok(demandeCongeService.findEnAttente());
    }

    @GetMapping("/mes-conges")
    @PreAuthorize("hasAnyRole('Admin','Employe')")
    public ResponseEntity<List<DemandeCongeResponse>> mesConges(
            @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur u = (Utilisateur) userDetails;
        return ResponseEntity.ok(demandeCongeService.findByEmploye(SecurityUtils.requireIdEmploye(u)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','Employe')")
    public ResponseEntity<DemandeCongeResponse> findById(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(demandeCongeService.findById(id, (Utilisateur) userDetails));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin','Employe')")
    public ResponseEntity<DemandeCongeResponse> create(
            @Valid @RequestBody DemandeCongeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(demandeCongeService.create(request, (Utilisateur) userDetails));
    }

    @PatchMapping("/{id}/approuver")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<DemandeCongeResponse> approuver(@PathVariable Integer id) {
        return ResponseEntity.ok(demandeCongeService.approuver(id));
    }

    @PatchMapping("/{id}/refuser")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<DemandeCongeResponse> refuser(@PathVariable Integer id) {
        return ResponseEntity.ok(demandeCongeService.refuser(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','Employe')")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        demandeCongeService.delete(id, (Utilisateur) userDetails);
        return ResponseEntity.noContent().build();
    }
}
