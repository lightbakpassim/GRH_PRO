package com.example.gestion_rh.controller;

import com.example.gestion_rh.dto.request.EntrepriseCreateRequest;
import com.example.gestion_rh.dto.request.EntrepriseSuspensionRequest;
import com.example.gestion_rh.dto.response.EntrepriseResponse;
import com.example.gestion_rh.dto.response.PlateformeDashboardResponse;
import com.example.gestion_rh.service.PlateformeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plateforme")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SuperAdmin')")
public class PlateformeController {

    private final PlateformeService plateformeService;

    @GetMapping("/dashboard")
    public ResponseEntity<PlateformeDashboardResponse> dashboard() {
        return ResponseEntity.ok(plateformeService.dashboard());
    }

    @GetMapping("/entreprises")
    public ResponseEntity<List<EntrepriseResponse>> lister() {
        return ResponseEntity.ok(plateformeService.lister());
    }

    @GetMapping("/entreprises/{id}")
    public ResponseEntity<EntrepriseResponse> get(@PathVariable Integer id) {
        return ResponseEntity.ok(plateformeService.getById(id));
    }

    @PostMapping("/entreprises")
    public ResponseEntity<EntrepriseResponse> creer(@Valid @RequestBody EntrepriseCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(plateformeService.creer(request));
    }

    @PatchMapping("/entreprises/{id}/suspendre")
    public ResponseEntity<EntrepriseResponse> suspendre(
            @PathVariable Integer id,
            @RequestBody(required = false) EntrepriseSuspensionRequest request) {
        return ResponseEntity.ok(plateformeService.suspendre(id, request != null ? request : new EntrepriseSuspensionRequest()));
    }

    @PatchMapping("/entreprises/{id}/reactiver")
    public ResponseEntity<EntrepriseResponse> reactiver(@PathVariable Integer id) {
        return ResponseEntity.ok(plateformeService.reactiver(id));
    }

    @DeleteMapping("/entreprises/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Integer id) {
        plateformeService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
