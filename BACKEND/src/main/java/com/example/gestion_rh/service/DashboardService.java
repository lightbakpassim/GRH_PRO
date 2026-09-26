package com.example.gestion_rh.service;

import com.example.gestion_rh.dto.response.EntrepriseDashboardResponse;
import com.example.gestion_rh.model.*;
import com.example.gestion_rh.repository.*;
import com.example.gestion_rh.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;
    private final DemandeCongeRepository demandeCongeRepository;
    private final SuiviTempsRepository suiviTempsRepository;
    private final PaiementRepository paiementRepository;
    private final RapportRepository rapportRepository;

    private static final DateTimeFormatter JOUR_FMT = DateTimeFormatter.ofPattern("dd/MM");

    public EntrepriseDashboardResponse vueEntreprise() {
        LocalDate today = LocalDate.now();
        int mois = today.getMonthValue();
        int annee = today.getYear();

        List<Employe> actifs = employeRepository.findByStatutEmploye(Employe.StatutEmploye.Actif);
        List<Employe> inactifs = employeRepository.findByStatutEmploye(Employe.StatutEmploye.Inactif);

        BigDecimal masse = actifs.stream()
                .map(Employe::getSalaireBase)
                .filter(s -> s != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DemandeConge> congesAttente = demandeCongeRepository
                .findByStatutConge(DemandeConge.StatutConge.En_attente);
        List<SuiviTemps> hsAttente = suiviTempsRepository
                .findByStatutSupp(SuiviTemps.StatutSupp.En_attente);
        List<SuiviTemps> pointagesJour = suiviTempsRepository.findByDateTravail(today);

        List<Paiement> paiementsMois = paiementRepository.findByMoisAndAnnee(mois, annee);
        long paiementsEffectues = paiementsMois.stream()
                .filter(p -> p.getStatut() == Paiement.StatutPaiement.Effectué)
                .count();
        long paiementsValides = paiementsMois.stream()
                .filter(p -> p.getStatut() == Paiement.StatutPaiement.Validé)
                .count();
        long paiementsAttente = paiementsMois.stream()
                .filter(p -> p.getStatut() == Paiement.StatutPaiement.En_attente)
                .count();

        long rapportsNonLus = compterRapportsNonLus();

        List<EntrepriseDashboardResponse.DepartementStat> parDept = departementRepository.findAll().stream()
                .map(d -> EntrepriseDashboardResponse.DepartementStat.builder()
                        .idDepartement(d.getIdDepartement())
                        .nomDepartement(d.getNomDepartement())
                        .effectif(employeRepository.findByDepartement_IdDepartement(d.getIdDepartement()).stream()
                                .filter(e -> e.getStatutEmploye() == Employe.StatutEmploye.Actif)
                                .count())
                        .build())
                .sorted(Comparator.comparing(EntrepriseDashboardResponse.DepartementStat::getNomDepartement))
                .toList();

        List<EntrepriseDashboardResponse.ActiviteItem> pointagesRecents = pointagesJour.stream()
                .sorted(Comparator.comparing(SuiviTemps::getHeuresDebut).reversed())
                .limit(8)
                .map(s -> EntrepriseDashboardResponse.ActiviteItem.builder()
                        .label(nom(s.getEmploye()))
                        .detail(s.getHeuresDebut() + " → " + s.getHeuresFin()
                                + " (" + s.getHeuresTravaillees() + " h)")
                        .statut(s.getStatutSupp().toString())
                        .build())
                .toList();

        List<EntrepriseDashboardResponse.ActiviteItem> congesRecents = congesAttente.stream()
                .sorted(Comparator.comparing(DemandeConge::getDateDemande).reversed())
                .limit(8)
                .map(c -> EntrepriseDashboardResponse.ActiviteItem.builder()
                        .label(nom(c.getEmploye()))
                        .detail(c.getDateDebut() + " → " + c.getDateFin()
                                + " (" + c.getNbJours() + " j)")
                        .statut(c.getStatutConge().toString())
                        .build())
                .toList();

        LocalDate debut7 = today.minusDays(6);
        List<SuiviTemps> suivis7j = suiviTempsRepository.findByDateTravailBetween(debut7, today);
        Map<LocalDate, Long> countParJour = suivis7j.stream()
                .collect(Collectors.groupingBy(SuiviTemps::getDateTravail, Collectors.counting()));
        Map<LocalDate, Double> hsParJour = suivis7j.stream()
                .collect(Collectors.groupingBy(
                        SuiviTemps::getDateTravail,
                        Collectors.summingDouble(s -> s.getHeuresSupplementaires() != null
                                ? s.getHeuresSupplementaires().doubleValue() : 0d)
                ));

        List<EntrepriseDashboardResponse.SeriePoint> pointages7j = new ArrayList<>();
        List<EntrepriseDashboardResponse.SeriePoint> heuresSupp7j = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = debut7.plusDays(i);
            String label = d.format(JOUR_FMT);
            pointages7j.add(EntrepriseDashboardResponse.SeriePoint.builder()
                    .label(label)
                    .valeur(countParJour.getOrDefault(d, 0L))
                    .build());
            heuresSupp7j.add(EntrepriseDashboardResponse.SeriePoint.builder()
                    .label(label)
                    .valeur(Math.round(hsParJour.getOrDefault(d, 0d) * 10.0) / 10.0)
                    .build());
        }

        return EntrepriseDashboardResponse.builder()
                .employesActifs(actifs.size())
                .employesInactifs(inactifs.size())
                .totalDepartements(departementRepository.count())
                .congesEnAttente(congesAttente.size())
                .heuresSuppEnAttente(hsAttente.size())
                .pointagesAujourdhui(pointagesJour.size())
                .paiementsMoisEnCours(paiementsEffectues + paiementsValides)
                .paiementsEnAttente(paiementsAttente)
                .paiementsValides(paiementsValides)
                .masseSalarialeActifs(masse)
                .rapportsNonLus(rapportsNonLus)
                .parDepartement(parDept)
                .pointagesRecents(pointagesRecents)
                .congesRecents(congesRecents)
                .pointages7j(pointages7j)
                .heuresSupp7j(heuresSupp7j)
                .genereA(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                .build();
    }

    private long compterRapportsNonLus() {
        Utilisateur courant = SecurityUtils.currentUser();
        if (courant.getRole() == Utilisateur.Role.DG) {
            return rapportRepository.countByDestinataireAndLuFalse(courant);
        }
        return rapportRepository.countByLuFalse();
    }

    private String nom(Employe e) {
        if (e == null) return "—";
        return e.getPrenomEmploye() + " " + e.getNomEmploye();
    }
}
