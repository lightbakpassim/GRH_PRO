package com.example.gestion_rh.controller;

import com.example.gestion_rh.dto.response.HistoriqueActionResponse;
import com.example.gestion_rh.dto.response.RapportResponse;
import com.example.gestion_rh.model.Rapport;
import com.example.gestion_rh.model.Utilisateur;
import com.example.gestion_rh.security.SecurityUtils;
import com.example.gestion_rh.service.RapportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rapports")
@RequiredArgsConstructor
public class RapportController {

    private final RapportService rapportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DG','Admin')")
    public ResponseEntity<List<RapportResponse>> lister() {
        Utilisateur u = SecurityUtils.currentUser();
        return ResponseEntity.ok(rapportService.listerPourUtilisateur(u));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('DG','Admin')")
    public ResponseEntity<byte[]> telecharger(@PathVariable Integer id) {
        Utilisateur u = SecurityUtils.currentUser();
        Rapport rapport = rapportService.getPdf(id, u);
        rapportService.marquerLu(id, u);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + rapport.getNomFichier() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(rapport.getFichierPdf());
    }

    @PostMapping("/generer-hebdo")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<RapportResponse> genererMaintenant() {
        Rapport r = rapportService.genererPourEntrepriseCourante();
        return ResponseEntity.ok(RapportResponse.builder()
                .idRapport(r.getIdRapport())
                .titre(r.getTitre())
                .typeRapport(r.getTypeRapport().name())
                .periodeDebut(r.getPeriodeDebut())
                .periodeFin(r.getPeriodeFin())
                .dateGeneration(r.getDateGeneration())
                .nomFichier(r.getNomFichier())
                .lu(r.getLu())
                .build());
    }

    @GetMapping("/historique")
    @PreAuthorize("hasAnyRole('DG','Admin')")
    public ResponseEntity<List<HistoriqueActionResponse>> historique() {
        return ResponseEntity.ok(rapportService.historique());
    }

    @DeleteMapping("/historique")
    @PreAuthorize("hasRole('DG')")
    public ResponseEntity<Map<String, Object>> viderHistorique() {
        int n = rapportService.viderHistorique();
        return ResponseEntity.ok(Map.of("supprimes", n));
    }

    @GetMapping("/statut")
    @PreAuthorize("hasAnyRole('DG','Admin')")
    public ResponseEntity<Map<String, Object>> statut() {
        return ResponseEntity.ok(rapportService.statutGeneration());
    }
}
