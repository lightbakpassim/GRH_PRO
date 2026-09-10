package com.example.gestion_rh.controller;


import com.example.gestion_rh.dto.request.AbsenceRequest;
import com.example.gestion_rh.dto.response.AbsenceResponse;
import com.example.gestion_rh.model.Utilisateur;
import com.example.gestion_rh.security.SecurityUtils;
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

    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<AbsenceResponse>> findAll(
            @RequestParam(required = false) Integer idEmploye) {
        if (idEmploye != null) {
            return ResponseEntity.ok(absenceService.findByEmploye(idEmploye));
        }
        return ResponseEntity.ok(absenceService.findAll());
    }

    @GetMapping("/en-attente")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<AbsenceResponse>> findEnAttente() {
        return ResponseEntity.ok(absenceService.findEnAttente());
    }

    @GetMapping("/mes-absences")
    @PreAuthorize("hasAnyRole('Admin','Employe')")
    public ResponseEntity<List<AbsenceResponse>> mesAbsences(
            @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur u = (Utilisateur) userDetails;
        return ResponseEntity.ok(absenceService.findByEmploye(SecurityUtils.requireIdEmploye(u)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','Employe')")
    public ResponseEntity<AbsenceResponse> findById(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(absenceService.findById(id, (Utilisateur) userDetails));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin','Employe')")
    public ResponseEntity<AbsenceResponse> create(
            @Valid @RequestBody AbsenceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(absenceService.create(request, (Utilisateur) userDetails));
    }

    @PatchMapping("/{id}/approuver")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<AbsenceResponse> approuver(@PathVariable Integer id) {
        return ResponseEntity.ok(absenceService.approuver(id));
    }

    @PatchMapping("/{id}/refuser")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<AbsenceResponse> refuser(@PathVariable Integer id) {
        return ResponseEntity.ok(absenceService.refuser(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        absenceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
