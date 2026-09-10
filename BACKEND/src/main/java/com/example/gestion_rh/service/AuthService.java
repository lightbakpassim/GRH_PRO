package com.example.gestion_rh.service;

import com.example.gestion_rh.dto.request.ChangePasswordRequest;
import com.example.gestion_rh.dto.request.LoginRequest;
import com.example.gestion_rh.dto.request.UpdateProfilRequest;
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

        if (utilisateur.getRole() != Utilisateur.Role.SuperAdmin
                && utilisateur.getEntreprise() == null) {
            throw new BadCredentialsException("Compte non rattaché à une entreprise — contactez le support");
        }

        if (utilisateur.getRole() != Utilisateur.Role.SuperAdmin
                && utilisateur.getEntreprise() != null
                && utilisateur.getEntreprise().getStatut() == com.example.gestion_rh.model.Entreprise.StatutEntreprise.Suspendu) {
            throw new BadCredentialsException("Espace entreprise suspendu — contactez le support GRH_PRO");
        }

        utilisateur.setDerniereConnexion(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);

        return toAuthResponse(utilisateur);
    }

    @Transactional(readOnly = true)
    public AuthResponse me() {
        Utilisateur courant = SecurityUtils.currentUser();
        Utilisateur managed = utilisateurRepository.findByLoginWithEmploye(courant.getLogin())
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable"));
        return toAuthResponse(managed);
    }

    @Transactional
    public AuthResponse updateProfil(UpdateProfilRequest request) {
        Utilisateur courant = SecurityUtils.currentUser();
        Utilisateur managed = utilisateurRepository.findById(courant.getIdUtilisateur())
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable"));

        if (!passwordEncoder.matches(request.getMotDePasseActuel(), managed.getMotDePasse())) {
            throw new BusinessException("Mot de passe actuel incorrect");
        }

        boolean loginChange = false;
        boolean mdpChange = false;

        if (request.getLogin() != null && !request.getLogin().isBlank()) {
            String nouveauLogin = request.getLogin().trim().toLowerCase();
            if (!nouveauLogin.equalsIgnoreCase(managed.getLogin())) {
                if (utilisateurRepository.existsByLogin(nouveauLogin)) {
                    throw new BusinessException("Ce login est déjà utilisé");
                }
                if (!nouveauLogin.contains("@")) {
                    throw new BusinessException("Le login doit être une adresse email valide");
                }
                String ancien = managed.getLogin();
                managed.setLogin(nouveauLogin);
                loginChange = true;
                historiqueActionService.enregistrer(
                        "CHANGEMENT_LOGIN",
                        "Login modifié : " + ancien + " → " + nouveauLogin,
                        nouveauLogin);
            }
        }

        if (request.getNouveauMotDePasse() != null && !request.getNouveauMotDePasse().isBlank()) {
            if (passwordEncoder.matches(request.getNouveauMotDePasse(), managed.getMotDePasse())) {
                throw new BusinessException("Le nouveau mot de passe doit être différent de l'ancien");
            }
            managed.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
            mdpChange = true;
            historiqueActionService.enregistrer(
                    "CHANGEMENT_MDP",
                    "Mot de passe modifié",
                    managed.getLogin());
        }

        if (!loginChange && !mdpChange) {
            throw new BusinessException("Aucune modification à enregistrer");
        }

        utilisateurRepository.save(managed);
        return toAuthResponse(managed);
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

    private AuthResponse toAuthResponse(Utilisateur utilisateur) {
        String token = jwtService.generateToken(utilisateur);
        String nomComplet = utilisateur.getEmploye() != null
                ? utilisateur.getEmploye().getPrenomEmploye() + " " + utilisateur.getEmploye().getNomEmploye()
                : utilisateur.getLogin();

        return AuthResponse.builder()
                .token(token)
                .login(utilisateur.getLogin())
                .role(utilisateur.getRole())
                .idEmploye(utilisateur.getEmploye() != null ? utilisateur.getEmploye().getIdEmploye() : null)
                .idEntreprise(utilisateur.getEntreprise() != null
                        ? utilisateur.getEntreprise().getIdEntreprise() : null)
                .nomEntreprise(utilisateur.getEntreprise() != null
                        ? utilisateur.getEntreprise().getNomEntreprise() : null)
                .nomComplet(nomComplet)
                .build();
    }
}
