package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.application.dto.user.DestinataireRequestDTO;
import com.smartlogi.sdms.application.dto.user.DestinataireResponseDTO;
import com.smartlogi.sdms.application.mapper.UserMapper;
import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.domain.model.entity.users.ClientExpediteur;
import com.smartlogi.sdms.domain.model.entity.users.Destinataire;
import com.smartlogi.sdms.domain.repository.ClientExpediteurRepository;
import com.smartlogi.sdms.domain.repository.DestinataireRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DestinataireService {

    private final DestinataireRepository destinataireRepository;
    private final ClientExpediteurRepository clientExpediteurRepository;
    private final UserMapper userMapper;

    // --- CREATE ---
    @Transactional
    public DestinataireResponseDTO createDestinataire(DestinataireRequestDTO dto) {
        // 1. Récupérer l'utilisateur connecté (Expéditeur)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        ClientExpediteur expediteur = (ClientExpediteur) clientExpediteurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Expéditeur non trouvé."));

        // 2. Mapper et Lier
        Destinataire destinataire = userMapper.toDestinataireEntity(dto);
        destinataire.setClientExpediteur(expediteur);

        // 3. Sauvegarder
        Destinataire saved = destinataireRepository.save(destinataire);
        return userMapper.toResponseDTO(saved);
    }

    // --- READ (Pageable) ---
    public Page<DestinataireResponseDTO> getAllDestinataires(Pageable pageable) {
        return destinataireRepository.findAll(pageable).map(userMapper::toResponseDTO);
    }

    public DestinataireResponseDTO getDestinataireById(String id) {
        Destinataire dest = destinataireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destinataire", "id", id));
        return userMapper.toResponseDTO(dest);
    }

    // --- UPDATE ---
    @Transactional
    public DestinataireResponseDTO updateDestinataire(String id, DestinataireRequestDTO dto) {
        Destinataire existing = destinataireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destinataire", "id", id));

        // Mise à jour des champs (Seulement ceux autorisés)
        if (dto.getNom() != null) existing.setLastName(dto.getNom());
        if (dto.getPrenom() != null) existing.setFirstName(dto.getPrenom());
        if (dto.getTelephone() != null) existing.setTelephone(dto.getTelephone());
        if (dto.getAdresse() != null) existing.setAdresse(dto.getAdresse());
        if (dto.getEmail() != null) existing.setEmail(dto.getEmail());

        Destinataire updated = destinataireRepository.save(existing);
        return userMapper.toResponseDTO(updated);
    }

    // --- DELETE (Standard) ---
    // Note: Pour un "Soft Delete", il faudrait ajouter un champ 'deleted' (boolean) dans l'entité BaseUser
    @Transactional
    public void deleteDestinataire(String id) {
        if (!destinataireRepository.existsById(id)) {
            throw new ResourceNotFoundException("Destinataire", "id", id);
        }
        destinataireRepository.deleteById(id);
    }

    // --- Méthode existante ---
    @Transactional(readOnly = true)
    public Page<DestinataireResponseDTO> getDestinatairesByClient(String clientId, Pageable pageable) {
        if (!clientExpediteurRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("ClientExpediteur", "id", clientId);
        }
        return destinataireRepository.findAllByClientExpediteurId(clientId, pageable).map(userMapper::toResponseDTO);
    }

    @Transactional
    public Destinataire findOrCreateDestinataire(DestinataireRequestDTO dto, ClientExpediteur expediteur) {
        // 1. n9lbo 3lih b email (ila kan deja kayn nrdoh)
        return destinataireRepository.findByEmail(dto.getEmail())
                .orElseGet(() -> {
                    // 2. ila makanch, n-creer wahd jdid
                    Destinataire newDest = userMapper.toDestinataireEntity(dto);
                    newDest.setClientExpediteur(expediteur); // Liaison m3a l'expediteur
                    return destinataireRepository.save(newDest);
                });
    }
}