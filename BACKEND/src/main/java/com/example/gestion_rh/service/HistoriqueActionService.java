package com.example.gestion_rh.service;

import com.example.gestion_rh.model.HistoriqueAction;
import com.example.gestion_rh.repository.HistoriqueActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HistoriqueActionService {

    private final HistoriqueActionRepository historiqueActionRepository;

    @Transactional
    public void enregistrer(String action, String detail, String acteurLogin) {
        historiqueActionRepository.save(HistoriqueAction.builder()
                .action(action)
                .detail(detail)
                .acteurLogin(acteurLogin)
                .dateAction(LocalDateTime.now())
                .build());
    }
}
