package com.example.gestion_rh.scheduler;

import com.example.gestion_rh.service.RapportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RapportHebdomadaireScheduler {

    private final RapportService rapportService;

    /** Chaque vendredi à 22:00 GMT. */
    @Scheduled(cron = "0 0 22 * * FRI", zone = "GMT")
    public void genererVendrediSoir() {
        try {
            var rapport = rapportService.genererEtLivrerRapportHebdomadaire();
            log.info("Rapport hebdo généré automatiquement (vendredi 22h GMT) id={} fichier={}",
                    rapport.getIdRapport(), rapport.getNomFichier());
        } catch (Exception e) {
            log.error("Échec génération rapport hebdomadaire automatique : {}", e.getMessage(), e);
        }
    }
}
