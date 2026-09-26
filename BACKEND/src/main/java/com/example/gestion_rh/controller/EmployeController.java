package com.example.gestion_rh.controller;



import com.example.gestion_rh.dto.request.EmployeRequest;
import com.example.gestion_rh.dto.response.EmployeResponse;
import com.example.gestion_rh.service.EmployeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('Admin')")
public class EmployeController {

    private final EmployeService employeService;

    /**
     * GET /api/employes
     * Liste tous les employés. Filtre optionnel par département ou recherche par nom.
     */
    @GetMapping
    public ResponseEntity<List<EmployeResponse>> findAll(
            @RequestParam(required = false) Integer idDepartement,
            @RequestParam(required = false) String search) {

        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(employeService.search(search));
        }
        if (idDepartement != null) {
            return ResponseEntity.ok(employeService.findByDepartement(idDepartement));
        }
        return ResponseEntity.ok(employeService.findAll());
    }

    /**
     * GET /api/employes/{id}
     * Retourne un employé par son id
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmployeResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(employeService.findById(id));
    }

    /**
     * POST /api/employes
     * Crée un nouvel employé
     */
    @PostMapping
    public ResponseEntity<EmployeResponse> create(@Valid @RequestBody EmployeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeService.create(request));
    }

    /**
     * PUT /api/employes/{id}
     * Met à jour les informations d'un employé
     */
    @PutMapping("/{id}")
    public ResponseEntity<EmployeResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody EmployeRequest request) {
        return ResponseEntity.ok(employeService.update(id, request));
    }

    /**
     * DELETE /api/employes/{id}
     * Supprime un employé
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        employeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}