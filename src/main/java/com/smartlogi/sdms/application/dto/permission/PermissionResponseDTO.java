package com.smartlogi.sdms.application.dto.permission;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionResponseDTO {
    private String message ;
    private String id;
    private String name;
}