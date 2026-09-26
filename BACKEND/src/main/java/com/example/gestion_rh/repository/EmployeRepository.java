package com.example.gestion_rh.repository;

import com.example.gestion_rh.model.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Integer> {

    Optional<Employe> findByEmailEmploye(String emailEmploye);

    boolean existsByEmailEmploye(String emailEmploye);

    List<Employe> findByDepartement_IdDepartement(Integer idDepartement);

    List<Employe> findByStatutEmploye(Employe.StatutEmploye statut);

    @Query("SELECT e FROM Employe e WHERE LOWER(e.nomEmploye) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(e.prenomEmploye) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Employe> searchByNomOrPrenom(@Param("search") String search);

    @Query("SELECT e FROM Employe e WHERE e.departement.idDepartement = :deptId AND e.statutEmploye = :statut")
    List<Employe> findByDepartementAndStatut(@Param("deptId") Integer deptId,
                                             @Param("statut") Employe.StatutEmploye statut);
}