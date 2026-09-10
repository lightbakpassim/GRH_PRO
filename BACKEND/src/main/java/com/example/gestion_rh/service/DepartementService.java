package com.example.gestion_rh.service;

import com.example.gestion_rh.dto.request.DepartementRequest;
import com.example.gestion_rh.dto.response.DepartementResponse;
import com.example.gestion_rh.exception.BusinessException;
import com.example.gestion_rh.exception.ResourceNotFoundException;
import com.example.gestion_rh.model.Departement;
import com.example.gestion_rh.model.Entreprise;
import com.example.gestion_rh.repository.DepartementRepository;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartementService {

    private final DepartementRepository departementRepository;
    private final EmployeRepository employeRepository;

    public List<DepartementResponse> findAll() {
        Integer idEntreprise = SecurityUtils.requireIdEntreprise();
        return departementRepository.findByEntreprise_IdEntrepriseOrderByNomDepartementAsc(idEntreprise).stream()
                .map(this::toResponse)
                .toList();
    }

    public DepartementResponse findById(Integer id) {
        return toResponse(getOrThrow(id));
    }

    public DepartementResponse create(DepartementRequest request) {
        Integer idEntreprise = SecurityUtils.requireIdEntreprise();
        if (departementRepository.existsByNomDepartementAndEntreprise_IdEntreprise(
                request.getNomDepartement(), idEntreprise)) {
            throw new BusinessException("Un département avec ce nom existe déjà");
        }
        Entreprise entreprise = SecurityUtils.currentEntrepriseOrNull();
        Departement dept = Departement.builder()
                .nomDepartement(request.getNomDepartement())
                .entreprise(entreprise)
                .build();
        return toResponse(departementRepository.save(dept));
    }

    public DepartementResponse update(Integer id, DepartementRequest request) {
        Departement dept = getOrThrow(id);
        dept.setNomDepartement(request.getNomDepartement());
        return toResponse(departementRepository.save(dept));
    }

    public void delete(Integer id) {
        Departement dept = getOrThrow(id);
        List<?> employes = employeRepository.findByDepartement_IdDepartement(id);
        if (!employes.isEmpty()) {
            throw new BusinessException("Impossible de supprimer un département contenant des employés");
        }
        departementRepository.delete(dept);
    }

    private Departement getOrThrow(Integer id) {
        Integer idEntreprise = SecurityUtils.requireIdEntreprise();
        return departementRepository.findByIdDepartementAndEntreprise_IdEntreprise(id, idEntreprise)
                .orElseThrow(() -> new ResourceNotFoundException("Département introuvable avec l'id : " + id));
    }

    private DepartementResponse toResponse(Departement dept) {
        long nbEmployes = employeRepository.findByDepartement_IdDepartement(dept.getIdDepartement()).size();
        return DepartementResponse.builder()
                .idDepartement(dept.getIdDepartement())
                .nomDepartement(dept.getNomDepartement())
                .nombreEmployes(nbEmployes)
                .build();
    }
}
