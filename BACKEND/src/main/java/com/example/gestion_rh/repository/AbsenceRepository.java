package com.example.gestion_rh.repository;


import com.example.gestion_rh.model.Absence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, Integer> {

    List<Absence> findByEmploye_IdEmploye(Integer idEmploye);

    List<Absence> findByStatutAbsence(Absence.StatutAbsence statut);

    List<Absence> findByTypeAbsence(Absence.TypeAbsence type);

    List<Absence> findByEmploye_IdEmployeAndDateAbsenceBetween(
            Integer idEmploye, LocalDate debut, LocalDate fin);

    @Query("SELECT COUNT(a) FROM Absence a WHERE a.employe.idEmploye = :id " +
            "AND MONTH(a.dateAbsence) = :mois AND YEAR(a.dateAbsence) = :annee")
    long countByEmployeAndMoisAnnee(@Param("id") Integer idEmploye,
                                    @Param("mois") int mois,
                                    @Param("annee") int annee);
}
