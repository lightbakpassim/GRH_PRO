package com.example.gestion_rh.service;

import com.example.gestion_rh.dto.request.ChangePasswordRequest;
import com.example.gestion_rh.dto.request.LoginRequest;
import com.example.gestion_rh.dto.response.AuthResponse;
import com.example.gestion_rh.exception.BusinessException;
import com.example.gestion_rh.model.Utilisateur;
import com.example.gestion_rh.repository.UtilisateurRepository;
import com.example.gestion_rh.security.JwtService;
import com.example.gestion_rh.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;
    private final HistoriqueActionService historiqueActionService;

    public AuthResponse login(LoginRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByLoginWithEmploye(request.getLogin())
                .orElseThrow(() -> new BadCredentialsException("Login ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.getMotDePasse(), utilisateur.getMotDePasse())) {
            throw new BadCredentialsException("Login ou mot de passe incorrect");
        }

        if (utilisateur.getStatutUtilisateur() != Utilisateur.StatutUtilisateur.Actif) {
            throw new BadCredentialsException("Compte inactif — contactez le service RH");
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

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Utilisateur utilisateur = SecurityUtils.currentUser();
        Utilisateur managed = utilisateurRepository.findById(utilisateur.getIdUtilisateur())
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable"));

        if (!passwordEncoder.matches(request.getMotDePasseActuel(), managed.getMotDePasse())) {
            throw new BusinessException("Mot de passe actuel incorrect");
        }
        if (passwordEncoder.matches(request.getNouveauMotDePasse(), managed.getMotDePasse())) {
            throw new BusinessException("Le nouveau mot de passe doit être différent de l'ancien");
        }

        managed.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
        utilisateurRepository.save(managed);

        historiqueActionService.enregistrer(
                "CHANGEMENT_MDP",
                "Mot de passe modifié",
                managed.getLogin());
    }
}
