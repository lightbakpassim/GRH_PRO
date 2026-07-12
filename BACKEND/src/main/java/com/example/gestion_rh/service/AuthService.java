package com.example.gestion_rh.service;


import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.gestion_rh.dto.request.LoginRequest;
import com.example.gestion_rh.dto.response.AuthResponse;
import com.example.gestion_rh.model.Utilisateur;
import com.example.gestion_rh.repository.UtilisateurRepository;
import com.example.gestion_rh.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        System.out.println("=== LOGIN ATTEMPT: " + request.getLogin());

        Utilisateur utilisateur = utilisateurRepository.findByLoginWithEmploye(request.getLogin())
                .orElseThrow(() -> new BadCredentialsException("Login ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.getMotDePasse(), utilisateur.getMotDePasse())) {
            throw new BadCredentialsException("Login ou mot de passe incorrect");
        }

        utilisateur.setDerniereConnexion(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);

        String token = jwtService.generateToken(utilisateur);

        String nomComplet = utilisateur.getEmploye() != null
                ? utilisateur.getEmploye().getPrenomEmploye() + " " + utilisateur.getEmploye().getNomEmploye()
                : utilisateur.getLogin();

        return AuthResponse.builder()
                .token(token)
                .login(utilisateur.getLogin())
                .role(utilisateur.getRole())
                .idEmploye(utilisateur.getEmploye() != null ? utilisateur.getEmploye().getIdEmploye() : null)
                .nomComplet(nomComplet)
                .build();
    }
}