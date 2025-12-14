package com.smartlogi.sdms.application.dto.permission;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RolePermissionsResponseDTO {
    private String role;
    private List<PermissionResponseDTO> permissions;
}