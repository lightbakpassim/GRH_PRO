package com.example.gestion_rh.config;

import com.example.gestion_rh.model.Departement;
import com.example.gestion_rh.model.Employe;
import com.example.gestion_rh.model.Utilisateur;
import com.example.gestion_rh.repository.DepartementRepository;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.repository.UtilisateurRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Comptes de test (dev) — SEED_TEST_USERS=false en prod.
 * Admin : admin@grh.tg / admin123
 * DG    : dg@grh.tg / dgChangeMe1
 */
@Component
@Profile("!test")
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private static final String LEGACY_TEST_EMPLOYE_LOGIN = "employe@grh.tg";

    private final UtilisateurRepository utilisateurRepository;
    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${app.seed-test-users:true}")
    private boolean seedEnabled;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            return;
        }

        entityManager.createNativeQuery(
                "ALTER TABLE utilisateur MODIFY COLUMN role VARCHAR(20) NOT NULL"
        ).executeUpdate();
        entityManager.createNativeQuery(
                "UPDATE utilisateur SET role = 'Employe' WHERE role NOT IN ('Admin','Employe','DG')"
        ).executeUpdate();

        removeLegacyTestEmploye(LEGACY_TEST_EMPLOYE_LOGIN);
        utilisateurRepository.findByLogin("admin").ifPresent(u -> {
            entityManager.createNativeQuery(
                    "DELETE FROM utilisateur WHERE login = 'admin'"
            ).executeUpdate();
            log.info("Ancien login legacy 'admin' supprimé");
        });

        Departement dept = ensureDepartement("Direction");

        Employe adminEmploye = ensureEmploye(
                "Admin", "Système", "admin@grh.tg", "90000001",
                "Administrateur RH", new BigDecimal("800000.00"), dept);
        ensureUtilisateur("admin@grh.tg", "admin123", Utilisateur.Role.Admin, adminEmploye);

        ensureUtilisateur("dg@grh.tg", "dgChangeMe1", Utilisateur.Role.DG, null);

        log.info("Comptes test — admin@grh.tg/admin123 | dg@grh.tg/dgChangeMe1");
    }

    /** Retire l’ancien compte seed employé s’il existe encore. */
    private void removeLegacyTestEmploye(String login) {
        utilisateurRepository.findByLogin(login).ifPresent(u -> {
            Integer employeId = u.getEmploye() != null ? u.getEmploye().getIdEmploye() : null;
            Integer userId = u.getIdUtilisateur();
            if (employeId != null) {
                deleteEmployeCascade(employeId);
            }
            entityManager.createNativeQuery(
                    "DELETE FROM utilisateur WHERE id_utilisateur = :id"
            ).setParameter("id", userId).executeUpdate();
            log.info("Ancien compte test employé supprimé : {}", login);
        });

        employeRepository.findByEmailEmploye(login).ifPresent(e -> {
            deleteEmployeCascade(e.getIdEmploye());
            log.info("Fiche employé test orpheline supprimée : {}", login);
        });
    }

    private void deleteEmployeCascade(Integer employeId) {
        entityManager.createNativeQuery("DELETE FROM notification WHERE id_employe = :id")
                .setParameter("id", employeId).executeUpdate();
        entityManager.createNativeQuery("""
                DELETE n FROM notification n
                INNER JOIN paiement p ON n.id_paiement = p.id_paiement
                WHERE p.id_employe = :id
                """).setParameter("id", employeId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM absence WHERE id_employe = :id")
                .setParameter("id", employeId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM demande_conge WHERE id_employe = :id")
                .setParameter("id", employeId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM suivi_temps WHERE id_employe = :id")
                .setParameter("id", employeId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM paiement WHERE id_employe = :id")
                .setParameter("id", employeId).executeUpdate();
        entityManager.createNativeQuery(
                "UPDATE utilisateur SET id_employe = NULL WHERE id_employe = :id"
        ).setParameter("id", employeId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM employe WHERE id_employe = :id")
                .setParameter("id", employeId).executeUpdate();
    }

    private Departement ensureDepartement(String nom) {
        return departementRepository.findByNomDepartement(nom)
                .orElseGet(() -> departementRepository.save(
                        Departement.builder().nomDepartement(nom).build()));
    }

    private Employe ensureEmploye(String nom, String prenom, String email, String tel,
                                  String poste, BigDecimal salaire, Departement dept) {
        return employeRepository.findByEmailEmploye(email)
                .map(e -> {
                    e.setNomEmploye(nom);
                    e.setPrenomEmploye(prenom);
                    e.setTelephone(tel);
                    e.setPoste(poste);
                    e.setSalaireBase(salaire);
                    e.setStatutEmploye(Employe.StatutEmploye.Actif);
                    e.setDepartement(dept);
                    return employeRepository.save(e);
                })
                .orElseGet(() -> employeRepository.save(Employe.builder()
                        .nomEmploye(nom)
                        .prenomEmploye(prenom)
                        .emailEmploye(email)
                        .telephone(tel)
                        .sexe(Employe.Sexe.M)
                        .etatCivil(Employe.EtatCivil.Célibataire)
                        .dateEmbauche(LocalDate.of(2024, 1, 15))
                        .poste(poste)
                        .salaireBase(salaire)
                        .statutEmploye(Employe.StatutEmploye.Actif)
                        .departement(dept)
                        .build()));
    }

    private void ensureUtilisateur(String login, String rawPassword, Utilisateur.Role role, Employe employe) {
        utilisateurRepository.findByLogin(login)
                .ifPresentOrElse(u -> {
                    u.setMotDePasse(passwordEncoder.encode(rawPassword));
                    u.setRole(role);
                    u.setStatutUtilisateur(Utilisateur.StatutUtilisateur.Actif);
                    u.setEmploye(employe);
                    utilisateurRepository.save(u);
                }, () -> utilisateurRepository.save(Utilisateur.builder()
                        .login(login)
                        .motDePasse(passwordEncoder.encode(rawPassword))
                        .role(role)
                        .statutUtilisateur(Utilisateur.StatutUtilisateur.Actif)
                        .employe(employe)
                        .build()));
    }
}
