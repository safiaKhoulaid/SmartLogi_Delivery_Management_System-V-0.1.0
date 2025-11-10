package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.application.dto.routes.LocationDTO;
import com.smartlogi.sdms.application.dto.routes.OptimizedRouteResponse;
import com.smartlogi.sdms.application.dto.routes.RouteRequest;
import com.smartlogi.sdms.application.dto.tournee.TourneeRequestDTO;
import com.smartlogi.sdms.application.dto.tournee.TourneeResponseDTO;
import com.smartlogi.sdms.application.mapper.TourneeMapper;
import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.domain.model.entity.Colis;
import com.smartlogi.sdms.domain.model.entity.Tournee;
import com.smartlogi.sdms.domain.model.entity.Zone;
import com.smartlogi.sdms.domain.model.entity.users.Livreur;
import com.smartlogi.sdms.domain.model.enums.StatutTournee;
import com.smartlogi.sdms.domain.model.vo.Adresse;
import com.smartlogi.sdms.domain.repository.ColisRepository;
import com.smartlogi.sdms.domain.repository.LivreurRepository;
import com.smartlogi.sdms.domain.repository.TourneeRepository;
import com.smartlogi.sdms.domain.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourneeService {

    private final TourneeRepository tourneeRepository;
    private final ColisRepository colisRepository;
    private final LivreurRepository livreurRepository;
    private final ZoneRepository zoneRepository;
    private final RouteOptimizationService routeOptimizationService;
    private final TourneeMapper tourneeMapper;

    @Transactional
    public TourneeResponseDTO createAndOptimizeTournee(TourneeRequestDTO request) {

        //verification de livreur
        Livreur livreur = livreurRepository.findById(request.getLivreurId())
                .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé avec l'ID: " + request.getLivreurId()));

        //verification de zone
        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zone non trouvée avec l'ID: " + request.getZoneId()));

        //verification de colis
        List<Colis> colisALivrer = colisRepository.findAllById(request.getColisIds());
        if (colisALivrer.size() != request.getColisIds().size()) {
            throw new ResourceNotFoundException("Certains colis n'ont pas été trouvés.");
        }

        // 1. Préparer la RouteRequest pour l'optimisation
        RouteRequest routeRequest = buildRouteRequest(livreur, zone, colisALivrer);

        // 2. Appeler le service d'optimisation
        OptimizedRouteResponse optimizationResponse = routeOptimizationService.optimizeRoutes(
                routeRequest,
                request.getAlgorithme()
        );

        // 3. Créer et sauvegarder l'entité Tournee
        Tournee tournee = new Tournee();
        tournee.setLivreur(livreur);
        tournee.setZone(zone);
        tournee.setDateTournee(request.getDateTournee());
        tournee.setStatut(StatutTournee.PLANIFIEE);

        // 4. Traiter la réponse d'optimisation
        // CORRECTION : Utilisation de .getTournees() (votre 1ère demande)
        if (optimizationResponse.getTournees() == null || optimizationResponse.getTournees().isEmpty()) {
            throw new IllegalStateException("L'optimisation n'a retourné aucune tournée.");
        }

        OptimizedRouteResponse.RouteDetails routeDetails = optimizationResponse.getTournees().get(0);

        tournee.setDistanceTotaleKm(routeDetails.getRouteDistanceKm());
        tournee.setDureeEstimeeHeures(routeDetails.getRouteEstimatedTimeHours());

        Tournee savedTournee = tourneeRepository.save(tournee);

        // 5. Mettre à jour les colis avec l'ordre et l'ID de la tournée
        Map<String, Colis> colisMap = colisALivrer.stream()
                .collect(Collectors.toMap(Colis::getId, Function.identity()));

        List<String> stopOrderIds = routeDetails.getStopOrder();
        for (int i = 0; i < stopOrderIds.size(); i++) {
            String colisId = stopOrderIds.get(i);
            Colis colis = colisMap.get(colisId);
            if (colis != null) {
                colis.setTournee(savedTournee);
                colis.setOrdreLivraison(i + 1); // Ordre commençant à 1
                colisRepository.save(colis);
            }
        }

        // Rafraîchir l'entité tournée pour inclure la liste des colis mis à jour
        // Note : L'ID de Tournee est un Long (auto-généré), pas un String
        Tournee finalTournee = tourneeRepository.findById(savedTournee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Erreur lors de la récupération de la tournée sauvegardée."));

        return tourneeMapper.toResponseDTO(finalTournee);
    }

    /**
     * Méthode corrigée pour utiliser l'adresse de dépôt de la zone.
     */
    // Dans : sdms/src/main/java/com/smartlogi/sdms/application/service/TourneeService.java

    /**
     * Méthode corrigée pour utiliser les accesseurs des records (.latitude() au lieu de .getLatitude())
     * ET pour convertir BigDecimal en double (.doubleValue()).
     */
    // Dans : sdms/src/main/java/com/smartlogi/sdms/application/service/TourneeService.java

    // Dans : sdms/src/main/java/com/smartlogi/sdms/application/service/TourneeService.java

    private RouteRequest buildRouteRequest(Livreur livreur, Zone zone, List<Colis> colisList) {

        // 1. Vérification et définition du dépôt (Point de départ)
        if (zone.getAdresseDepot() == null || zone.getAdresseDepot().latitude() == null || zone.getAdresseDepot().longitude() == null) {
            throw new IllegalStateException("La zone " + zone.getNom() + " (ID: " + zone.getId() + ") n'a pas de coordonnées de dépôt configurées.");
        }

        LocationDTO depot = new LocationDTO(
                "DEPOT_" + zone.getId(),
                zone.getAdresseDepot().latitude(),  // (Ceci est sûr grâce à la vérification ci-dessus)
                zone.getAdresseDepot().longitude(), // (Ceci est sûr grâce à la vérification ci-dessus)
                BigDecimal.ZERO
        );

        // 2. Vérification et définition des points de livraison (Colis)
        List<LocationDTO> locations = colisList.stream()
                .map(colis -> {
                    // Vérifications de base
                    if (colis.getDestinataire() == null || colis.getDestinataire().getAdresse() == null) {
                        throw new IllegalStateException("Le colis " + colis.getId() + " n'a pas de destinataire ou d'adresse valide.");
                    }
                    if (colis.getPoids() == null) {
                        throw new IllegalStateException("Le colis " + colis.getId() + " n'a pas de poids défini.");
                    }

                    // ⬇️ --- CORRECTION IMPORTANTE --- ⬇️
                    // Vérification que les coordonnées de l'adresse du colis existent
                    Adresse adresseColis = colis.getDestinataire().getAdresse();
                    if (adresseColis.latitude() == null || adresseColis.longitude() == null) {
                        throw new IllegalStateException("Le colis " + colis.getId() + " (Destinataire: " + colis.getDestinataire().getFirstName() + colis.getDestinataire().getFirstName() + ") n'a pas de coordonnées GPS (latitude/longitude) valides.");
                    }
                    // ⬆️ --- FIN DE LA CORRECTION --- ⬆️

                    return new LocationDTO(
                            colis.getId(),
                            adresseColis.latitude(),  // (Ceci est maintenant sûr)
                            adresseColis.longitude(), // (Ceci est maintenant sûr)
                            colis.getPoids().valeur()
                    );
                })
                .collect(Collectors.toList());

        // 3. Définir les données du livreur
        if (livreur.getVehicule() == null) {
            throw new IllegalStateException("Le livreur " + livreur.getId() + " n'a pas de véhicule assigné.");
        }

        RouteRequest.LivreurData livreurData = new RouteRequest.LivreurData(
                livreur.getId(),
                livreur.getVehicule().capaciteMaximale()
        );

        return new RouteRequest(depot, locations, List.of(livreurData));

    }
    // --- Autres méthodes CRUD (l'ID de Tournee est un Long) ---

    public TourneeResponseDTO getTourneeById(Long id) {
        Tournee tournee = tourneeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournée non trouvée avec l'ID: " + id));
        return tourneeMapper.toResponseDTO(tournee);
    }

    public List<TourneeResponseDTO> getTourneesByLivreur(String livreurId) {
        return tourneeRepository.findByLivreurId(livreurId).stream()
                .map(tourneeMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTournee(Long id) {
        Tournee tournee = tourneeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournée non trouvée avec l'ID: " + id));

        // Dé-associer les colis avant de supprimer
        tournee.getLivraisons().forEach(colis -> {
            colis.setTournee(null);
            colis.setOrdreLivraison(null);
            colisRepository.save(colis);
        });

        tourneeRepository.delete(tournee);
    }

    @Transactional
    public TourneeResponseDTO updateTourneeStatus(Long id, StatutTournee statut) {
        Tournee tournee = tourneeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournée non trouvée avec l'ID: " + id));
        tournee.setStatut(statut);
        Tournee updatedTournee = tourneeRepository.save(tournee);
        return tourneeMapper.toResponseDTO(updatedTournee);
    }
}