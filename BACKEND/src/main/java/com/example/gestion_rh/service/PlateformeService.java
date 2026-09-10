package com.example.gestion_rh.service;

import com.example.gestion_rh.dto.request.EntrepriseCreateRequest;
import com.example.gestion_rh.dto.request.EntrepriseSuspensionRequest;
import com.example.gestion_rh.dto.response.EntrepriseResponse;
import com.example.gestion_rh.dto.response.EntrepriseSanteResponse;
import com.example.gestion_rh.dto.response.PlateformeDashboardResponse;
import com.example.gestion_rh.exception.BusinessException;
import com.example.gestion_rh.exception.ResourceNotFoundException;
import com.example.gestion_rh.model.Departement;
import com.example.gestion_rh.model.DemandeConge;
import com.example.gestion_rh.model.Entreprise;
import com.example.gestion_rh.model.Paiement;
import com.example.gestion_rh.model.SuiviTemps;
import com.example.gestion_rh.model.Utilisateur;
import com.example.gestion_rh.repository.DepartementRepository;
import com.example.gestion_rh.repository.EntrepriseRepository;
import com.example.gestion_rh.repository.UtilisateurRepository;
import com.example.gestion_rh.util.PasswordGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlateformeService {

    private final EntrepriseRepository entrepriseRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DepartementRepository departementRepository;
    private final PasswordEncoder passwordEncoder;
    private final HistoriqueActionService historiqueActionService;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public PlateformeDashboardResponse dashboard() {
        List<Entreprise> all = entrepriseRepository.findAll();
        List<EntrepriseSanteResponse> santes = all.stream()
                .map(this::toSante)
                .sorted(Comparator
                        .comparing((EntrepriseSanteResponse s) -> s.getStatut() == Entreprise.StatutEntreprise.Actif ? 0 : 1)
                        .thenComparing(EntrepriseSanteResponse::getNomEntreprise, String.CASE_INSENSITIVE_ORDER))
                .toList();

        long actives = santes.stream().filter(s -> s.getStatut() == Entreprise.StatutEntreprise.Actif).count();
        long totalEmployes = santes.stream().mapToLong(EntrepriseSanteResponse::getNbEmployes).sum();
        long totalPointages7j = santes.stream().mapToLong(EntrepriseSanteResponse::getPointages7j).sum();
        long totalConnectes7j = santes.stream().mapToLong(EntrepriseSanteResponse::getNbEmployesConnectes7j).sum();
        long totalBacklog = santes.stream()
                .mapToLong(s -> s.getHeuresSuppEnAttente() + s.getCongesEnAttente() + s.getPaiementsEnAttente())
                .sum();

        LocalDate debut7 = LocalDate.now().minusDays(6);
        List<PlateformeDashboardResponse.SeriePoint> pointagesSerie = seriePointages(debut7);
        List<PlateformeDashboardResponse.SeriePoint> activitesSerie = serieActivites(debut7);
        List<PlateformeDashboardResponse.SeriePoint> congesSerie = serieConges(debut7);

        return PlateformeDashboardResponse.builder()
                .totalEntreprises(all.size())
                .entreprisesActives(actives)
                .entreprisesSuspendues(all.size() - actives)
                .totalUtilisateurs(utilisateurRepository.countByRoleNot(Utilisateur.Role.SuperAdmin))
                .totalEmployes(totalEmployes)
                .totalPointages7j(totalPointages7j)
                .totalConnectes7j(totalConnectes7j)
                .totalBacklog(totalBacklog)
                .entreprises(santes)
                .pointages7j(pointagesSerie)
                .activites7j(activitesSerie)
                .conges7j(congesSerie)
                .genereA(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                .build();
    }

    private List<PlateformeDashboardResponse.SeriePoint> seriePointages(LocalDate debut) {
        List<SuiviTemps> suivis = entityManager.createQuery(
                        "SELECT s FROM SuiviTemps s WHERE s.dateTravail >= :d", SuiviTemps.class)
                .setParameter("d", debut)
                .getResultList();
        Map<LocalDate, Long> counts = suivis.stream()
                .collect(Collectors.groupingBy(SuiviTemps::getDateTravail, Collectors.counting()));
        return fillSerie(debut, counts);
    }

    private List<PlateformeDashboardResponse.SeriePoint> serieActivites(LocalDate debut) {
        LocalDateTime depuis = debut.atStartOfDay();
        List<LocalDateTime> dates = entityManager.createQuery(
                        """
                        SELECT h.dateAction FROM HistoriqueAction h
                        WHERE h.dateAction >= :d AND h.entreprise IS NOT NULL
                        """, LocalDateTime.class)
                .setParameter("d", depuis)
                .getResultList();
        Map<LocalDate, Long> counts = new HashMap<>();
        for (LocalDateTime dt : dates) {
            counts.merge(dt.toLocalDate(), 1L, Long::sum);
        }
        return fillSerie(debut, counts);
    }

    private List<PlateformeDashboardResponse.SeriePoint> serieConges(LocalDate debut) {
        LocalDateTime depuis = debut.atStartOfDay();
        List<LocalDateTime> dates = entityManager.createQuery(
                        "SELECT d.dateDemande FROM DemandeConge d WHERE d.dateDemande >= :d",
                        LocalDateTime.class)
                .setParameter("d", depuis)
                .getResultList();
        Map<LocalDate, Long> counts = new HashMap<>();
        for (LocalDateTime dt : dates) {
            if (dt != null) counts.merge(dt.toLocalDate(), 1L, Long::sum);
        }
        return fillSerie(debut, counts);
    }

    private List<PlateformeDashboardResponse.SeriePoint> fillSerie(LocalDate debut, Map<LocalDate, Long> counts) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        List<PlateformeDashboardResponse.SeriePoint> out = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            LocalDate d = debut.plusDays(i);
            out.add(PlateformeDashboardResponse.SeriePoint.builder()
                    .label(d.format(fmt))
                    .valeur(counts.getOrDefault(d, 0L))
                    .build());
        }
        return out;
    }

    public List<EntrepriseResponse> lister() {
        return entrepriseRepository.findAll().stream()
                .map(e -> toResponse(e, null, null, null))
                .toList();
    }

    public EntrepriseResponse getById(Integer id) {
        Entreprise e = entrepriseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entreprise introuvable"));
        return toResponse(e, null, null, null);
    }

    @Transactional
    public EntrepriseResponse creer(EntrepriseCreateRequest request) {
        if (entrepriseRepository.existsByNomEntrepriseIgnoreCase(request.getNomEntreprise().trim())) {
            throw new BusinessException("Une entreprise porte déjà ce nom");
        }
        String dgLogin = request.getDgLogin().trim().toLowerCase();
        if (utilisateurRepository.existsByLogin(dgLogin)) {
            throw new BusinessException("Ce login DG est déjà utilisé");
        }

        Entreprise entreprise = Entreprise.builder()
                .nomEntreprise(request.getNomEntreprise().trim())
                .emailContact(request.getEmailContact() != null ? request.getEmailContact().trim() : null)
                .telephone(request.getTelephone())
                .statut(Entreprise.StatutEntreprise.Actif)
                .dateCreation(LocalDateTime.now())
                .build();
        entreprise = entrepriseRepository.save(entreprise);

        // Département initial propre à l'entreprise
        departementRepository.save(Departement.builder()
                .nomDepartement("Direction")
                .entreprise(entreprise)
                .build());

        String mdpDg = (request.getDgMotDePasse() != null && !request.getDgMotDePasse().isBlank())
                ? request.getDgMotDePasse()
                : PasswordGenerator.generate(12);

        utilisateurRepository.save(Utilisateur.builder()
                .login(dgLogin)
                .motDePasse(passwordEncoder.encode(mdpDg))
                .role(Utilisateur.Role.DG)
                .statutUtilisateur(Utilisateur.StatutUtilisateur.Actif)
                .entreprise(entreprise)
                .build());

        // Compte RH (Admin) par défaut — MDP unique modifiable ensuite
        String rhLogin = buildRhLogin(entreprise, dgLogin);
        if (utilisateurRepository.existsByLogin(rhLogin)) {
            rhLogin = "rh." + entreprise.getIdEntreprise() + "@grh.pro";
        }
        String mdpRh = PasswordGenerator.generate(12);
        utilisateurRepository.save(Utilisateur.builder()
                .login(rhLogin)
                .motDePasse(passwordEncoder.encode(mdpRh))
                .role(Utilisateur.Role.Admin)
                .statutUtilisateur(Utilisateur.StatutUtilisateur.Actif)
                .entreprise(entreprise)
                .build());

        historiqueActionService.enregistrer(
                "ENTREPRISE_CREEE",
                "Entreprise « " + entreprise.getNomEntreprise()
                        + " » — DG " + dgLogin + " — RH " + rhLogin,
                "plateforme",
                null);

        historiqueActionService.enregistrer(
                "COMPTE_RH_CREE",
                "Compte RH initial créé : " + rhLogin,
                "plateforme",
                entreprise);

        return toResponse(entreprise, mdpDg, rhLogin, mdpRh);
    }

    @Transactional
    public EntrepriseResponse suspendre(Integer id, EntrepriseSuspensionRequest request) {
        Entreprise e = entrepriseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entreprise introuvable"));
        if (e.getStatut() == Entreprise.StatutEntreprise.Suspendu) {
            throw new BusinessException("Cette entreprise est déjà suspendue");
        }
        e.setStatut(Entreprise.StatutEntreprise.Suspendu);
        e.setDateSuspension(LocalDateTime.now());
        e.setMotifSuspension(request != null ? request.getMotif() : null);
        entrepriseRepository.save(e);

        // Accès bloqué via statut entreprise (AuthService + JWT).
        // On ne touche pas au statut individuel → conserve les Inactif manuels.

        historiqueActionService.enregistrer(
                "ENTREPRISE_SUSPENDUE",
                "Entreprise « " + e.getNomEntreprise() + " » — "
                        + (e.getMotifSuspension() != null ? e.getMotifSuspension() : "sans motif"),
                "plateforme",
                null);

        return toResponse(e, null, null, null);
    }

    @Transactional
    public EntrepriseResponse reactiver(Integer id) {
        Entreprise e = entrepriseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entreprise introuvable"));
        if (e.getStatut() == Entreprise.StatutEntreprise.Actif) {
            throw new BusinessException("Cette entreprise est déjà active");
        }
        e.setStatut(Entreprise.StatutEntreprise.Actif);
        e.setDateSuspension(null);
        e.setMotifSuspension(null);
        entrepriseRepository.save(e);

        // Ne réactive pas en masse les Employe Inactif (volontaires ou non).

        historiqueActionService.enregistrer(
                "ENTREPRISE_REACTIVEE",
                "Entreprise « " + e.getNomEntreprise() + " » réactivée",
                "plateforme",
                null);

        return toResponse(e, null, null, null);
    }

    @Transactional
    public void supprimer(Integer id) {
        Entreprise e = entrepriseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entreprise introuvable"));
        String nom = e.getNomEntreprise();
        // Évite qu'Hibernate réécrit l'entité après DELETE SQL natif
        entityManager.detach(e);

        entityManager.createNativeQuery(
                "DELETE FROM historique_action WHERE id_entreprise = :id"
        ).setParameter("id", id).executeUpdate();

        entityManager.createNativeQuery(
                "DELETE FROM rapport WHERE id_entreprise = :id"
        ).setParameter("id", id).executeUpdate();
        entityManager.createNativeQuery("""
                DELETE r FROM rapport r
                INNER JOIN utilisateur u ON r.destinataire_id = u.id_utilisateur
                WHERE u.id_entreprise = :id
                """).setParameter("id", id).executeUpdate();

        entityManager.createNativeQuery("""
                DELETE n FROM notification n
                INNER JOIN employe emp ON n.id_employe = emp.id_employe
                WHERE emp.id_entreprise = :id
                """).setParameter("id", id).executeUpdate();
        entityManager.createNativeQuery("""
                DELETE n FROM notification n
                INNER JOIN paiement p ON n.id_paiement = p.id_paiement
                INNER JOIN employe emp ON p.id_employe = emp.id_employe
                WHERE emp.id_entreprise = :id
                """).setParameter("id", id).executeUpdate();

        entityManager.createNativeQuery("""
                DELETE a FROM absence a
                INNER JOIN employe emp ON a.id_employe = emp.id_employe
                WHERE emp.id_entreprise = :id
                """).setParameter("id", id).executeUpdate();
        entityManager.createNativeQuery("""
                DELETE a FROM absence a
                INNER JOIN utilisateur u ON a.id_utilisateur = u.id_utilisateur
                WHERE u.id_entreprise = :id
                """).setParameter("id", id).executeUpdate();

        entityManager.createNativeQuery("""
                DELETE d FROM demande_conge d
                INNER JOIN employe emp ON d.id_employe = emp.id_employe
                WHERE emp.id_entreprise = :id
                """).setParameter("id", id).executeUpdate();
        entityManager.createNativeQuery("""
                DELETE d FROM demande_conge d
                INNER JOIN utilisateur u ON d.id_utilisateur = u.id_utilisateur
                WHERE u.id_entreprise = :id
                """).setParameter("id", id).executeUpdate();

        entityManager.createNativeQuery("""
                DELETE s FROM suivi_temps s
                INNER JOIN employe emp ON s.id_employe = emp.id_employe
                WHERE emp.id_entreprise = :id
                """).setParameter("id", id).executeUpdate();
        entityManager.createNativeQuery("""
                DELETE s FROM suivi_temps s
                INNER JOIN utilisateur u ON s.id_utilisateur = u.id_utilisateur
                WHERE u.id_entreprise = :id
                """).setParameter("id", id).executeUpdate();

        entityManager.createNativeQuery("""
                DELETE p FROM paiement p
                INNER JOIN employe emp ON p.id_employe = emp.id_employe
                WHERE emp.id_entreprise = :id
                """).setParameter("id", id).executeUpdate();

        entityManager.createNativeQuery(
                "UPDATE utilisateur SET id_employe = NULL WHERE id_entreprise = :id"
        ).setParameter("id", id).executeUpdate();
        entityManager.createNativeQuery(
                "DELETE FROM utilisateur WHERE id_entreprise = :id"
        ).setParameter("id", id).executeUpdate();
        entityManager.createNativeQuery(
                "DELETE FROM employe WHERE id_entreprise = :id"
        ).setParameter("id", id).executeUpdate();
        entityManager.createNativeQuery(
                "DELETE FROM departement WHERE id_entreprise = :id"
        ).setParameter("id", id).executeUpdate();
        entityManager.createNativeQuery(
                "DELETE FROM entreprise WHERE id_entreprise = :id"
        ).setParameter("id", id).executeUpdate();

        historiqueActionService.enregistrer(
                "ENTREPRISE_SUPPRIMEE",
                "Entreprise « " + nom + " » et toutes ses données ont été supprimées",
                "plateforme",
                null);
    }

    private EntrepriseSanteResponse toSante(Entreprise e) {
        List<Utilisateur> users = utilisateurRepository.findByEntrepriseIdWithEmploye(e.getIdEntreprise());
        Utilisateur dg = users.stream()
                .filter(u -> u.getRole() == Utilisateur.Role.DG)
                .findFirst()
                .orElse(null);

        long nbEmployes = users.stream().filter(u -> u.getRole() == Utilisateur.Role.Employe).count();
        long nbAdmins = users.stream().filter(u -> u.getRole() == Utilisateur.Role.Admin).count();
        long actifs = users.stream()
                .filter(u -> u.getStatutUtilisateur() == Utilisateur.StatutUtilisateur.Actif)
                .count();

        LocalDateTime seuil7j = LocalDateTime.now().minusDays(7);
        long employesConnectes7j = users.stream()
                .filter(u -> u.getRole() == Utilisateur.Role.Employe)
                .filter(u -> u.getDerniereConnexion() != null && u.getDerniereConnexion().isAfter(seuil7j))
                .count();

        LocalDateTime derniereActivite = users.stream()
                .map(Utilisateur::getDerniereConnexion)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        List<Integer> employeIds = users.stream()
                .filter(u -> u.getEmploye() != null)
                .map(u -> u.getEmploye().getIdEmploye())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        LocalDate depuis = LocalDate.now().minusDays(7);
        long pointages7j = countPointages(employeIds, depuis);
        long hsEnAttente = countHsEnAttente(employeIds);
        long congesEnAttente = countCongesEnAttente(employeIds);
        long paiementsEnAttente = countPaiementsEnAttente(employeIds);

        int score = calculerScoreFlux(
                e.getStatut(),
                nbEmployes,
                employesConnectes7j,
                pointages7j,
                hsEnAttente + congesEnAttente + paiementsEnAttente,
                derniereActivite);

        return EntrepriseSanteResponse.builder()
                .idEntreprise(e.getIdEntreprise())
                .nomEntreprise(e.getNomEntreprise())
                .statut(e.getStatut())
                .dgLogin(dg != null ? dg.getLogin() : null)
                .dgDerniereConnexion(dg != null ? dg.getDerniereConnexion() : null)
                .derniereActivite(derniereActivite)
                .nbEmployes(nbEmployes)
                .nbEmployesConnectes7j(employesConnectes7j)
                .nbAdmins(nbAdmins)
                .nbUtilisateursActifs(actifs)
                .pointages7j(pointages7j)
                .heuresSuppEnAttente(hsEnAttente)
                .congesEnAttente(congesEnAttente)
                .paiementsEnAttente(paiementsEnAttente)
                .scoreFlux(score)
                .qualiteFlux(labelQualite(score, e.getStatut()))
                .build();
    }

    private long countPointages(List<Integer> employeIds, LocalDate depuis) {
        if (employeIds.isEmpty()) return 0;
        Long n = entityManager.createQuery(
                        "SELECT COUNT(s) FROM SuiviTemps s WHERE s.employe.idEmploye IN :ids AND s.dateTravail >= :d",
                        Long.class)
                .setParameter("ids", employeIds)
                .setParameter("d", depuis)
                .getSingleResult();
        return n != null ? n : 0;
    }

    private long countHsEnAttente(List<Integer> employeIds) {
        if (employeIds.isEmpty()) return 0;
        Long n = entityManager.createQuery(
                        "SELECT COUNT(s) FROM SuiviTemps s WHERE s.employe.idEmploye IN :ids AND s.statutSupp = :st",
                        Long.class)
                .setParameter("ids", employeIds)
                .setParameter("st", SuiviTemps.StatutSupp.En_attente)
                .getSingleResult();
        return n != null ? n : 0;
    }

    private long countCongesEnAttente(List<Integer> employeIds) {
        if (employeIds.isEmpty()) return 0;
        Long n = entityManager.createQuery(
                        "SELECT COUNT(d) FROM DemandeConge d WHERE d.employe.idEmploye IN :ids AND d.statutConge = :st",
                        Long.class)
                .setParameter("ids", employeIds)
                .setParameter("st", DemandeConge.StatutConge.En_attente)
                .getSingleResult();
        return n != null ? n : 0;
    }

    private long countPaiementsEnAttente(List<Integer> employeIds) {
        if (employeIds.isEmpty()) return 0;
        Long n = entityManager.createQuery(
                        "SELECT COUNT(p) FROM Paiement p WHERE p.employe.idEmploye IN :ids AND p.statut = :st",
                        Long.class)
                .setParameter("ids", employeIds)
                .setParameter("st", Paiement.StatutPaiement.En_attente)
                .getSingleResult();
        return n != null ? n : 0;
    }

    private int calculerScoreFlux(Entreprise.StatutEntreprise statut,
                                  long nbEmployes,
                                  long employesConnectes7j,
                                  long pointages7j,
                                  long backlog,
                                  LocalDateTime derniereActivite) {
        if (statut == Entreprise.StatutEntreprise.Suspendu) {
            return 0;
        }
        if (nbEmployes == 0) {
            return derniereActivite != null && derniereActivite.isAfter(LocalDateTime.now().minusDays(14))
                    ? 45 : 25;
        }

        double tauxConnexion = (double) employesConnectes7j / nbEmployes;
        int score = (int) Math.round(tauxConnexion * 55);

        if (pointages7j > 0) {
            score += Math.min(25, 8 + (int) Math.min(pointages7j, 20));
        }

        // Backlog trop élevé pénalise
        if (backlog == 0) score += 15;
        else if (backlog <= 3) score += 8;
        else if (backlog <= 10) score += 3;
        else score -= Math.min(20, (int) (backlog / 2));

        if (derniereActivite == null || derniereActivite.isBefore(LocalDateTime.now().minusDays(14))) {
            score -= 15;
        }

        return Math.max(0, Math.min(100, score));
    }

    private String labelQualite(int score, Entreprise.StatutEntreprise statut) {
        if (statut == Entreprise.StatutEntreprise.Suspendu) return "Critique";
        if (score >= 80) return "Excellent";
        if (score >= 60) return "Bon";
        if (score >= 40) return "Moyen";
        if (score >= 20) return "Faible";
        return "Critique";
    }

    private String buildRhLogin(Entreprise entreprise, String dgLogin) {
        String domain = null;
        if (entreprise.getEmailContact() != null && entreprise.getEmailContact().contains("@")) {
            domain = entreprise.getEmailContact().substring(entreprise.getEmailContact().indexOf('@') + 1);
        } else if (dgLogin != null && dgLogin.contains("@")) {
            domain = dgLogin.substring(dgLogin.indexOf('@') + 1);
        }
        if (domain == null || domain.isBlank()) {
            domain = "grh.pro";
        }
        return ("rh@" + domain).toLowerCase();
    }

    private EntrepriseResponse toResponse(Entreprise e, String mdpDg, String rhLogin, String mdpRh) {
        List<Utilisateur> users = utilisateurRepository.findByEntreprise_IdEntreprise(e.getIdEntreprise());
        Utilisateur dg = users.stream()
                .filter(u -> u.getRole() == Utilisateur.Role.DG)
                .findFirst()
                .orElse(null);
        Utilisateur rh = users.stream()
                .filter(u -> u.getRole() == Utilisateur.Role.Admin)
                .findFirst()
                .orElse(null);

        long nbEmployes = users.stream().filter(u -> u.getRole() == Utilisateur.Role.Employe).count();
        long nbAdmins = users.stream().filter(u -> u.getRole() == Utilisateur.Role.Admin).count();
        long actifs = users.stream()
                .filter(u -> u.getStatutUtilisateur() == Utilisateur.StatutUtilisateur.Actif)
                .count();

        return EntrepriseResponse.builder()
                .idEntreprise(e.getIdEntreprise())
                .nomEntreprise(e.getNomEntreprise())
                .emailContact(e.getEmailContact())
                .telephone(e.getTelephone())
                .statut(e.getStatut())
                .dateCreation(e.getDateCreation())
                .dateSuspension(e.getDateSuspension())
                .motifSuspension(e.getMotifSuspension())
                .dgLogin(dg != null ? dg.getLogin() : null)
                .dgDerniereConnexion(dg != null ? dg.getDerniereConnexion() : null)
                .nbEmployes(nbEmployes)
                .nbAdmins(nbAdmins)
                .nbUtilisateursActifs(actifs)
                .dgMotDePasseTemporaire(mdpDg)
                .rhLogin(rhLogin != null ? rhLogin : (rh != null ? rh.getLogin() : null))
                .rhMotDePasseTemporaire(mdpRh)
                .build();
    }
}
