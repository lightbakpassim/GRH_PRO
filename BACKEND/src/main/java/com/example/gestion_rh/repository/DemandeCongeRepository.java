package com.example.gestion_rh.repository;


import com.example.gestion_rh.model.DemandeConge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DemandeCongeRepository extends JpaRepository<DemandeConge, Integer> {

    List<DemandeConge> findByEmploye_IdEmploye(Integer idEmploye);

    List<DemandeConge> findByStatutConge(DemandeConge.StatutConge statut);

    List<DemandeConge> findByEmploye_IdEmployeAndStatutConge(
            Integer idEmploye, DemandeConge.StatutConge statut);

    // Vérifie s'il existe un congé qui chevauche les dates demandées
    @Query("SELECT COUNT(d) > 0 FROM DemandeConge d " +
            "WHERE d.employe.idEmploye = :id " +
            "AND d.statutConge != 'Refusée' " +
            "AND d.dateDebut <= :fin AND d.dateFin >= :debut")
    boolean existsChevauchement(@Param("id") Integer idEmploye,
                                @Param("debut") LocalDate debut,
                                @Param("fin") LocalDate fin);

    @Query("SELECT SUM(d.nbJours) FROM DemandeConge d " +
            "WHERE d.employe.idEmploye = :id AND d.statutConge = 'Approuvée' " +
            "AND YEAR(d.dateDebut) = :annee")
    Integer totalJoursApprouvesPourAnnee(@Param("id") Integer idEmploye,
                                         @Param("annee") int annee);
}