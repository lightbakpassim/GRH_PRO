package com.example.gestion_rh.repository;


import com.example.gestion_rh.model.Departement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartementRepository extends JpaRepository<Departement, Integer> {
    Optional<Departement> findByNomDepartement(String nomDepartement);
    boolean existsByNomDepartement(String nomDepartement);

    List<Departement> findByEntreprise_IdEntrepriseOrderByNomDepartementAsc(Integer idEntreprise);

    boolean existsByNomDepartementAndEntreprise_IdEntreprise(String nomDepartement, Integer idEntreprise);

    Optional<Departement> findByIdDepartementAndEntreprise_IdEntreprise(Integer id, Integer idEntreprise);
}
