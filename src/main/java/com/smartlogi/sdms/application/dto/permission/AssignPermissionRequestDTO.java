package com.smartlogi.sdms.application.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignPermissionRequestDTO {

    @NotBlank(message = "Le rôle est obligatoire")
    private String role;

    @NotNull(message = "L'ID de la permission est obligatoire")
    private String permissionId;
}