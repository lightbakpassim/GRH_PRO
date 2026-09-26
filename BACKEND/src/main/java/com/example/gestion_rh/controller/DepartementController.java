package com.example.gestion_rh.controller;


import com.example.gestion_rh.dto.request.DepartementRequest;
import com.example.gestion_rh.dto.response.DepartementResponse;
import com.example.gestion_rh.service.DepartementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departements")
@RequiredArgsConstructor
@PreAuthorize("hasRole('Admin')")
public class DepartementController {

    private final DepartementService departementService;

    /**
     * GET /api/departements
     * Retourne la liste de tous les départements
     */
    @GetMapping
    public ResponseEntity<List<DepartementResponse>> findAll() {
        return ResponseEntity.ok(departementService.findAll());
    }

    /**
     * GET /api/departements/{id}
     * Retourne un département par son id
     */
    @GetMapping("/{id}")
    public ResponseEntity<DepartementResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(departementService.findById(id));
    }

    /**
     * POST /api/departements
     * Crée un nouveau département
     */
    @PostMapping
    public ResponseEntity<DepartementResponse> create(@Valid @RequestBody DepartementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departementService.create(request));
    }

    /**
     * PUT /api/departements/{id}
     * Met à jour un département
     */
    @PutMapping("/{id}")
    public ResponseEntity<DepartementResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody DepartementRequest request) {
        return ResponseEntity.ok(departementService.update(id, request));
    }

    /**
     * DELETE /api/departements/{id}
     * Supprime un département (si aucun employé n'y est rattaché)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        departementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
