package org.url.urlshortenerbe.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.url.urlshortenerbe.dtos.requests.AuthenticationRequest;
import org.url.urlshortenerbe.dtos.requests.IntrospectTokenRequest;
import org.url.urlshortenerbe.dtos.responses.AuthenticationResponse;
import org.url.urlshortenerbe.dtos.responses.IntrospectTokenResponse;
import org.url.urlshortenerbe.dtos.responses.Response;
import org.url.urlshortenerbe.dtos.responses.UserResponse;
import org.url.urlshortenerbe.services.AuthenticationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/token")
    public Response<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest authenticationRequest) {
        return Response.<AuthenticationResponse>builder()
                .success(true)
                .data(authenticationService.authenticate(authenticationRequest))
                .build();
    }

    @PostMapping("/introspect")
    public Response<IntrospectTokenResponse> introspectToken(
            @RequestBody @Valid IntrospectTokenRequest introspectTokenRequest) {
        return Response.<IntrospectTokenResponse>builder()
                .success(true)
                .data(authenticationService.introspectToken(introspectTokenRequest))
                .build();
    }

    @GetMapping("/me")
    public Response<UserResponse> getCurrentUser() {
        return Response.<UserResponse>builder()
                .success(true)
                .data(authenticationService.getCurrentUser())
                .build();
    }
}
