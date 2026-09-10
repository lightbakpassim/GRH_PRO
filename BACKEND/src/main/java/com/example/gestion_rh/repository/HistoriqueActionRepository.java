package com.example.gestion_rh.repository;

import com.example.gestion_rh.model.HistoriqueAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoriqueActionRepository extends JpaRepository<HistoriqueAction, Integer> {

    List<HistoriqueAction> findAllByOrderByDateActionDesc();

    List<HistoriqueAction> findByEntreprise_IdEntrepriseOrderByDateActionDesc(Integer idEntreprise);

    List<HistoriqueAction> findByEntreprise_IdEntrepriseAndDateActionBetweenOrderByDateActionAsc(
            Integer idEntreprise, LocalDateTime debut, LocalDateTime fin);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM HistoriqueAction h WHERE h.entreprise.idEntreprise = :id")
    int deleteByEntrepriseId(@Param("id") Integer idEntreprise);
}
