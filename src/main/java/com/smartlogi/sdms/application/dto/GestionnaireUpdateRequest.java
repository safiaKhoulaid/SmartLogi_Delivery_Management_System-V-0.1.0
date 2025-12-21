package com.smartlogi.sdms.application.dto;

import com.smartlogi.sdms.domain.model.vo.Adresse;
import com.smartlogi.sdms.domain.model.vo.Telephone;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GestionnaireUpdateRequest {


    @Size(min = 2)
    private String firstName;

    @Size(min = 2)
    private String lastName;

    @Size(min = 8)
    private String password;

    @Valid
    private Adresse adresse;

    @Valid
    private Telephone telephone;
}