package com.example.gestion_rh.repository;

import com.example.gestion_rh.model.SuiviTemps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SuiviTempsRepository extends JpaRepository<SuiviTemps, Integer> {

    List<SuiviTemps> findByEmploye_IdEmploye(Integer idEmploye);

    List<SuiviTemps> findByStatutSupp(SuiviTemps.StatutSupp statut);

    List<SuiviTemps> findByEmploye_IdEmployeAndDateTravailBetween(
            Integer idEmploye, LocalDate debut, LocalDate fin);

    @Query("SELECT s FROM SuiviTemps s WHERE s.employe.idEmploye = :id " +
            "AND MONTH(s.dateTravail) = :mois AND YEAR(s.dateTravail) = :annee")
    List<SuiviTemps> findByEmployeAndMoisAnnee(@Param("id") Integer idEmploye,
                                               @Param("mois") int mois,
                                               @Param("annee") int annee);

    List<SuiviTemps> findByStatutSuppOrderByDateTravailDesc(SuiviTemps.StatutSupp statut);
}