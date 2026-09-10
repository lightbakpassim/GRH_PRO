package com.example.gestion_rh.repository;

import com.example.gestion_rh.model.HistoriqueAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueActionRepository extends JpaRepository<HistoriqueAction, Integer> {

    List<HistoriqueAction> findAllByOrderByDateActionDesc();
}
