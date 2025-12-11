package com.smartlogi.sdms.application.dto.user;

import com.smartlogi.sdms.domain.model.vo.Adresse;
import com.smartlogi.sdms.domain.model.vo.Telephone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinataireResponseDTO {

    private String id;
    private String nom;
    private String prenom;
    private String email;
    private Telephone telephone;
    private Adresse adresse;
}