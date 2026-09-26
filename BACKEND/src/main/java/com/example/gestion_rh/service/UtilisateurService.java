package com.example.gestion_rh.service;


import com.example.gestion_rh.dto.request.UtilisateurRequest;
import com.example.gestion_rh.dto.response.UtilisateurResponse;
import com.example.gestion_rh.exception.BusinessException;
import com.example.gestion_rh.exception.ResourceNotFoundException;
import com.example.gestion_rh.model.Employe;
import com.example.gestion_rh.model.Utilisateur;
import com.example.gestion_rh.repository.UtilisateurRepository;
import com.example.gestion_rh.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final EmployeService employeService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public List<UtilisateurResponse> findAll() {
        return utilisateurRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UtilisateurResponse findById(Integer id) {
        return toResponse(getOrThrow(id));
    }

    public UtilisateurResponse create(UtilisateurRequest request) {
        if (utilisateurRepository.existsByLogin(request.getLogin())) {
            throw new BusinessException("Ce login est déjà utilisé");
        }

        Employe employe = null;
        if (request.getRole() == Utilisateur.Role.DG) {
            if (request.getIdEmploye() != null) {
                throw new BusinessException("Un compte DG ne doit pas être lié à un employé");
            }
        } else {
            if (request.getIdEmploye() == null) {
                throw new BusinessException("L'id de l'employé est obligatoire pour ce rôle");
            }
            if (utilisateurRepository.existsByEmploye_IdEmploye(request.getIdEmploye())) {
                throw new BusinessException("Cet employé possède déjà un compte utilisateur");
            }
            employe = employeService.getOrThrow(request.getIdEmploye());
        }

        String motDePasseClair = (request.getMotDePasse() != null && !request.getMotDePasse().isBlank())
                ? request.getMotDePasse()
                : PasswordGenerator.generate(12);

        String emailDest = resolveEmailDestinataire(request.getLogin(), employe);
        String nomComplet = employe != null
                ? employe.getPrenomEmploye() + " " + employe.getNomEmploye()
                : request.getLogin();

        Utilisateur utilisateur = Utilisateur.builder()
                .login(request.getLogin())
                .motDePasse(passwordEncoder.encode(motDePasseClair))
                .statutUtilisateur(request.getStatutUtilisateur() != null
                        ? request.getStatutUtilisateur()
                        : Utilisateur.StatutUtilisateur.Actif)
                .role(request.getRole())
                .employe(employe)
                .build();

        Utilisateur saved = utilisateurRepository.save(utilisateur);

        boolean emailEnvoye = false;
        String avertissementMail = null;
        try {
            emailService.envoyerIdentifiants(emailDest, nomComplet, saved.getLogin(), motDePasseClair);
            emailEnvoye = true;
        } catch (IllegalStateException e) {
            avertissementMail = e.getMessage();
            log.warn("Identifiants non envoyés pour {} : {}", saved.getLogin(), e.getMessage());
        }

        UtilisateurResponse response = toResponse(saved);
        response.setEmailEnvoye(emailEnvoye);
        response.setMotDePasseTemporaire(emailEnvoye ? null : motDePasseClair);
        response.setAvertissement(avertissementMail);
        return response;
    }

    public UtilisateurResponse update(Integer id, UtilisateurRequest request) {
        Utilisateur utilisateur = getOrThrow(id);

        if (!utilisateur.getLogin().equals(request.getLogin())
                && utilisateurRepository.existsByLogin(request.getLogin())) {
            throw new BusinessException("Ce login est déjà utilisé");
        }

        utilisateur.setLogin(request.getLogin());
        if (request.getMotDePasse() != null && !request.getMotDePasse().isBlank()) {
            utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        }
        utilisateur.setStatutUtilisateur(request.getStatutUtilisateur());
        utilisateur.setRole(request.getRole());

        return toResponse(utilisateurRepository.save(utilisateur));
    }

    public void toggleStatut(Integer id) {
        Utilisateur u = getOrThrow(id);
        u.setStatutUtilisateur(
                u.getStatutUtilisateur() == Utilisateur.StatutUtilisateur.Actif
                        ? Utilisateur.StatutUtilisateur.Inactif
                        : Utilisateur.StatutUtilisateur.Actif
        );
        utilisateurRepository.save(u);
    }

    public void delete(Integer id) {
        utilisateurRepository.delete(getOrThrow(id));
    }

    private String resolveEmailDestinataire(String login, Employe employe) {
        if (employe != null && employe.getEmailEmploye() != null && employe.getEmailEmploye().contains("@")) {
            return employe.getEmailEmploye();
        }
        if (login != null && login.contains("@")) {
            return login;
        }
        throw new BusinessException("Impossible d'envoyer le mot de passe : aucun email valide (login ou employé)");
    }

    private Utilisateur getOrThrow(Integer id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'id : " + id));
    }

    private UtilisateurResponse toResponse(Utilisateur u) {
        String nomComplet = u.getEmploye() != null
                ? u.getEmploye().getPrenomEmploye() + " " + u.getEmploye().getNomEmploye()
                : null;
        return UtilisateurResponse.builder()
                .idUtilisateur(u.getIdUtilisateur())
                .login(u.getLogin())
                .statutUtilisateur(u.getStatutUtilisateur())
                .derniereConnexion(u.getDerniereConnexion())
                .role(u.getRole())
                .idEmploye(u.getEmploye() != null ? u.getEmploye().getIdEmploye() : null)
                .nomCompletEmploye(nomComplet)
                .build();
    }
}
