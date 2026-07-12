package com.example.gestion_rh.controller;


import com.example.gestion_rh.dto.request.UtilisateurRequest;
import com.example.gestion_rh.dto.response.UtilisateurResponse;
import com.example.gestion_rh.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('Admin')")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    /**
     * GET /api/utilisateurs
     * Liste tous les comptes utilisateurs
     */
    @GetMapping
    public ResponseEntity<List<UtilisateurResponse>> findAll() {
        return ResponseEntity.ok(utilisateurService.findAll());
    }

    /**
     * GET /api/utilisateurs/{id}
     * Retourne un utilisateur par son id
     */
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(utilisateurService.findById(id));
    }

    /**
     * POST /api/utilisateurs
     * Crée un compte utilisateur pour un employé
     */
    @PostMapping
    public ResponseEntity<UtilisateurResponse> create(@Valid @RequestBody UtilisateurRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.create(request));
    }

    /**
     * PUT /api/utilisateurs/{id}
     * Met à jour un compte utilisateur
     */
    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody UtilisateurRequest request) {
        return ResponseEntity.ok(utilisateurService.update(id, request));
    }

    /**
     * PATCH /api/utilisateurs/{id}/toggle-statut
     * Active ou désactive un compte utilisateur
     */
    @PatchMapping("/{id}/toggle-statut")
    public ResponseEntity<Void> toggleStatut(@PathVariable Integer id) {
        utilisateurService.toggleStatut(id);
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/utilisateurs/{id}
     * Supprime un compte utilisateur
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        utilisateurService.delete(id);
        return ResponseEntity.noContent().build();
    }
}