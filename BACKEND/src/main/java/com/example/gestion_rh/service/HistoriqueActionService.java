package com.example.gestion_rh.service;

import com.example.gestion_rh.model.Entreprise;
import com.example.gestion_rh.model.HistoriqueAction;
import com.example.gestion_rh.model.Utilisateur;
import com.example.gestion_rh.repository.HistoriqueActionRepository;
import com.example.gestion_rh.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HistoriqueActionService {

    private final HistoriqueActionRepository historiqueActionRepository;

    @Transactional
    public void enregistrer(String action, String detail, String acteurLogin) {
        Entreprise entreprise = null;
        try {
            Utilisateur u = SecurityUtils.currentUser();
            if (u.getRole() != Utilisateur.Role.SuperAdmin) {
                entreprise = u.getEntreprise();
            }
        } catch (AccessDeniedException ignored) {
            // appel technique hors auth (scheduler / seed)
        }
        enregistrer(action, detail, acteurLogin, entreprise);
    }

    @Transactional
    public void enregistrer(String action, String detail, String acteurLogin, Entreprise entreprise) {
        historiqueActionRepository.save(HistoriqueAction.builder()
                .action(action)
                .detail(detail)
                .acteurLogin(acteurLogin)
                .dateAction(LocalDateTime.now())
                .entreprise(entreprise)
                .build());
    }
}
