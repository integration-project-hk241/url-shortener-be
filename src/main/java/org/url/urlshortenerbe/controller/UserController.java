package org.url.urlshortenerbe.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.url.urlshortenerbe.dtos.requests.UserCreationRequest;
import org.url.urlshortenerbe.dtos.requests.UserUpdateRequest;
import org.url.urlshortenerbe.dtos.responses.PageResponse;
import org.url.urlshortenerbe.dtos.responses.Response;
import org.url.urlshortenerbe.dtos.responses.UserResponse;
import org.url.urlshortenerbe.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public Response<UserResponse> create(@RequestBody @Valid UserCreationRequest userCreationRequest) {
        return Response.<UserResponse>builder()
                .success(true)
                .data(userService.create(userCreationRequest))
                .build();
    }

    @GetMapping
    public Response<PageResponse<UserResponse>> getAll(
            @RequestParam(defaultValue = "1", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "false", required = false) boolean compact,
            @RequestParam(defaultValue = "not_deleted", required = false) String type) {
        return Response.<PageResponse<UserResponse>>builder()
                .success(true)
                .data(userService.getAll(page, size, compact, type))
                .build();
    }

    @GetMapping("/{userId}")
    public Response<UserResponse> getOne(
            @PathVariable("userId") String userId,
            @RequestParam(defaultValue = "false", required = false) boolean compact) {
        return Response.<UserResponse>builder()
                .success(true)
                .data(userService.getOne(userId, compact))
                .build();
    }

    @PutMapping("/{userId}")
    public Response<UserResponse> update(
            @PathVariable("userId") String userId, @RequestBody @Valid UserUpdateRequest userUpdateRequest) {
        return Response.<UserResponse>builder()
                .success(true)
                .data(userService.update(userId, userUpdateRequest))
                .build();
    }

    @DeleteMapping("/{userId}")
    public Response<Void> delete(@PathVariable("userId") String userId) {
        userService.delete(userId);

        return Response.<Void>builder().success(true).build();
    }
}
