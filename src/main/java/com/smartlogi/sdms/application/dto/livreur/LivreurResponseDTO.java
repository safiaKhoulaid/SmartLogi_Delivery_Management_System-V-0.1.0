package com.smartlogi.sdms.application.dto.livreur;


import com.smartlogi.sdms.domain.model.vo.Telephone;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Valid
public class LivreurResponseDTO {
    String id;
    String nomComplet;
    Telephone telephone;
    String email;
}
