package com.smartlogi.sdms.application.dto.permission;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionRequestDTO {
    @NotBlank(message = "Le nom de la permission est obligatoire")
    private String name;
}