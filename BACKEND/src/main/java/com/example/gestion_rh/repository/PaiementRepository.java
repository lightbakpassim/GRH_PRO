package com.example.gestion_rh.repository;


import com.example.gestion_rh.model.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Integer> {

    List<Paiement> findByEmploye_IdEmploye(Integer idEmploye);

    List<Paiement> findByStatut(Paiement.StatutPaiement statut);

    Optional<Paiement> findByEmploye_IdEmployeAndMoisAndAnnee(
            Integer idEmploye, Integer mois, Integer annee);

    boolean existsByEmploye_IdEmployeAndMoisAndAnnee(
            Integer idEmploye, Integer mois, Integer annee);

    @Query("SELECT p FROM Paiement p WHERE p.mois = :mois AND p.annee = :annee")
    List<Paiement> findByMoisAndAnnee(@Param("mois") int mois, @Param("annee") int annee);

    @Query("SELECT p FROM Paiement p WHERE p.employe.idEmploye = :id ORDER BY p.annee DESC, p.mois DESC")
    List<Paiement> findHistoriqueByEmploye(@Param("id") Integer idEmploye);
}
