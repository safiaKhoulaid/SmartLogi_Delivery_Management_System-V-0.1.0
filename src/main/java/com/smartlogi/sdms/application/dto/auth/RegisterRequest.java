package com.smartlogi.sdms.application.dto.auth;

import com.smartlogi.sdms.domain.model.vo.Adresse;
import com.smartlogi.sdms.domain.model.vo.Telephone;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String lastName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Veuillez fournir une adresse email valide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @NotNull(message = "L'adresse est obligatoire")
    @Valid
    private Adresse adresse;

    @NotNull(message = "Le numéro de téléphone est obligatoire")
    @Valid
    private Telephone telephone;
}