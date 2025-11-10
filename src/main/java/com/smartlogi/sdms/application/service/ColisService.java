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
import com.smartlogi.sdms.domain.repository.ClientExpediteurRepository;
import com.smartlogi.sdms.domain.repository.ColisRepository;
import com.smartlogi.sdms.domain.repository.DestinataireRepository;
import com.smartlogi.sdms.domain.repository.ZoneRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ColisService {

    private final ColisRepository colisRepository;
    private final DestinataireRepository destinataireRepository;
    private final BaseUserService baseUserService;
    private final ColisMapper colisMapper;
    private final ZoneRepository zoneRepository; // AJOUT: Repository de Zone
    private final ClientExpediteurRepository clientExpediteurRepository;


    @Transactional
    public Colis createColis(ColisRequestDTO dto) {

        Destinataire destinataire;
        if (dto.getExpediteurId() == null) {
            throw new ValidationException("L'ID de l'expéditeur est obligatoire.");
        }
        // Validation Mutuellement Exclusive pour le Destinataire
        ClientExpediteur clientExpediteur = baseUserService.findClientExpediteurById(dto.getExpediteurId()).get();

        if (dto.getDestinataireId() != null && dto.getDestinataireInfo() != null) {
            throw new ValidationException("Vous devez spécifier un ID de destinataire EXISTANT OU de NOUVELLES informations, mais pas les deux.");
        }

        // --- 1. Gérer le destinataire ---
        if (dto.getDestinataireId() != null) {
            destinataire = destinataireRepository.findById(dto.getDestinataireId())
                    .orElseThrow(() -> new ResourceNotFoundException("Destinataire existant introuvable"));

        } else if (dto.getDestinataireInfo() != null) {
            DestinataireRequestDTO newDestinataireInfo = dto.getDestinataireInfo();
            Destinataire existingDestinataire = destinataireRepository.findByEmail(newDestinataireInfo.getEmail()).orElse(null);

            if (existingDestinataire != null) {
                destinataire = existingDestinataire;
            } else {
                destinataire = baseUserService.createDestinataire(newDestinataireInfo, clientExpediteur);
            }
        } else {
            throw new ValidationException("Les informations du destinataire sont obligatoires.");
        }

        // --- 2. Récupérer les dépendances obligatoires ---

        // A. Client Expéditeur (obligatoire)
        ClientExpediteur expediteur = baseUserService.findClientExpediteurById(dto.getExpediteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Client expéditeur introuvable."));

        // B. Définir la ville et trouver la zone (obligatoires)
        String villeDestination = destinataire.getAdresse().ville();
        String codePostal = destinataire.getAdresse().codePostal();

        // Assurez-vous d'avoir findByVille dans ZoneRepository
        Zone zoneDestination = zoneRepository.findByVilleAndCodePostal(villeDestination, codePostal)
                .orElseThrow(() -> new ResourceNotFoundException("Zone logistique introuvable pour la ville: " + villeDestination));


        // --- 3. Création et Mappage du Colis ---
        Colis colis = colisMapper.toEntity(dto); // Mappe les champs directs (description, poids)

        // Mappage des champs dérivés/obligatoires

        // Relations Many-to-One (NOT NULL)
        colis.setDestinataire(destinataire);
        colis.setClientExpediteur(expediteur);       // CORRECTION N°1
        colis.setZoneDestination(zoneDestination);   // CORRECTION N°2

        // Champs dérivés (NOT NULL)
        colis.setVilleDestination(villeDestination); // CORRECTION N°3 (Déjà trouvée, mais non mappée)
        colis.setDateCreation(dto.getDateCreation() != null ? dto.getDateCreation() : LocalDateTime.now()); // Date (si le mapper ne le fait pas)

        // Enums (NOT NULL)
        colis.setStatut(StatusColis.CREE); // CORRECTION N°4: Statut initial par défaut
        colis.setPriorite(dto.getPriority() != null ? dto.getPriority() : PriorityColis.NORMALE); // CORRECTION N°5: Priorité par défaut ou du DTO
        // Remarque: Le DTO ColisRequestDTO ne contient pas de champ 'priorite' exposé,
        // donc nous forçons une valeur par défaut NORMALE, ou nous nous assurons que le mapper la définit.

        return colisRepository.save(colis);
    }

    // Dans sdms/src/main/java/com/smartlogi/sdms/application/service/ColisService.java
// Assurez-vous que ClientExpediteurRepository est injecté dans le constructeur.

    /**
     * Récupère tous les colis pour un client spécifique, de manière performante
     * et avec une gestion correcte des exceptions.
     */


    public Page<ColisResponseDTO> getColisByClientExpediteurId(String idClient, Pageable pageable) {

        // --- 1. Vérification de l'existence du client ---
        if (!clientExpediteurRepository.existsById(idClient)) {
            throw new ResourceNotFoundException(
                    "Impossible de trouver les colis car le ClientExpediteur avec l'ID " + idClient + " est introuvable."
            );
        }

        // --- 2. Requête optimisée (pagination + fetch relations nécessaires) ---
        Page<Colis> colisPage = colisRepository.findAllByClientExpediteurId(idClient, pageable);

        // --- 3. Mappage vers DTO ---
        // `.map()` est fourni par Page, donc aucun besoin de convertir manuellement en stream
        return colisPage.map(colisMapper::toColisResponseDTO);
    }

}