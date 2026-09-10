package com.example.gestion_rh.controller;

import com.example.gestion_rh.dto.request.ChangePasswordRequest;
import com.example.gestion_rh.dto.request.LoginRequest;
import com.example.gestion_rh.dto.request.UpdateProfilRequest;
import com.example.gestion_rh.dto.response.AuthResponse;
import com.example.gestion_rh.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthResponse> me() {
        return ResponseEntity.ok(authService.me());
    }

    @PatchMapping("/profil")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthResponse> updateProfil(@Valid @RequestBody UpdateProfilRequest request) {
        return ResponseEntity.ok(authService.updateProfil(request));
    }

    @PatchMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(Map.of("message", "Mot de passe mis à jour"));
    }
}
