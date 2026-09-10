package com.example.gestion_rh.repository;

import com.example.gestion_rh.model.Rapport;
import com.example.gestion_rh.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RapportRepository extends JpaRepository<Rapport, Integer> {

    List<Rapport> findByDestinataireOrderByDateGenerationDesc(Utilisateur destinataire);

    List<Rapport> findByEntreprise_IdEntrepriseOrderByDateGenerationDesc(Integer idEntreprise);

    long countByDestinataireAndLuFalse(Utilisateur destinataire);

    long countByEntreprise_IdEntrepriseAndLuFalse(Integer idEntreprise);

    boolean existsByEntreprise_IdEntrepriseAndPeriodeDebutAndPeriodeFinAndTypeRapport(
            Integer idEntreprise,
            LocalDate periodeDebut,
            LocalDate periodeFin,
            Rapport.TypeRapport typeRapport);
}
