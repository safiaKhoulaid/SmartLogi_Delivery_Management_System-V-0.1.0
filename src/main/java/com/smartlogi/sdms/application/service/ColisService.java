package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.application.dto.colis.ColisRequestDTO;
import com.smartlogi.sdms.application.dto.colis.ColisResponseDTO;
import com.smartlogi.sdms.application.dto.user.DestinataireRequestDTO;
import com.smartlogi.sdms.application.mapper.ColisMapper;
import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.domain.model.entity.Colis;
import com.smartlogi.sdms.domain.model.entity.Zone;
import com.smartlogi.sdms.domain.model.entity.users.ClientExpediteur;
import com.smartlogi.sdms.domain.model.entity.users.Destinataire;
import com.smartlogi.sdms.domain.model.enums.PriorityColis;
import com.smartlogi.sdms.domain.model.enums.StatusColis;
import com.smartlogi.sdms.domain.repository.*;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j // 👈 AJOUT 2: Annotation pour activer le logger 'log'
public class ColisService {

    private final ColisRepository colisRepository;
    private final DestinataireRepository destinataireRepository;
    private final BaseUserService baseUserService;
    private final ColisMapper colisMapper;
    private final ZoneRepository zoneRepository; // AJOUT: Repository de Zone
    private final ClientExpediteurRepository clientExpediteurRepository;
    public final MissionRepository missionRepository;


    @Transactional
    public Colis createColis(ColisRequestDTO dto) {

        log.info("Début de la création du colis pour l'expéditeur: {}", dto.getExpediteurId());

        Destinataire destinataire;
        if (dto.getExpediteurId() == null) {
            log.warn("Tentative de création de colis sans expediteurId.");
            throw new ValidationException("L'ID de l'expéditeur est obligatoire.");
        }

        // 🗑️ SUPPRESSION : La ligne 50 (l'appel .get() dangereux) a été supprimée.

        // Validation Mutuellement Exclusive pour le Destinataire
        if (dto.getDestinataireId() != null && dto.getDestinataireInfo() != null) {
            log.warn("Validation échouée : destinataireId ({}) et destinataireInfo fournis en même temps.", dto.getDestinataireId());
            throw new ValidationException("Vous devez spécifier un ID de destinataire EXISTANT OU de NOUVELLES informations, mais pas les deux.");
        }

        // --- 1. Gérer le destinataire ---
        if (dto.getDestinataireId() != null) {
            log.info("Recherche du destinataire existant par ID: {}", dto.getDestinataireId());
            destinataire = destinataireRepository.findById(dto.getDestinataireId())
                    .orElseThrow(() -> {
                        log.warn("Destinataire non trouvé avec ID: {}", dto.getDestinataireId());
                        return new ResourceNotFoundException("Destinataire existant introuvable");
                    });

        } else if (dto.getDestinataireInfo() != null) {
            log.info("Traitement d'un nouveau destinataire par email: {}", dto.getDestinataireInfo().getEmail());
            DestinataireRequestDTO newDestinataireInfo = dto.getDestinataireInfo();

            // On doit récupérer l'expéditeur ici pour le passer au service de création de destinataire
            ClientExpediteur expediteurPourDestinataire = baseUserService.findClientExpediteurById(dto.getExpediteurId())
                    .orElseThrow(() -> {
                        log.warn("Client expéditeur (pour création destinataire) non trouvé avec ID: {}", dto.getExpediteurId());
                        return new ResourceNotFoundException("Client expéditeur introuvable.");
                    });

            Destinataire existingDestinataire = destinataireRepository.findByEmail(newDestinataireInfo.getEmail()).orElse(null);

            if (existingDestinataire != null) {
                log.info("Destinataire trouvé par email. Utilisation de l'ID: {}", existingDestinataire.getId());
                destinataire = existingDestinataire;
            } else {
                log.info("Aucun destinataire existant trouvé. Création d'un nouveau destinataire...");
                destinataire = baseUserService.createDestinataire(newDestinataireInfo, expediteurPourDestinataire);
                log.info("Nouveau destinataire créé avec ID: {}", destinataire.getId());
            }
        } else {
            log.warn("Validation échouée : ni destinataireId ni destinataireInfo n'ont été fournis.");
            throw new ValidationException("Les informations du destinataire sont obligatoires.");
        }

        // --- 2. Récupérer les dépendances obligatoires ---

        // A. Client Expéditeur (obligatoire)
        ClientExpediteur expediteur = baseUserService.findClientExpediteurById(dto.getExpediteurId())
                .orElseThrow(() -> {
                    log.warn("Client expéditeur non trouvé avec ID: {}", dto.getExpediteurId());
                    return new ResourceNotFoundException("Client expéditeur introuvable.");
                });

        // B. Définir la ville et trouver la zone (obligatoires)
        String villeDestination = destinataire.getAdresse().ville();
        String codePostal = destinataire.getAdresse().codePostal();

        log.info("Recherche de la zone pour la ville: {} et code postal: {}", villeDestination, codePostal);
        Zone zoneDestination = zoneRepository.findByVilleAndCodePostal(villeDestination, codePostal)
                .orElseThrow(() -> {
                    log.warn("Zone logistique introuvable pour la ville: {} et code postal: {}", villeDestination, codePostal);
                    return new ResourceNotFoundException("Zone logistique introuvable pour la ville: " + villeDestination);
                });
        log.info("Zone trouvée: {}", zoneDestination.getId());


        // --- 3. Création et Mappage du Colis ---
        Colis colis = colisMapper.toEntity(dto);

        // Relations Many-to-One
        colis.setDestinataire(destinataire);
        colis.setClientExpediteur(expediteur);
        colis.setZoneDestination(zoneDestination);

        // Champs dérivés
        colis.setVilleDestination(villeDestination);
        colis.setDateCreation(dto.getDateCreation() != null ? dto.getDateCreation() : LocalDateTime.now());

        // Enums (avec valeurs par défaut)
        colis.setStatut(StatusColis.CREE);
        colis.setPriorite(dto.getPriority() != null ? dto.getPriority() : PriorityColis.NORMALE);

        Colis savedColis = colisRepository.save(colis);
        log.info("Colis créé avec succès. ID: {}", savedColis.getId());

        return savedColis;
    }

