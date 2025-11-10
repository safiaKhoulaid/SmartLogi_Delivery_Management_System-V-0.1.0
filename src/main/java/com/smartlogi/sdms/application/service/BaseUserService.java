package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.application.dto.user.DestinataireRequestDTO;
import com.smartlogi.sdms.application.mapper.UserMapper; // Import requis
import com.smartlogi.sdms.domain.model.entity.users.ClientExpediteur;
import com.smartlogi.sdms.domain.model.entity.users.Destinataire;
import com.smartlogi.sdms.domain.repository.ClientExpediteurRepository; // Import requis
import com.smartlogi.sdms.domain.repository.DestinataireRepository; // Import requis
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BaseUserService {

    // Dépendances nécessaires pour les deux fonctions :
    private final DestinataireRepository destinataireRepository;
    private final ClientExpediteurRepository clientExpediteurRepository;
    private final UserMapper userMapper; // Supposé exister pour le mappage DTO <-> Entity

    // Constructeur pour l'injection de dépendances
    public BaseUserService(DestinataireRepository destinataireRepository, ClientExpediteurRepository clientExpediteurRepository, UserMapper userMapper) {
        this.destinataireRepository = destinataireRepository;
        this.clientExpediteurRepository = clientExpediteurRepository;
        this.userMapper = userMapper;
    }

    /**
     * Crée, persiste, et retourne une nouvelle entité Destinataire.
     * @param dto Les données du nouveau destinataire.
     * @return L'entité Destinataire persistée.
     */
    public Destinataire createDestinataire(DestinataireRequestDTO dto, ClientExpediteur expediteur) { // ATTENTION à la signature

        // 1. Mappage du DTO vers l'entité Destinataire (BaseUser est mappé)
        Destinataire destinataire = userMapper.toDestinataireEntity(dto);

        // 2. CORRECTION CRITIQUE: Lier la relation Many-to-One
        // Ceci mappe l'entité expediteur sur la colonne expediteur_id
        destinataire.setClientExpediteur(expediteur);

        // 3. Sauvegarde de l'entité
        return destinataireRepository.save(destinataire);
    }
    /**
     * Recherche un ClientExpediteur par son ID.
     * NOTE: L'ID est supposé être de type Long (bigint en base).
     * @param expediteurId L'ID du ClientExpediteur.
     * @return Un Optional contenant le ClientExpediteur s'il est trouvé.
     */
    public Optional<ClientExpediteur> findClientExpediteurById(String expediteurId) {

        // Utilise le repository spécifique pour ClientExpediteur.
        return clientExpediteurRepository.findById(expediteurId);
    }
}