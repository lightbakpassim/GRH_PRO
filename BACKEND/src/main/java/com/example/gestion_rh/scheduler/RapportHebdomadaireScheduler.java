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

    /** Chaque vendredi à 22:00 (Africa/Lome) — un PDF par entreprise, livré à son DG. */
    @Scheduled(cron = "0 0 22 * * FRI", zone = "Africa/Lome")
    public void genererVendrediSoir() {
        try {
            var rapports = rapportService.genererTousLesRapportsHebdomadaires();
            log.info("Rapports hebdo générés automatiquement (vendredi 22h Africa/Lome) : {} entreprise(s)",
                    rapports.size());
        } catch (Exception e) {
            log.error("Échec génération rapports hebdomadaires automatiques : {}", e.getMessage(), e);
        }
    }
}
