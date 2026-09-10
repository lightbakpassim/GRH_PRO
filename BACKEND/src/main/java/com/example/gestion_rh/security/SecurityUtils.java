package com.example.gestion_rh.security;

import com.example.gestion_rh.exception.BusinessException;
import com.example.gestion_rh.model.Entreprise;
import com.example.gestion_rh.model.Utilisateur;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Utilisateur currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Utilisateur utilisateur)) {
            throw new AccessDeniedException("Non authentifié");
        }
        return utilisateur;
    }

    public static boolean isAdmin(Utilisateur utilisateur) {
        return utilisateur.getRole() == Utilisateur.Role.Admin;
    }

    public static boolean isSuperAdmin(Utilisateur utilisateur) {
        return utilisateur.getRole() == Utilisateur.Role.SuperAdmin;
    }

    /** Entreprise du locataire connecté (null pour SuperAdmin). */
    public static Entreprise currentEntrepriseOrNull() {
        return currentUser().getEntreprise();
    }

    public static Integer requireIdEntreprise() {
        Entreprise e = currentEntrepriseOrNull();
        if (e == null || e.getIdEntreprise() == null) {
            throw new BusinessException("Aucune entreprise associée à ce compte");
        }
        return e.getIdEntreprise();
    }

    public static Integer requireIdEmploye(Utilisateur utilisateur) {
        if (utilisateur.getEmploye() == null || utilisateur.getEmploye().getIdEmploye() == null) {
            throw new BusinessException("Aucun employé lié à cet utilisateur");
        }
        return utilisateur.getEmploye().getIdEmploye();
    }

    public static Integer resolveTargetEmployeId(Utilisateur connecte, Integer requestedIdEmploye) {
        if (isAdmin(connecte)) {
            if (requestedIdEmploye == null) {
                throw new BusinessException("L'identifiant employé est obligatoire");
            }
            return requestedIdEmploye;
        }
        return requireIdEmploye(connecte);
    }

    public static void assertOwnsEmployeResource(Utilisateur connecte, Integer resourceIdEmploye) {
        if (isAdmin(connecte)) {
            return;
        }
        Integer id = requireIdEmploye(connecte);
        if (!id.equals(resourceIdEmploye)) {
            throw new AccessDeniedException("Accès refusé à cette ressource");
        }
    }
}
