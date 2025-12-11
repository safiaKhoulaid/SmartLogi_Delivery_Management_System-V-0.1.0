package com.smartlogi.sdms.application.dto.livreur;


import com.smartlogi.sdms.domain.model.vo.Telephone;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder

public class LivreurResponseDTO {
    private String id;
    private String nomComplet;
    private Telephone telephone;
    private String email;
}
