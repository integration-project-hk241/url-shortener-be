package org.url.urlshortenerbe.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.url.urlshortenerbe.dtos.requests.PermissionCreationRequest;
import org.url.urlshortenerbe.dtos.requests.PermissionUpdateRequest;
import org.url.urlshortenerbe.dtos.responses.PageResponse;
import org.url.urlshortenerbe.dtos.responses.PermissionResponse;
import org.url.urlshortenerbe.entities.Permission;
import org.url.urlshortenerbe.exceptions.AppException;
import org.url.urlshortenerbe.exceptions.ErrorCode;
import org.url.urlshortenerbe.mappers.PermissionMapper;
import org.url.urlshortenerbe.repositories.PermissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionResponse create(PermissionCreationRequest permissionCreationRequest) {
        Permission permission = permissionMapper.toPermission(permissionCreationRequest);
        permission.setName(permission.getName().toUpperCase());

        return permissionMapper.toPermissionResponse(permissionRepository.save(permission));
    }

    public PageResponse<PermissionResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Permission> permissions = permissionRepository.findAll(pageable);

        List<PermissionResponse> permissionResponseList = permissions.getContent().stream()
                .map(permissionMapper::toPermissionResponse)
                .toList();

        return PageResponse.<PermissionResponse>builder()
                .items(permissionResponseList)
                .page(page)
                .records(permissions.getTotalElements())
                .totalPages(permissions.getTotalPages())
                .build();
    }

    public PermissionResponse getOne(String permissionName) {
        Permission permission = getPermission(permissionName);

        return permissionMapper.toPermissionResponse(permission);
    }

    public PermissionResponse update(String permissionId, PermissionUpdateRequest permissionUpdateRequest) {
        Permission permission = getPermission(permissionId);

        permissionMapper.updatePermission(permission, permissionUpdateRequest);

        return permissionMapper.toPermissionResponse(permissionRepository.save(permission));
    }

    public void delete(String permissionName) {
        // find the permission first
        Permission permission = getPermission(permissionName);

        permissionRepository.delete(permission);
    }

    private Permission getPermission(String permissionName) {
        return permissionRepository
                .findById(permissionName)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOTFOUND));
    }
}
