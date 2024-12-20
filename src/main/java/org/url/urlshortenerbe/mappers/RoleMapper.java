package org.url.urlshortenerbe.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.url.urlshortenerbe.dtos.requests.RoleCreationRequest;
import org.url.urlshortenerbe.dtos.requests.RoleUpdateRequest;
import org.url.urlshortenerbe.dtos.responses.RoleResponse;
import org.url.urlshortenerbe.entities.Role;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleCreationRequest roleCreationRequest);

    @Mapping(target = "permissions", ignore = true)
    RoleResponse toRoleResponse(Role role);

    @Mapping(target = "permissions", ignore = true)
    void updateRole(@MappingTarget Role role, RoleUpdateRequest roleUpdateRequest);
}
