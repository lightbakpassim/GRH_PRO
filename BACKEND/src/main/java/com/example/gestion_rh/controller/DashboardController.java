package com.example.gestion_rh.controller;

import com.example.gestion_rh.dto.response.EntrepriseDashboardResponse;
import com.example.gestion_rh.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /** Vue entreprise temps réel — destinée principalement au DG. */
    @GetMapping("/entreprise")
    @PreAuthorize("hasAnyRole('DG','Admin')")
    public ResponseEntity<EntrepriseDashboardResponse> entreprise() {
        return ResponseEntity.ok(dashboardService.vueEntreprise());
    }
}
