package com.smartlogi.sdms.application.service;

// 1. DTOs
import com.smartlogi.sdms.application.dto.Email.EmailRequest;
import com.smartlogi.sdms.application.dto.colis.ColisRequestDTO;
import com.smartlogi.sdms.application.dto.colis.ColisResponseDTO;
import com.smartlogi.sdms.application.dto.user.DestinataireRequestDTO;

// 2. Mappers & Entities
import com.smartlogi.sdms.application.mapper.ColisMapper;
import com.smartlogi.sdms.domain.model.entity.Colis;
import com.smartlogi.sdms.domain.model.entity.Zone;
import com.smartlogi.sdms.domain.model.entity.users.ClientExpediteur;
import com.smartlogi.sdms.domain.model.entity.users.Destinataire;
import com.smartlogi.sdms.domain.model.entity.users.Livreur;

// 3. Enums
import com.smartlogi.sdms.domain.model.enums.PriorityColis;
import com.smartlogi.sdms.domain.model.enums.StatusColis;

// 4. Repositories
import com.smartlogi.sdms.domain.repository.ClientExpediteurRepository;
import com.smartlogi.sdms.domain.repository.ColisRepository;
import com.smartlogi.sdms.domain.repository.DestinataireRepository;
import com.smartlogi.sdms.domain.repository.LivreurRepository;
import com.smartlogi.sdms.domain.repository.MissionRepository;
import com.smartlogi.sdms.domain.repository.ZoneRepository;

// 5. Exceptions
import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import jakarta.validation.ValidationException;
import jakarta.mail.MessagingException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

