package com.smartlogi.sdms.application.service;


import com.smartlogi.sdms.application.dto.mission.MissionRequestDTO;
import com.smartlogi.sdms.application.dto.mission.MissionResponseDTO;
import com.smartlogi.sdms.application.mapper.ColisMapper;
import com.smartlogi.sdms.application.mapper.MissionMapper;
import com.smartlogi.sdms.application.service.email.EmailExpediteurEvent;
import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.domain.model.entity.Colis;
import com.smartlogi.sdms.domain.model.entity.Mission;
import com.smartlogi.sdms.domain.model.entity.users.Livreur;
import com.smartlogi.sdms.domain.model.enums.StatutMission;
import com.smartlogi.sdms.domain.repository.ColisRepository;
import com.smartlogi.sdms.domain.repository.LivreurRepository;
import com.smartlogi.sdms.domain.repository.MissionRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final LivreurRepository livreurRepository;
    private final ColisRepository colisRepository;
    private final MissionMapper missionMapper;
    private final ColisMapper colisMapper;
    private final EmailExpediteurEvent emailExpediteurEvent;

    @Transactional
    public MissionResponseDTO createMission(MissionRequestDTO dto) throws MessagingException {

        Livreur livreur = livreurRepository
                .findById(dto.getLivreurId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("le livreur avec id" + dto.getLivreurId() + "est introuvable")));
        Colis colis = colisRepository
                .findById(dto.getColisId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("colis avec l'id" + dto.getColisId() + "est introuvable")));

        Mission mission = missionMapper.toEntity(dto);
        mission.setLivreur(livreur);
        mission.setColis(colis);
        mission.setStatut(StatutMission.AFFECTEE);
        Mission missionSaved = missionRepository.save(mission);

       emailExpediteurEvent.notifyCollecte(livreur , missionSaved);
        return missionMapper.toResponseDto(missionSaved);
    }
}
