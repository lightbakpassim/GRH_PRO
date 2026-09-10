package com.example.gestion_rh.service;

import com.example.gestion_rh.dto.request.EmployeRequest;
import com.example.gestion_rh.dto.response.EmployeResponse;
import com.example.gestion_rh.dto.response.PageResponse;
import com.example.gestion_rh.exception.BusinessException;
import com.example.gestion_rh.exception.ResourceNotFoundException;
import com.example.gestion_rh.model.Departement;
import com.example.gestion_rh.model.Employe;
import com.example.gestion_rh.model.Entreprise;
import com.example.gestion_rh.model.Utilisateur;
import com.example.gestion_rh.repository.DepartementRepository;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.repository.UtilisateurRepository;
import com.example.gestion_rh.security.SecurityUtils;
import com.example.gestion_rh.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final HistoriqueActionService historiqueActionService;

    public PageResponse<EmployeResponse> findAll(Pageable pageable) {
        Integer idEntreprise = SecurityUtils.requireIdEntreprise();
        return PageResponse.from(
                employeRepository.findByEntreprise_IdEntreprise(idEntreprise, pageable).map(this::toResponse));
    }

    public List<EmployeResponse> findAll() {
        Integer idEntreprise = SecurityUtils.requireIdEntreprise();
        return employeRepository.findByEntreprise_IdEntreprise(idEntreprise).stream()
                .map(this::toResponse).toList();
    }

    public EmployeResponse findById(Integer id) {
        return toResponse(getOrThrow(id));
    }

    public List<EmployeResponse> findByDepartement(Integer idDepartement) {
        Integer idEntreprise = SecurityUtils.requireIdEntreprise();
        return employeRepository.findByDepartement_IdDepartement(idDepartement).stream()
                .filter(e -> e.getEntreprise() != null
                        && idEntreprise.equals(e.getEntreprise().getIdEntreprise()))
                .map(this::toResponse).toList();
    }

    public List<EmployeResponse> search(String query) {
        Integer idEntreprise = SecurityUtils.requireIdEntreprise();
        return employeRepository.searchByEntrepriseAndNom(idEntreprise, query)
                .stream().map(this::toResponse).toList();
    }

    public EmployeResponse create(EmployeRequest request) {
        Integer idEntreprise = SecurityUtils.requireIdEntreprise();
        if (request.getIdDepartement() == null) {
            throw new BusinessException("Le département est obligatoire à la création");
        }
        if (employeRepository.existsByEmailEmploye(request.getEmailEmploye())) {
            throw new BusinessException("Un employé avec cet email existe déjà");
        }
        if (utilisateurRepository.existsByLogin(request.getEmailEmploye())) {
            throw new BusinessException("Un compte utilisateur existe déjà pour cet email");
        }

        Departement dept = departementRepository
                .findByIdDepartementAndEntreprise_IdEntreprise(request.getIdDepartement(), idEntreprise)
                .orElseThrow(() -> new ResourceNotFoundException("Département introuvable"));

        Entreprise entreprise = SecurityUtils.currentEntrepriseOrNull();

        Employe employe = Employe.builder()
                .nomEmploye(request.getNomEmploye())
                .prenomEmploye(request.getPrenomEmploye())
                .emailEmploye(request.getEmailEmploye())
                .telephone(request.getTelephone())
                .sexe(request.getSexe())
                .etatCivil(request.getEtatCivil())
                .dateEmbauche(request.getDateEmbauche())
                .poste(request.getPoste())
                .salaireBase(request.getSalaireBase())
                .statutEmploye(request.getStatutEmploye() != null
                        ? request.getStatutEmploye()
                        : Employe.StatutEmploye.Actif)
                .departement(dept)
                .entreprise(entreprise)
                .build();

        employe = employeRepository.save(employe);

        String motDePasseClair = PasswordGenerator.generate(12);
        Utilisateur acteur = SecurityUtils.currentUser();
        utilisateurRepository.save(Utilisateur.builder()
                .login(request.getEmailEmploye())
                .motDePasse(passwordEncoder.encode(motDePasseClair))
                .role(Utilisateur.Role.Employe)
                .statutUtilisateur(Utilisateur.StatutUtilisateur.Actif)
                .employe(employe)
                .entreprise(acteur != null ? acteur.getEntreprise() : null)
                .build());

        String nomComplet = employe.getPrenomEmploye() + " " + employe.getNomEmploye();
        boolean emailEnvoye = false;
        String avertissementMail = null;
        try {
            emailService.envoyerIdentifiants(
                    employe.getEmailEmploye(),
                    nomComplet,
                    employe.getEmailEmploye(),
                    motDePasseClair);
            emailEnvoye = true;
        } catch (IllegalStateException e) {
            avertissementMail = e.getMessage();
            log.warn("Identifiants non envoyés à {} : {}", employe.getEmailEmploye(), e.getMessage());
        }

        historiqueActionService.enregistrer(
                "CREATION_EMPLOYE",
                "Employé " + nomComplet + " (" + employe.getEmailEmploye()
                        + ") — département " + dept.getNomDepartement()
                        + (emailEnvoye ? " — email envoyé" : " — email non envoyé"),
                safeActeur());

        EmployeResponse response = toResponse(employe);
        response.setCompteCree(true);
        response.setEmailEnvoye(emailEnvoye);
        // Si l'email n'est pas parti, l'admin doit transmettre le MDP manuellement
        response.setMotDePasseTemporaire(emailEnvoye ? null : motDePasseClair);
        response.setAvertissement(avertissementMail);
        return response;
    }

    public EmployeResponse update(Integer id, EmployeRequest request) {
        Employe employe = getOrThrow(id);
        String ancienEmail = employe.getEmailEmploye();

        if (!ancienEmail.equals(request.getEmailEmploye())
                && employeRepository.existsByEmailEmploye(request.getEmailEmploye())) {
            throw new BusinessException("Cet email est déjà utilisé par un autre employé");
        }

        Departement dept = departementRepository
                .findByIdDepartementAndEntreprise_IdEntreprise(
                        request.getIdDepartement(), SecurityUtils.requireIdEntreprise())
                .orElseThrow(() -> new ResourceNotFoundException("Département introuvable"));

        employe.setNomEmploye(request.getNomEmploye());
        employe.setPrenomEmploye(request.getPrenomEmploye());
        employe.setEmailEmploye(request.getEmailEmploye());
        employe.setTelephone(request.getTelephone());
        employe.setSexe(request.getSexe());
        employe.setEtatCivil(request.getEtatCivil());
        employe.setDateEmbauche(request.getDateEmbauche());
        employe.setPoste(request.getPoste());
        employe.setSalaireBase(request.getSalaireBase());
        employe.setStatutEmploye(request.getStatutEmploye());
        employe.setDepartement(dept);

        Employe saved = employeRepository.save(employe);

        // Garder le login aligné sur l'email professionnel
        if (!ancienEmail.equals(request.getEmailEmploye())) {
            utilisateurRepository.findByLogin(ancienEmail).ifPresent(u -> {
                if (utilisateurRepository.existsByLogin(request.getEmailEmploye())) {
                    throw new BusinessException("Impossible de synchroniser le login : email déjà pris");
                }
                u.setLogin(request.getEmailEmploye());
                utilisateurRepository.save(u);
            });
        }

        return toResponse(saved);
    }

    /** Régénère un MDP unique, tente l'email, sinon le renvoie à l'admin. */
    public EmployeResponse regenererIdentifiants(Integer id) {
        Employe employe = getOrThrow(id);
        Utilisateur utilisateur = utilisateurRepository.findByEmploye_IdEmploye(employe.getIdEmploye())
                .or(() -> utilisateurRepository.findByLogin(employe.getEmailEmploye()))
                .orElseThrow(() -> new BusinessException("Aucun compte utilisateur pour cet employé"));

        String motDePasseClair = PasswordGenerator.generate(12);
        utilisateur.setMotDePasse(passwordEncoder.encode(motDePasseClair));
        if (!employe.getEmailEmploye().equals(utilisateur.getLogin())) {
            if (utilisateurRepository.existsByLogin(employe.getEmailEmploye())
                    && !utilisateur.getLogin().equals(employe.getEmailEmploye())) {
                throw new BusinessException("Impossible d'aligner le login sur l'email : déjà utilisé");
            }
            utilisateur.setLogin(employe.getEmailEmploye());
        }
        utilisateur.setStatutUtilisateur(Utilisateur.StatutUtilisateur.Actif);
        utilisateurRepository.save(utilisateur);

        String nomComplet = employe.getPrenomEmploye() + " " + employe.getNomEmploye();
        boolean emailEnvoye = false;
        String avertissementMail = null;
        try {
            emailService.envoyerIdentifiants(
                    employe.getEmailEmploye(),
                    nomComplet,
                    utilisateur.getLogin(),
                    motDePasseClair);
            emailEnvoye = true;
        } catch (IllegalStateException e) {
            avertissementMail = e.getMessage();
            log.warn("Renvoi identifiants échoué pour {} : {}", employe.getEmailEmploye(), e.getMessage());
        }

        historiqueActionService.enregistrer(
                "RESET_MDP_EMPLOYE",
                "Identifiants régénérés pour " + employe.getEmailEmploye()
                        + (emailEnvoye ? " — email envoyé" : " — email non envoyé"),
                safeActeur());

        EmployeResponse response = toResponse(employe);
        response.setCompteCree(true);
        response.setEmailEnvoye(emailEnvoye);
        response.setMotDePasseTemporaire(emailEnvoye ? null : motDePasseClair);
        response.setAvertissement(avertissementMail);
        return response;
    }

    /** Désactivation logique (évite les FK en DELETE dur). */
    public EmployeResponse desactiver(Integer id) {
        Employe employe = getOrThrow(id);
        employe.setStatutEmploye(Employe.StatutEmploye.Inactif);
        employeRepository.save(employe);
        utilisateurRepository.findByEmploye_IdEmploye(employe.getIdEmploye()).ifPresentOrElse(u -> {
            u.setStatutUtilisateur(Utilisateur.StatutUtilisateur.Inactif);
            utilisateurRepository.save(u);
        }, () -> utilisateurRepository.findByLogin(employe.getEmailEmploye()).ifPresent(u -> {
            u.setStatutUtilisateur(Utilisateur.StatutUtilisateur.Inactif);
            utilisateurRepository.save(u);
        }));
        historiqueActionService.enregistrer(
                "DESACTIVATION_EMPLOYE",
                "Employé " + employe.getEmailEmploye() + " désactivé",
                safeActeur());
        return toResponse(employe);
    }

    public void delete(Integer id) {
        desactiver(id);
    }

    private String safeActeur() {
        try {
            return SecurityUtils.currentUser().getLogin();
        } catch (Exception e) {
            return "SYSTEME";
        }
    }

    public Employe getOrThrow(Integer id) {
        Integer idEntreprise = SecurityUtils.requireIdEntreprise();
        return employeRepository.findByIdEmployeAndEntreprise_IdEntreprise(id, idEntreprise)
                .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable avec l'id : " + id));
    }

    public EmployeResponse toResponse(Employe e) {
        return EmployeResponse.builder()
                .idEmploye(e.getIdEmploye())
                .nomEmploye(e.getNomEmploye())
                .prenomEmploye(e.getPrenomEmploye())
                .emailEmploye(e.getEmailEmploye())
                .telephone(e.getTelephone())
                .sexe(e.getSexe())
                .etatCivil(e.getEtatCivil())
                .dateEmbauche(e.getDateEmbauche())
                .poste(e.getPoste())
                .salaireBase(e.getSalaireBase())
                .statutEmploye(e.getStatutEmploye())
                .idDepartement(e.getDepartement() != null ? e.getDepartement().getIdDepartement() : null)
                .nomDepartement(e.getDepartement() != null ? e.getDepartement().getNomDepartement() : null)
                .build();
    }
}
