package org.url.urlshortenerbe.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.url.urlshortenerbe.dtos.requests.PermissionCreationRequest;
import org.url.urlshortenerbe.dtos.requests.PermissionUpdateRequest;
import org.url.urlshortenerbe.dtos.responses.PermissionResponse;
import org.url.urlshortenerbe.entities.Permission;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PermissionMapper {
    Permission toPermission(PermissionCreationRequest permissionRequest);

    PermissionResponse toPermissionResponse(Permission permission);

    void updatePermission(@MappingTarget Permission permission, PermissionUpdateRequest permissionUpdateRequest);
}
