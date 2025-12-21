package com.smartlogi.sdms.application.service;


import com.smartlogi.sdms.application.dto.GestionnaireUpdateRequest;
import com.smartlogi.sdms.application.dto.auth.RegisterRequest;
import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.domain.exception.UserAlreadyExistsException;
import com.smartlogi.sdms.domain.model.entity.users.Gestionnaire;
import com.smartlogi.sdms.domain.model.enums.Role;
import com.smartlogi.sdms.domain.repository.BaseUserRepository;
import com.smartlogi.sdms.domain.repository.GestionnaireRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestionnaireService {

    private final GestionnaireRepository gestionnaireRepository;
    private final BaseUserRepository baseUserRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. CREATE
    @Transactional
    public RegisterRequest createGestionnaire(RegisterRequest dto) {
        log.info("Création d'un nouveau gestionnaire : {}", dto.getEmail());

        // Verification Email
        if (baseUserRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Cet email est déjà utilisé.");
        }

        // Verification Password pour création
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new ValidationException("Le mot de passe est obligatoire pour la création.");
        }

        Gestionnaire gestionnaire = Gestionnaire.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword())) // 🔒 Hash Password
                .adresse(dto.getAdresse())
                .telephone(dto.getTelephone())
                .role(Role.GESTIONNAIRE) // 👈 Role Fixe
                .build();

        Gestionnaire saved = gestionnaireRepository.save(gestionnaire);
        return mapToDTO(saved);
    }

    public Page<RegisterRequest> getAllGestionnaires(Pageable pageable) {
        return gestionnaireRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    // 3. READ ONE
    public RegisterRequest getGestionnaireById(String id) {
        Gestionnaire g = gestionnaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gestionnaire introuvable avec ID: " + id));
        return mapToDTO(g);
    }

    //  4. UPDATE
    @Transactional
    public RegisterRequest updateGestionnaire(String id, GestionnaireUpdateRequest dto) {
        log.info("Mise à jour du gestionnaire ID: {}", id);

        Gestionnaire gestionnaire = gestionnaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gestionnaire introuvable"));

        // Update champs simples
        gestionnaire.setFirstName(dto.getFirstName());
        gestionnaire.setLastName(dto.getLastName());
        gestionnaire.setAdresse(dto.getAdresse());
        gestionnaire.setTelephone(dto.getTelephone());

        // Update Password (Ghir ila t-fourna wahd jdid)
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            gestionnaire.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        Gestionnaire updated = gestionnaireRepository.save(gestionnaire);
        return mapToDTO(updated);
    }

    // ✅ 5. DELETE
    @Transactional
    public void deleteGestionnaire(String id) {
        if (!gestionnaireRepository.existsById(id)) {
            throw new ResourceNotFoundException("Gestionnaire introuvable");
        }
        gestionnaireRepository.deleteById(id);
        log.info("Gestionnaire supprimé avec succès. ID: {}", id);
    }

    // 🔄 Helper: Map Entity -> DTO
    private RegisterRequest mapToDTO(Gestionnaire entity) {
        return RegisterRequest.builder()
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .adresse(entity.getAdresse())
                .telephone(entity.getTelephone())
                .build();
    }
}