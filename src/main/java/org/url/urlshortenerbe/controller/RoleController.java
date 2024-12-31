package org.url.urlshortenerbe.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.url.urlshortenerbe.dtos.requests.RoleCreationRequest;
import org.url.urlshortenerbe.dtos.requests.RoleUpdateRequest;
import org.url.urlshortenerbe.dtos.responses.PageResponse;
import org.url.urlshortenerbe.dtos.responses.Response;
import org.url.urlshortenerbe.dtos.responses.RoleResponse;
import org.url.urlshortenerbe.services.RoleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    Response<RoleResponse> create(@RequestBody @Valid RoleCreationRequest roleCreationRequest) {
        return Response.<RoleResponse>builder()
                .success(true)
                .data(roleService.create(roleCreationRequest))
                .build();
    }

    @GetMapping
    Response<PageResponse<RoleResponse>> getAll(
            @RequestParam(defaultValue = "1", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "false", required = false) boolean compact) {
        return Response.<PageResponse<RoleResponse>>builder()
                .success(true)
                .data(roleService.getAll(page, size, compact))
                .build();
    }

    @GetMapping("/{roleName}")
    Response<RoleResponse> getOne(@PathVariable("roleName") String roleName) {
        return Response.<RoleResponse>builder()
                .success(true)
                .data(roleService.getOne(roleName))
                .build();
    }

    @PutMapping("/{roleName}")
    Response<RoleResponse> update(
            @PathVariable("roleName") String roleName, @RequestBody @Valid RoleUpdateRequest roleUpdateRequest) {
        return Response.<RoleResponse>builder()
                .success(true)
                .data(roleService.update(roleName, roleUpdateRequest))
                .build();
    }

    @DeleteMapping("/{roleName}")
    Response<Void> delete(@PathVariable("roleName") String roleName) {
        roleService.delete(roleName);

        return Response.<Void>builder().success(true).build();
    }
}
