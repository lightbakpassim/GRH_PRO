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

        entityManager.createNativeQuery("""
                CREATE TABLE IF NOT EXISTS entreprise (
                    id_entreprise INT NOT NULL AUTO_INCREMENT,
                    nom_entreprise VARCHAR(150) NOT NULL,
                    email_contact VARCHAR(100),
                    telephone VARCHAR(30),
                    statut VARCHAR(20) NOT NULL,
                    date_creation DATETIME NOT NULL,
                    date_suspension DATETIME,
                    motif_suspension TEXT,
                    PRIMARY KEY (id_entreprise)
                )
                """).executeUpdate();

        ensureColumn("utilisateur", "id_entreprise", "INT NULL");
        ensureForeignKey(
                "utilisateur",
                "fk_utilisateur_entreprise",
                "ALTER TABLE utilisateur ADD CONSTRAINT fk_utilisateur_entreprise "
                        + "FOREIGN KEY (id_entreprise) REFERENCES entreprise (id_entreprise)");

        ensureColumn("historique_action", "id_entreprise", "INT NULL");
        ensureForeignKey(
                "historique_action",
                "fk_historique_entreprise",
                "ALTER TABLE historique_action ADD CONSTRAINT fk_historique_entreprise "
                        + "FOREIGN KEY (id_entreprise) REFERENCES entreprise (id_entreprise)");

        ensureColumn("departement", "id_entreprise", "INT NULL");
        ensureForeignKey(
                "departement",
                "fk_departement_entreprise",
                "ALTER TABLE departement ADD CONSTRAINT fk_departement_entreprise "
                        + "FOREIGN KEY (id_entreprise) REFERENCES entreprise (id_entreprise)");

        ensureColumn("employe", "id_entreprise", "INT NULL");
        ensureForeignKey(
                "employe",
                "fk_employe_entreprise",
                "ALTER TABLE employe ADD CONSTRAINT fk_employe_entreprise "
                        + "FOREIGN KEY (id_entreprise) REFERENCES entreprise (id_entreprise)");

        ensureColumn("rapport", "id_entreprise", "INT NULL");
        ensureForeignKey(
                "rapport",
                "fk_rapport_entreprise",
                "ALTER TABLE rapport ADD CONSTRAINT fk_rapport_entreprise "
                        + "FOREIGN KEY (id_entreprise) REFERENCES entreprise (id_entreprise)");

        // Backfill rapports legacy (destinataire → entreprise)
        try {
            entityManager.createNativeQuery("""
                    UPDATE rapport r
                    INNER JOIN utilisateur u ON r.destinataire_id = u.id_utilisateur
                    SET r.id_entreprise = u.id_entreprise
                    WHERE r.id_entreprise IS NULL AND u.id_entreprise IS NOT NULL
                    """).executeUpdate();
        } catch (Exception e) {
            log.warn("Backfill rapport.id_entreprise : {}", e.getMessage());
        }

        // Alignement rôle Java ↔ BDD (Employé → Employe, colonne VARCHAR)
        try {
            entityManager.createNativeQuery(
                    "ALTER TABLE utilisateur MODIFY COLUMN role VARCHAR(20) NOT NULL"
            ).executeUpdate();
            entityManager.createNativeQuery(
                    "UPDATE utilisateur SET role = 'Employe' WHERE role NOT IN ('Admin','Employe','DG','SuperAdmin')"
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

        log.info("Schéma rapport / historique_action / entreprise / paiement vérifié");
    }

    private void ensureColumn(String table, String column, String definition) {
        Number count = (Number) entityManager.createNativeQuery("""
                SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = :table
                  AND COLUMN_NAME = :column
                """)
                .setParameter("table", table)
                .setParameter("column", column)
                .getSingleResult();
        if (count.longValue() == 0) {
            entityManager.createNativeQuery(
                    "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition
            ).executeUpdate();
        }
    }

    private void ensureForeignKey(String table, String constraintName, String alterSql) {
        Number count = (Number) entityManager.createNativeQuery("""
                SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = :table
                  AND CONSTRAINT_NAME = :cname
                  AND CONSTRAINT_TYPE = 'FOREIGN KEY'
                """)
                .setParameter("table", table)
                .setParameter("cname", constraintName)
                .getSingleResult();
        if (count.longValue() == 0) {
            entityManager.createNativeQuery(alterSql).executeUpdate();
        }
    }
}
