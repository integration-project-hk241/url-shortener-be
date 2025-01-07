package org.url.urlshortenerbe.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.url.urlshortenerbe.dtos.requests.RoleCreationRequest;
import org.url.urlshortenerbe.dtos.requests.RoleUpdateRequest;
import org.url.urlshortenerbe.dtos.responses.PageResponse;
import org.url.urlshortenerbe.dtos.responses.PermissionResponse;
import org.url.urlshortenerbe.dtos.responses.RoleResponse;
import org.url.urlshortenerbe.entities.Permission;
import org.url.urlshortenerbe.entities.Role;
import org.url.urlshortenerbe.exceptions.AppException;
import org.url.urlshortenerbe.exceptions.ErrorCode;
import org.url.urlshortenerbe.mappers.PermissionMapper;
import org.url.urlshortenerbe.mappers.RoleMapper;
import org.url.urlshortenerbe.repositories.PermissionRepository;
import org.url.urlshortenerbe.repositories.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    public RoleResponse create(RoleCreationRequest roleCreationRequest) {
        Role role = roleMapper.toRole(roleCreationRequest);
        role.setName(role.getName().toUpperCase().replace("-", "_"));
        role.setPermissions(new HashSet<>());

        Set<PermissionResponse> permissionResponseSet = new HashSet<>();

        roleCreationRequest.getPermissions().forEach(permission -> {
            Permission permissionEntity = permissionRepository
                    .findById(permission.toUpperCase().replace("-", "_"))
                    .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOTFOUND));

            role.getPermissions().add(permissionEntity);
            permissionResponseSet.add(permissionMapper.toPermissionResponse(permissionEntity));
        });

        RoleResponse roleResponse = roleMapper.toRoleResponse(roleRepository.save(role));
        roleResponse.setPermissions(permissionResponseSet);

        return roleResponse;
    }

    public PageResponse<RoleResponse> getAll(int page, int size, boolean compact) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Role> roles = roleRepository.findAll(pageable);

        List<RoleResponse> roleResponseList = roles.getContent().stream()
                .map(role -> {
                    RoleResponse roleResponse = roleMapper.toRoleResponse(role);

                    if (compact) {
                        return roleResponse;
                    }

                    Set<PermissionResponse> permissionResponseSet = role.getPermissions().stream()
                            .map(permissionMapper::toPermissionResponse)
                            .collect(Collectors.toSet());
                    roleResponse.setPermissions(permissionResponseSet);

                    return roleResponse;
                })
                .toList();

        return PageResponse.<RoleResponse>builder()
                .items(roleResponseList)
                .page(page)
                .records(roles.getTotalElements())
                .totalPages(roles.getTotalPages())
                .build();
    }

    public RoleResponse getOne(String roleName) {
        Role role = getRole(roleName);

        RoleResponse roleResponse = roleMapper.toRoleResponse(role);
        Set<PermissionResponse> permissionResponseSet = role.getPermissions().stream()
                .map(permissionMapper::toPermissionResponse)
                .collect(Collectors.toSet());
        roleResponse.setPermissions(permissionResponseSet);

        return roleResponse;
    }

    public RoleResponse update(String roleName, RoleUpdateRequest roleUpdateRequest) {
        Role role = getRole(roleName);

        roleMapper.updateRole(role, roleUpdateRequest);
        role.setName(role.getName().toUpperCase().replace("-", "_"));

        Set<Permission> permissions = new HashSet<>();
        // Check if all permissions available
        roleUpdateRequest.getPermissions().forEach(permission -> {
            Permission permissionEntity = permissionRepository
                    .findById(permission.toUpperCase().replace("-", "_"))
                    .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOTFOUND));

            permissions.add(permissionEntity);
        });
        // Set permissions
        role.setPermissions(permissions);

        // Save new permissions
        roleRepository.save(role);

        RoleResponse roleResponse = roleMapper.toRoleResponse(roleRepository.save(role));
        roleResponse.setPermissions(
                permissions.stream().map(permissionMapper::toPermissionResponse).collect(Collectors.toSet()));

        return roleResponse;
    }

    public void delete(String roleName) {
        // find the permission first
        Role role = getRole(roleName);

        roleRepository.delete(role);
    }

    private Role getRole(String roleName) {
        return roleRepository.findById(roleName).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOTFOUND));
    }
}
