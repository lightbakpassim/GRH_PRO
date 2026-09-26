package com.example.gestion_rh.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
@Order(1)
@Slf4j
public class SchemaInitializer implements ApplicationRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        entityManager.createNativeQuery("""
                CREATE TABLE IF NOT EXISTS rapport (
                    id_rapport INT NOT NULL AUTO_INCREMENT,
                    titre VARCHAR(255) NOT NULL,
                    type_rapport VARCHAR(50) NOT NULL,
                    periode_debut DATE NOT NULL,
                    periode_fin DATE NOT NULL,
                    date_generation DATETIME NOT NULL,
                    destinataire_id INT NOT NULL,
                    fichier_pdf LONGBLOB NOT NULL,
                    nom_fichier VARCHAR(255) NOT NULL,
                    lu TINYINT(1) NOT NULL DEFAULT 0,
                    PRIMARY KEY (id_rapport),
                    CONSTRAINT fk_rapport_destinataire
                        FOREIGN KEY (destinataire_id) REFERENCES utilisateur (id_utilisateur)
                )
                """).executeUpdate();

        entityManager.createNativeQuery("""
                CREATE TABLE IF NOT EXISTS historique_action (
                    id_historique INT NOT NULL AUTO_INCREMENT,
                    action VARCHAR(100) NOT NULL,
                    detail TEXT,
                    acteur_login VARCHAR(50),
                    date_action DATETIME NOT NULL,
                    PRIMARY KEY (id_historique)
                )
                """).executeUpdate();

        // Alignement rôle Java ↔ BDD (Employé → Employe, colonne VARCHAR)
        try {
            entityManager.createNativeQuery(
                    "ALTER TABLE utilisateur MODIFY COLUMN role VARCHAR(20) NOT NULL"
            ).executeUpdate();
            entityManager.createNativeQuery(
                    "UPDATE utilisateur SET role = 'Employe' WHERE role NOT IN ('Admin','Employe','DG')"
            ).executeUpdate();
        } catch (Exception e) {
            log.warn("Normalisation colonne role : {}", e.getMessage());
        }

        // Statut paiement : En attente → Effectué → Validé (employé)
        try {
            entityManager.createNativeQuery("""
                    ALTER TABLE paiement MODIFY COLUMN statut
                    ENUM('En attente','Effectué','Validé') NULL
                    """).executeUpdate();
        } catch (Exception e) {
            try {
                entityManager.createNativeQuery(
                        "ALTER TABLE paiement MODIFY COLUMN statut VARCHAR(20) NULL"
                ).executeUpdate();
            } catch (Exception ignored) {
                log.warn("Alignement statut paiement : {}", e.getMessage());
            }
        }

        log.info("Schéma rapport / historique_action / paiement vérifié");
    }
}