    /**
     * Récupère tous les colis pour un client spécifique.
     */
    public Page<ColisResponseDTO> getColisByClientExpediteurId(String idClient, Pageable pageable) {

        log.info("Recherche des colis pour le client: {}, Page: {}, Taille: {}", idClient, pageable.getPageNumber(), pageable.getPageSize());

        // --- 1. Vérification de l'existence du client ---
        if (!clientExpediteurRepository.existsById(idClient)) {
            log.warn("Tentative de recherche de colis pour un client inexistant: {}", idClient);
            throw new ResourceNotFoundException(
                    "Impossible de trouver les colis car le ClientExpediteur avec l'ID " + idClient + " est introuvable."
            );
        }

        // --- 2. Requête ---
        Page<Colis> colisPage = colisRepository.findAllByClientExpediteurId(idClient, pageable);
        log.info("Succès. {} colis trouvés pour le client: {}.", colisPage.getTotalElements(), idClient);


        // --- 3. Mappage vers DTO ---
        return colisPage.map(colisMapper::toColisResponseDTO);
    }

    @Transactional
    public ColisResponseDTO updateColis(String colisId, ColisRequestDTO dto) {
        log.info("Tentative de mise à jour du colis ID: {}", colisId);

        // 1. Trouver le colis existant
        Colis colis = colisRepository.findById(colisId)
                .orElseThrow(() -> {
                    log.warn("Mise à jour échouée : Colis non trouvé avec ID: {}", colisId);
                    return new ResourceNotFoundException("Colis", "id", colisId);
                });

        // 2. Mettre à jour les champs autorisés
        // Nous mettons à jour manuellement pour éviter d'écraser des champs
        // comme l'expéditeur, le destinataire ou la zone via ce simple endpoint.

        if (dto.getDescription() != null) {
            colis.setDescription(dto.getDescription());
        }
        if (dto.getPoids() != null) {
            colis.setPoids(dto.getPoids());
        }
        if (dto.getPriority() != null) {
            colis.setPriorite(dto.getPriority());
        }
        // Le statut est généralement mis à jour par d'autres processus (ex: MissionService)
        if (dto.getStatus() != null) {
            colis.setStatut(dto.getStatus());
        }

        // 3. Sauvegarder les modifications
        Colis updatedColis = colisRepository.save(colis);
        log.info("Colis ID: {} mis à jour avec succès.", updatedColis.getId());

        // 4. Retourner le DTO de réponse
        return colisMapper.toColisResponseDTO(updatedColis);
    }

    @Transactional
    public void deleteColis(String colisId) {
        log.info("Tentative de suppression du colis ID: {}", colisId);

        // 1. Vérifier si le colis existe
        if (!colisRepository.existsById(colisId)) {
            log.warn("Suppression échouée : Colis non trouvé avec ID: {}", colisId);
            throw new ResourceNotFoundException("Colis", "id", colisId);
        }

        // 2. Supprimer les enfants (Missions)
        // Note : Les HistoriqueLivraison sont gérés par "CascadeType.ALL" sur l'entité Colis
        log.info("Suppression des missions associées au colis ID: {}", colisId);
        missionRepository.deleteAllByColisId(colisId); //

        // 3. Supprimer le parent (Colis)
        colisRepository.deleteById(colisId);
        log.info("Colis ID: {} et missions associées supprimés avec succès.", colisId);
    }
}