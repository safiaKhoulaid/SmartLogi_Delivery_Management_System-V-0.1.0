package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.application.dto.livreur.LivreurResponseDTO;
import com.smartlogi.sdms.application.mapper.LivreurMapper;
import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.domain.model.entity.users.Livreur;
import com.smartlogi.sdms.domain.model.enums.StatusLivreur;
import com.smartlogi.sdms.domain.repository.LivreurRepository;
import com.smartlogi.sdms.domain.repository.ZoneRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional

public class LivreurService {

    private final LivreurRepository livreurRepository;
    private final ZoneRepository zoneRepository;
    private final LivreurMapper livreurMapper;

    public LivreurService(LivreurRepository livreurRepository, ZoneRepository zoneRepository, LivreurMapper livreurMapper) {
        this.livreurRepository = livreurRepository;
        this.livreurMapper = livreurMapper;
        this.zoneRepository = zoneRepository;
    }


    public List<Livreur> findAllLivreurs() {
        return livreurRepository.findAll();
    }

    public ResponseEntity<List<LivreurResponseDTO>> findAllDisponibleLivreurByZoneAssigneeId(String zoneId) {

        if (!zoneRepository.existsById(zoneId)) {
            throw new ResourceNotFoundException("La zone logistique avec l'ID '" + zoneId + "' est introuvable. Veuillez vérifier l'ID.");
        }
        List<Livreur> livreurs =
                livreurRepository.findAllByZoneAssigneeIdAndStatusLivreur(zoneId, StatusLivreur.DISPONIBLE);
        List<LivreurResponseDTO> livreursDto =  livreurMapper.toResponseDTO(livreurs);

        return ResponseEntity.ok(livreursDto);
    }


}
