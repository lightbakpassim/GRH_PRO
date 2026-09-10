package com.example.gestion_rh.controller;



import com.example.gestion_rh.dto.request.EmployeRequest;
import com.example.gestion_rh.dto.response.EmployeResponse;
import com.example.gestion_rh.dto.response.PageResponse;
import com.example.gestion_rh.service.EmployeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('Admin')")
public class EmployeController {

    private final EmployeService employeService;

    /**
     * GET /api/employes?page=0&size=20
     * Liste paginée. Filtres optionnels : idDepartement, search (non paginés).
     */
    @GetMapping
    public ResponseEntity<PageResponse<EmployeResponse>> findAll(
            @RequestParam(required = false) Integer idDepartement,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {

        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(PageResponse.ofList(employeService.search(search)));
        }
        if (idDepartement != null) {
            return ResponseEntity.ok(PageResponse.ofList(employeService.findByDepartement(idDepartement)));
        }
        return ResponseEntity.ok(employeService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(employeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<EmployeResponse> create(@Valid @RequestBody EmployeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody EmployeRequest request) {
        return ResponseEntity.ok(employeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EmployeResponse> delete(@PathVariable Integer id) {
        return ResponseEntity.ok(employeService.desactiver(id));
    }

    @PatchMapping("/{id}/desactiver")
    public ResponseEntity<EmployeResponse> desactiver(@PathVariable Integer id) {
        return ResponseEntity.ok(employeService.desactiver(id));
    }

    /** Régénère le mot de passe et tente l'envoi par email (sinon MDP dans la réponse). */
    @PostMapping("/{id}/renvoyer-identifiants")
    public ResponseEntity<EmployeResponse> renvoyerIdentifiants(@PathVariable Integer id) {
        return ResponseEntity.ok(employeService.regenererIdentifiants(id));
    }
}
