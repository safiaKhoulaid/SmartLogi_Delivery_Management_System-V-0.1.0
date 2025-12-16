package com.smartlogi.sdms.application.dto.user;

import com.smartlogi.sdms.application.validation.email.EmailValid;
import com.smartlogi.sdms.application.validation.password.PasswordValid;
import com.smartlogi.sdms.domain.model.enums.Role;
import com.smartlogi.sdms.domain.model.vo.Adresse;
import com.smartlogi.sdms.domain.model.vo.Telephone;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull; // AJOUTEZ CET IMPORT
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@PasswordValid // Validation que password et matchingPassword sont identiques
public class UserRequestRegisterDTO {


    @EmailValid
    @NotBlank(message = "Email est require")
    private String email;

    @NotBlank(message = "Password est require")
    @Size(min = 6, max = 20, message = "Password doit être entre 6 et 20 caractères")
    private String password;

    @NotBlank(message = "S'il vous plait confirmer votre mot de passe")
    private String matchingPassword;

    @NotBlank(message = "Votre prenom est require")
    private String prenom;

    @NotBlank(message = "Votre nom est require")
    private String nom;

    // CORRECTION APPLIQUÉE
    @NotNull(message = "Numero de telephone est require")
    private Telephone telephone;

    // CORRECTION APPLIQUÉE
    @NotNull(message = "Adresse est require")
    private Adresse adresse;


}