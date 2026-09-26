package com.example.gestion_rh.service;


import com.example.gestion_rh.dto.request.UtilisateurRequest;
import com.example.gestion_rh.dto.response.UtilisateurResponse;
import com.example.gestion_rh.exception.BusinessException;
import com.example.gestion_rh.exception.ResourceNotFoundException;
import com.example.gestion_rh.model.Employe;
import com.example.gestion_rh.model.Utilisateur;
import com.example.gestion_rh.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final EmployeService employeService;
    private final PasswordEncoder passwordEncoder;

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
        if (utilisateurRepository.existsByEmploye_IdEmploye(request.getIdEmploye())) {
            throw new BusinessException("Cet employé possède déjà un compte utilisateur");
        }

        Employe employe = employeService.getOrThrow(request.getIdEmploye());

        Utilisateur utilisateur = Utilisateur.builder()
                .login(request.getLogin())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .statutUtilisateur(request.getStatutUtilisateur())
                .role(request.getRole())
                .employe(employe)
                .build();

        return toResponse(utilisateurRepository.save(utilisateur));
    }

    public UtilisateurResponse update(Integer id, UtilisateurRequest request) {
        Utilisateur utilisateur = getOrThrow(id);

        if (!utilisateur.getLogin().equals(request.getLogin())
                && utilisateurRepository.existsByLogin(request.getLogin())) {
            throw new BusinessException("Ce login est déjà utilisé");
        }

        utilisateur.setLogin(request.getLogin());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
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