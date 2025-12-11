package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.application.dto.user.DestinataireResponseDTO;
import com.smartlogi.sdms.application.mapper.UserMapper;
import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.domain.model.entity.users.Destinataire;
import com.smartlogi.sdms.domain.repository.ClientExpediteurRepository;
import com.smartlogi.sdms.domain.repository.DestinataireRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DestinataireService {

    private final DestinataireRepository destinataireRepository;
    private final ClientExpediteurRepository clientExpediteurRepository;
    private final BaseUserService baseUserService;
    private final UserMapper userMapper;

    /**
     * Récupère une page de destinataires associés à un client expéditeur spécifique.
     *
     * @param clientId L'ID du ClientExpediteur
     * @param pageable Les informations de pagination
     * @return Une page de DestinataireResponseDTO
     * @throws ResourceNotFoundException si le ClientExpediteur n'est pas trouvé
     */

    @Transactional
    public Page<DestinataireResponseDTO> getDestinatairesByClient(String clientId, Pageable pageable) {
        log.info("Recherche des destinataires pour le client ID: {}", clientId);

        // 1. Vérifier que le client existe (Guard Clause)
        if (!clientExpediteurRepository.existsById(clientId)) {
            log.warn("ClientExpediteur non trouvé with ID: {}", clientId);
            throw new ResourceNotFoundException("ClientExpediteur", "id", clientId);
        }

        // 2. Récupérer la page d'entités
        // CORRECTION : Suppresion de .orElseThrow() car Page<> n'est pas un Optional
        Page<Destinataire> destinatairesPage = destinataireRepository.findAllByClientExpediteurId(clientId, pageable);

        log.info("{} destinataires trouvés pour le client ID: {}", destinatairesPage.getTotalElements(), clientId);

        // 3. Mapper la page d'entités vers une page de DTOs
        return destinatairesPage.map(userMapper::toResponseDTO);
    }
}