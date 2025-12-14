package com.smartlogi.sdms.application.mapper;

import com.smartlogi.sdms.application.dto.permission.PermissionRequestDTO;
import com.smartlogi.sdms.application.dto.permission.PermissionResponseDTO;
import com.smartlogi.sdms.domain.model.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    // Permission -> DTO
    PermissionResponseDTO toResponseDTO(Permission permission);

    // List<Permission> -> List<DTO>
    List<PermissionResponseDTO> toResponseDTOs(List<Permission> permissions);

    // DTO -> Permission (Ignorer ID)
    @Mapping(target = "id", ignore = true)
    Permission toEntity(PermissionRequestDTO dto);
}