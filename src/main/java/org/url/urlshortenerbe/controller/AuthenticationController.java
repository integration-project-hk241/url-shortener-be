package org.url.urlshortenerbe.controller;

import java.text.ParseException;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.url.urlshortenerbe.dtos.requests.AuthenticationRequest;
import org.url.urlshortenerbe.dtos.requests.IntrospectTokenRequest;
import org.url.urlshortenerbe.dtos.requests.RefreshTokenRequest;
import org.url.urlshortenerbe.dtos.requests.RevokeTokenRequest;
import org.url.urlshortenerbe.dtos.responses.AuthenticationResponse;
import org.url.urlshortenerbe.dtos.responses.IntrospectTokenResponse;
import org.url.urlshortenerbe.dtos.responses.Response;
import org.url.urlshortenerbe.dtos.responses.UserResponse;
import org.url.urlshortenerbe.services.AuthenticationService;

import com.nimbusds.jose.JOSEException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/auth")
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
            @RequestBody @Valid IntrospectTokenRequest introspectTokenRequest) throws ParseException, JOSEException {
        return Response.<IntrospectTokenResponse>builder()
                .success(true)
                .data(authenticationService.introspectToken(introspectTokenRequest))
                .build();
    }

    @PostMapping("/revoke")
    public Response<Void> revokeToken(@RequestBody @Valid RevokeTokenRequest revokeTokenRequest) {
        authenticationService.revokeToken(revokeTokenRequest);

        return Response.<Void>builder().success(true).build();
    }

    @PostMapping("/refresh")
    public Response<AuthenticationResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest)
            throws ParseException, JOSEException {
        return Response.<AuthenticationResponse>builder()
                .success(true)
                .data(authenticationService.refreshToken(refreshTokenRequest))
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
