package com.example.gestion_rh.service;


import com.example.gestion_rh.dto.request.SuiviTempsRequest;
import com.example.gestion_rh.dto.response.PageResponse;
import com.example.gestion_rh.dto.response.SuiviTempsResponse;
import com.example.gestion_rh.exception.BusinessException;
import com.example.gestion_rh.exception.ResourceNotFoundException;
import com.example.gestion_rh.model.Employe;
import com.example.gestion_rh.model.SuiviTemps;
import com.example.gestion_rh.model.Utilisateur;
import com.example.gestion_rh.repository.SuiviTempsRepository;
import com.example.gestion_rh.repository.UtilisateurRepository;
import com.example.gestion_rh.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SuiviTempsService {

    private final SuiviTempsRepository suiviTempsRepository;
    private final EmployeService employeService;
    private final UtilisateurRepository utilisateurRepository;

    public PageResponse<SuiviTempsResponse> findAll(Pageable pageable) {
        return PageResponse.from(suiviTempsRepository.findAll(pageable).map(this::toResponse));
    }

    public List<SuiviTempsResponse> findAll() {
        return suiviTempsRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<SuiviTempsResponse> findByEmploye(Integer idEmploye) {
        return suiviTempsRepository.findByEmploye_IdEmploye(idEmploye)
                .stream().map(this::toResponse).toList();
    }

    public List<SuiviTempsResponse> findByEmployeAndMois(Integer idEmploye, int mois, int annee) {
        return suiviTempsRepository.findByEmployeAndMoisAnnee(idEmploye, mois, annee)
                .stream().map(this::toResponse).toList();
    }

    public List<SuiviTempsResponse> findEnAttente() {
        return suiviTempsRepository.findByStatutSupp(SuiviTemps.StatutSupp.En_attente)
                .stream().map(this::toResponse).toList();
    }

    public SuiviTempsResponse create(SuiviTempsRequest request, Utilisateur connecte) {
        if (request.getHeuresFin().isBefore(request.getHeuresDebut())) {
            throw new BusinessException("L'heure de fin doit être après l'heure de début");
        }

        Integer idEmploye = SecurityUtils.resolveTargetEmployeId(connecte, request.getIdEmploye());
        Employe employe = employeService.getOrThrow(idEmploye);
        Utilisateur utilisateur = utilisateurRepository.getReferenceById(connecte.getIdUtilisateur());

        SuiviTemps suivi = SuiviTemps.builder()
                .employe(employe)
                .utilisateur(utilisateur)
                .dateTravail(request.getDateTravail())
                .heuresDebut(request.getHeuresDebut())
                .heuresFin(request.getHeuresFin())
                // Le trigger BDD calcule ces valeurs, on met 0 comme placeholder
                .heuresTravaillees(BigDecimal.ZERO)
                .heuresSupplementaires(BigDecimal.ZERO)
                .statutSupp(request.getStatutSupp())
                .build();

        return toResponse(suiviTempsRepository.save(suivi));
    }

    public SuiviTempsResponse approuver(Integer id) {
        return changerStatut(id, SuiviTemps.StatutSupp.Approuvées);
    }

    public SuiviTempsResponse refuser(Integer id) {
        return changerStatut(id, SuiviTemps.StatutSupp.Refusées);
    }

    private SuiviTempsResponse changerStatut(Integer id, SuiviTemps.StatutSupp statut) {
        SuiviTemps suivi = getOrThrow(id);
        suivi.setStatutSupp(statut);
        suivi.setDateApprobation(LocalDateTime.now());
        return toResponse(suiviTempsRepository.save(suivi));
    }

    public void delete(Integer id) {
        suiviTempsRepository.delete(getOrThrow(id));
    }

    private SuiviTemps getOrThrow(Integer id) {
        return suiviTempsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suivi temps introuvable avec l'id : " + id));
    }

    public SuiviTempsResponse toResponse(SuiviTemps s) {
        String nomComplet = s.getEmploye().getPrenomEmploye() + " " + s.getEmploye().getNomEmploye();
        return SuiviTempsResponse.builder()
                .idSuivi(s.getIdSuivi())
                .idEmploye(s.getEmploye().getIdEmploye())
                .nomCompletEmploye(nomComplet)
                .dateTravail(s.getDateTravail())
                .heuresDebut(s.getHeuresDebut())
                .heuresFin(s.getHeuresFin())
                .heuresTravaillees(s.getHeuresTravaillees())
                .heuresSupplementaires(s.getHeuresSupplementaires())
                .statutSupp(s.getStatutSupp())
                .dateApprobation(s.getDateApprobation())
                .build();
    }
}
