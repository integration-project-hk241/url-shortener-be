package org.url.urlshortenerbe.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.url.urlshortenerbe.dtos.requests.PermissionCreationRequest;
import org.url.urlshortenerbe.dtos.requests.PermissionUpdateRequest;
import org.url.urlshortenerbe.dtos.responses.PageResponse;
import org.url.urlshortenerbe.dtos.responses.PermissionResponse;
import org.url.urlshortenerbe.dtos.responses.Response;
import org.url.urlshortenerbe.services.PermissionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    @PostMapping
    Response<PermissionResponse> create(@RequestBody @Valid PermissionCreationRequest permissionCreationRequest) {
        return Response.<PermissionResponse>builder()
                .success(true)
                .data(permissionService.create(permissionCreationRequest))
                .build();
    }

    @GetMapping
    Response<PageResponse<PermissionResponse>> getAll(
            @RequestParam(defaultValue = "1", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size) {
        return Response.<PageResponse<PermissionResponse>>builder()
                .success(true)
                .data(permissionService.getAll(page, size))
                .build();
    }

    @GetMapping("/{permissionName}")
    Response<PermissionResponse> getOne(@PathVariable("permissionName") String permissionName) {
        return Response.<PermissionResponse>builder()
                .success(true)
                .data(permissionService.getOne(permissionName))
                .build();
    }

    @PutMapping("/{permissionName}")
    Response<PermissionResponse> update(
            @PathVariable("permissionName") String permissionName,
            @RequestBody @Valid PermissionUpdateRequest permissionUpdateRequest) {
        return Response.<PermissionResponse>builder()
                .success(true)
                .data(permissionService.update(permissionName, permissionUpdateRequest))
                .build();
    }

    @DeleteMapping("/{permissionName}")
    Response<Void> delete(@PathVariable("permissionName") String permissionName) {
        permissionService.delete(permissionName);

        return Response.<Void>builder().success(true).build();
    }
}