// 6. Spring & Lombok
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ColisService {

    private final ColisRepository colisRepository;
    private final DestinataireRepository destinataireRepository;
    private final DestinataireService destinataireService;
    private final ColisMapper colisMapper;
    private final ZoneRepository zoneRepository;
    private final ClientExpediteurRepository clientExpediteurRepository;
    private final MissionRepository missionRepository;
    private final EmailService emailService;
    private final LivreurRepository livreurRepository;

    /**
     * Création d'un colis avec gestion intelligente de l'expéditeur (Auth) et du destinataire.
     */
    @Transactional
    public ColisResponseDTO createColis(ColisRequestDTO dto) throws MessagingException {

        log.info("Début du processus de création de colis...");

        // =================================================================================
        // 1. IDENTIFICATION DE L'EXPÉDITEUR (Gestionnaire vs Client) 🕵️‍♂️
        // =================================================================================
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();

        boolean isGestionnaire = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GESTIONNAIRE") || a.getAuthority().equals("GESTIONNAIRE"));

        ClientExpediteur expediteur;

        if (isGestionnaire) {
            // CAS GESTIONNAIRE : Il crée pour le compte d'un client
            log.info("Utilisateur connecté identifié comme GESTIONNAIRE.");

            if (dto.getExpediteurId() == null) {
                throw new ValidationException("En tant que Gestionnaire, vous devez obligatoirement spécifier l'ID du client expéditeur.");
            }

            expediteur = clientExpediteurRepository.findById(dto.getExpediteurId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client expéditeur introuvable avec l'ID : " + dto.getExpediteurId()));

        } else {
            // CAS CLIENT : Il crée pour lui-même (via son Token)
            log.info("Utilisateur connecté identifié comme CLIENT (via Token).");

            expediteur = clientExpediteurRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Compte Client introuvable pour l'email : " + currentUserEmail));
        }

        log.info("Colis sera créé pour le client : {} {}", expediteur.getFirstName(), expediteur.getLastName());

        // =================================================================================
        // 2. GESTION DU DESTINATAIRE (Délégation au Service) 📦
        // =================================================================================
        Destinataire destinataire;

        if (dto.getDestinataireId() != null) {
            // Cas A : ID fourni
            log.info("Recherche du destinataire existant ID: {}", dto.getDestinataireId());
            destinataire = destinataireRepository.findById(dto.getDestinataireId())
                    .orElseThrow(() -> new ResourceNotFoundException("Destinataire introuvable avec ID: " + dto.getDestinataireId()));

        } else if (dto.getDestinataireInfo() != null) {
            // Cas B : Nouvelles infos fournies -> On délègue la création/recherche
            log.info("Traitement des informations du destinataire via DestinataireService.");
            destinataire = destinataireService.findOrCreateDestinataire(dto.getDestinataireInfo(), expediteur);

        } else {
            throw new ValidationException("Vous devez fournir soit l'ID du destinataire, soit ses informations.");
        }

        // =================================================================================
        // 3. DÉTERMINATION DE LA ZONE LOGISTIQUE 🗺️
        // =================================================================================
        String villeDestination = destinataire.getAdresse().ville();
        String codePostal = destinataire.getAdresse().codePostal();

        log.info("Recherche de zone pour : {} ({})", villeDestination, codePostal);

        Zone zoneDestination = zoneRepository.findByVilleAndCodePostal(villeDestination, codePostal)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune zone logistique configurée pour la ville : " + villeDestination));

        // =================================================================================
        // 4. CRÉATION ET SAUVEGARDE DU COLIS 💾
        // =================================================================================
        Colis colis = colisMapper.toEntity(dto);

        // Relations
        colis.setClientExpediteur(expediteur);
        colis.setDestinataire(destinataire);
        colis.setZoneDestination(zoneDestination);

        // Données métiers
        colis.setVilleDestination(villeDestination);
        colis.setDateCreation(dto.getDateCreation() != null ? dto.getDateCreation() : LocalDateTime.now());
        colis.setStatut(StatusColis.CREE);
        colis.setPriorite(dto.getPriority() != null ? dto.getPriority() : PriorityColis.NORMALE);

        // Génération code de suivi
        colis.generateTrackingCode();

        Colis savedColis = colisRepository.save(colis);
        log.info("✅ Colis créé avec succès. Tracking ID: {}", savedColis.getTrackingCode());

        // =================================================================================
        // 5. NOTIFICATION EMAIL 📧
        // =================================================================================
        try {
            Map<String, Object> variables = Map.of(
                    "name", destinataire.getFirstName(),
                    "expediteurName", expediteur.getFirstName() + " " + expediteur.getLastName(),
                    "trackingCode", colis.getTrackingCode(),
                    "message", "Un nouveau colis vous a été envoyé."
            );

            EmailRequest emailReq = EmailRequest.builder()
                    .to(destinataire.getEmail())
                    .subject("📦 Vous avez un nouveau colis - SmartLogi")
                    .templateName("email-template.html")
                    .variables(variables)
                    .build();

            emailService.sendEmail(emailReq);
        } catch (Exception e) {
            log.warn("⚠️ Le colis est créé mais l'email n'a pas pu être envoyé : {}", e.getMessage());
        }

        return colisMapper.toColisResponseDTO(savedColis);
    }

    /**
     * Récupère tous les colis pour un client spécifique.
     */
    public Page<ColisResponseDTO> getColisByClientExpediteurId(String idClient, Pageable pageable) {
        log.info("Recherche des colis pour le client: {}, Page: {}, Taille: {}", idClient, pageable.getPageNumber(), pageable.getPageSize());

        if (!clientExpediteurRepository.existsById(idClient)) {
            throw new ResourceNotFoundException("Impossible de trouver les colis car le ClientExpediteur avec l'ID " + idClient + " est introuvable.");
        }
        Page<Colis> colisPage = colisRepository.findAllByClientExpediteurId(idClient, pageable);
        return colisPage.map(colisMapper::toColisResponseDTO);
    }

    /**
     * Met à jour les informations d'un colis.
     */
    @Transactional
    public ColisResponseDTO updateColis(String colisId, ColisRequestDTO dto) {
        Colis colis = colisRepository.findById(colisId)
                .orElseThrow(() -> new ResourceNotFoundException("Colis", "id", colisId));

        if (dto.getDescription() != null) colis.setDescription(dto.getDescription());
        if (dto.getPoids() != null) colis.setPoids(dto.getPoids());
        if (dto.getPriority() != null) colis.setPriorite(dto.getPriority());
        if (dto.getStatus() != null) colis.setStatut(dto.getStatus());

        Colis updatedColis = colisRepository.save(colis);
        return colisMapper.toColisResponseDTO(updatedColis);
    }

    /**
     * Supprime un colis et ses missions associées.
     */
    @Transactional
    public void deleteColis(String colisId) {
        if (!colisRepository.existsById(colisId)) {
            throw new ResourceNotFoundException("Colis", "id", colisId);
        }
        // Supprimer les missions liées avant de supprimer le colis
        missionRepository.deleteAllByColisId(colisId);
        colisRepository.deleteById(colisId);
    }

    /**
     * Recherche avancée pour l'admin/gestionnaire.
     */
    @Transactional(readOnly = true)
    public Page<ColisResponseDTO> searchColisAdmin(StatusColis statut, PriorityColis priorite, String ville, String description, String trackingCode, String expediteurId, Pageable pageable) {
        Page<Colis> page = colisRepository.rechercheAvancee(statut, priorite, ville, description, trackingCode, expediteurId, pageable);
        return page.map(colisMapper::toColisResponseDTO);
    }

    public List<ColisResponseDTO> updateAllColisByLivreur(String idLivreur) {
        List<Colis> colisTemp = colisRepository.findAllByLivreurId(idLivreur);
        for (Colis colis : colisTemp) {
            colis.setStatut(StatusColis.CREE);
            colisRepository.save(colis);
        }
        return colisTemp.stream().map(colisMapper::toColisResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public ColisResponseDTO suivreColis(String trackingCode) {
        Colis colis = colisRepository.findByTrackingCode(trackingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Colis", "code de suivi", trackingCode));
        return colisMapper.toColisResponseDTO(colis);
    }

    /**
     * Récupère les colis du livreur connecté.
     */
    @Transactional(readOnly = true)
    public List<ColisResponseDTO> getColisForAuthenticatedLivreur() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();

        Livreur livreur = livreurRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Livreur non trouvé avec l'email : " + currentEmail));

        List<Colis> colisAssignes = colisRepository.findAllByLivreurId(livreur.getId());

        return colisAssignes.stream().map(colisMapper::toColisResponseDTO).toList();
    }
}