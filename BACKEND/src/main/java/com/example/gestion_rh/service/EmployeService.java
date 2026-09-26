package com.example.gestion_rh.service;


import com.example.gestion_rh.dto.request.EmployeRequest;
import com.example.gestion_rh.dto.response.EmployeResponse;
import com.example.gestion_rh.exception.BusinessException;
import com.example.gestion_rh.exception.ResourceNotFoundException;
import com.example.gestion_rh.model.Departement;
import com.example.gestion_rh.model.Employe;
import com.example.gestion_rh.repository.DepartementRepository;
import com.example.gestion_rh.repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;

    public List<EmployeResponse> findAll() {
        return employeRepository.findAll().stream().map(this::toResponse).toList();
    }

    public EmployeResponse findById(Integer id) {
        return toResponse(getOrThrow(id));
    }

    public List<EmployeResponse> findByDepartement(Integer idDepartement) {
        return employeRepository.findByDepartement_IdDepartement(idDepartement)
                .stream().map(this::toResponse).toList();
    }

    public List<EmployeResponse> search(String query) {
        return employeRepository.searchByNomOrPrenom(query)
                .stream().map(this::toResponse).toList();
    }

    public EmployeResponse create(EmployeRequest request) {
        if (employeRepository.existsByEmailEmploye(request.getEmailEmploye())) {
            throw new BusinessException("Un employé avec cet email existe déjà");
        }
        Departement dept = departementRepository.findById(request.getIdDepartement())
                .orElseThrow(() -> new ResourceNotFoundException("Département introuvable"));

        Employe employe = Employe.builder()
                .nomEmploye(request.getNomEmploye())
                .prenomEmploye(request.getPrenomEmploye())
                .emailEmploye(request.getEmailEmploye())
                .telephone(request.getTelephone())
                .sexe(request.getSexe())
                .etatCivil(request.getEtatCivil())
                .dateEmbauche(request.getDateEmbauche())
                .poste(request.getPoste())
                .salaireBase(request.getSalaireBase())
                .statutEmploye(request.getStatutEmploye())
                .departement(dept)
                .build();

        return toResponse(employeRepository.save(employe));
    }

    public EmployeResponse update(Integer id, EmployeRequest request) {
        Employe employe = getOrThrow(id);

        // Vérification email si changé
        if (!employe.getEmailEmploye().equals(request.getEmailEmploye())
                && employeRepository.existsByEmailEmploye(request.getEmailEmploye())) {
            throw new BusinessException("Cet email est déjà utilisé par un autre employé");
        }

        Departement dept = departementRepository.findById(request.getIdDepartement())
                .orElseThrow(() -> new ResourceNotFoundException("Département introuvable"));

        employe.setNomEmploye(request.getNomEmploye());
        employe.setPrenomEmploye(request.getPrenomEmploye());
        employe.setEmailEmploye(request.getEmailEmploye());
        employe.setTelephone(request.getTelephone());
        employe.setSexe(request.getSexe());
        employe.setEtatCivil(request.getEtatCivil());
        employe.setDateEmbauche(request.getDateEmbauche());
        employe.setPoste(request.getPoste());
        employe.setSalaireBase(request.getSalaireBase());
        employe.setStatutEmploye(request.getStatutEmploye());
        employe.setDepartement(dept);

        return toResponse(employeRepository.save(employe));
    }

    public void delete(Integer id) {
        employeRepository.delete(getOrThrow(id));
    }

    public Employe getOrThrow(Integer id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable avec l'id : " + id));
    }

    public EmployeResponse toResponse(Employe e) {
        return EmployeResponse.builder()
                .idEmploye(e.getIdEmploye())
                .nomEmploye(e.getNomEmploye())
                .prenomEmploye(e.getPrenomEmploye())
                .emailEmploye(e.getEmailEmploye())
                .telephone(e.getTelephone())
                .sexe(e.getSexe())
                .etatCivil(e.getEtatCivil())
                .dateEmbauche(e.getDateEmbauche())
                .poste(e.getPoste())
                .salaireBase(e.getSalaireBase())
                .statutEmploye(e.getStatutEmploye())
                .idDepartement(e.getDepartement() != null ? e.getDepartement().getIdDepartement() : null)
                .nomDepartement(e.getDepartement() != null ? e.getDepartement().getNomDepartement() : null)
                .build();
    }
}