package org.url.urlshortenerbe.services;

import java.text.ParseException;
import java.util.Date;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.url.urlshortenerbe.dtos.requests.AuthenticationRequest;
import org.url.urlshortenerbe.dtos.requests.IntrospectTokenRequest;
import org.url.urlshortenerbe.dtos.requests.RefreshTokenRequest;
import org.url.urlshortenerbe.dtos.requests.RevokeTokenRequest;
import org.url.urlshortenerbe.dtos.responses.AuthenticationResponse;
import org.url.urlshortenerbe.dtos.responses.IntrospectTokenResponse;
import org.url.urlshortenerbe.dtos.responses.UserResponse;
import org.url.urlshortenerbe.entities.RevokedToken;
import org.url.urlshortenerbe.entities.User;
import org.url.urlshortenerbe.exceptions.AppException;
import org.url.urlshortenerbe.exceptions.ErrorCode;
import org.url.urlshortenerbe.mappers.RoleMapper;
import org.url.urlshortenerbe.mappers.UserMapper;
import org.url.urlshortenerbe.repositories.RevokedTokenRepository;
import org.url.urlshortenerbe.repositories.UserRepository;
import org.url.urlshortenerbe.utils.JwtUtil;

import com.nimbusds.jose.*;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final RevokedTokenRepository revokedTokenRepository;

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        User user = userRepository
                .findByEmail(authenticationRequest.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND));

        if (user.getBanned()) {
            throw new AppException(ErrorCode.USER_BANNED);
        }

        boolean authenticated = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());

        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String token = jwtUtil.generateToken(user);

        return AuthenticationResponse.builder().token(token).build();
    }

    public IntrospectTokenResponse introspectToken(@Valid IntrospectTokenRequest introspectTokenRequest)
            throws ParseException, JOSEException {
        boolean isValid = true;

        try {
            jwtUtil.verifyJWT(introspectTokenRequest.getToken(), false);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectTokenResponse.builder().isValid(isValid).build();
    }

    public void revokeToken(RevokeTokenRequest revokeTokenRequest) {
        // When revoke a token if it is not valid then we don't need to care about it
        // If it is valid then revoke it
        try {
            SignedJWT signedJWT = jwtUtil.verifyJWT(revokeTokenRequest.getToken(), true);

            String jit = signedJWT.getJWTClaimsSet().getJWTID();
            Date expiryDate = signedJWT.getJWTClaimsSet().getExpirationTime();

            revokedTokenRepository.save(
                    RevokedToken.builder().id(jit).expiryDate(expiryDate).build());

        } catch (Exception e) {
        }
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest)
            throws ParseException, JOSEException {
        // Revoke the old token
        SignedJWT signedJWT = jwtUtil.verifyJWT(refreshTokenRequest.getToken(), true);

        String jit = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryDate = signedJWT.getJWTClaimsSet().getExpirationTime();

        RevokedToken revokedToken =
                RevokedToken.builder().id(jit).expiryDate(expiryDate).build();

        revokedTokenRepository.save(revokedToken);

        // Issue new token
        String username = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        String token = jwtUtil.generateToken(user);

        return AuthenticationResponse.builder().token(token).build();
    }

    public UserResponse getCurrentUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String email = securityContext.getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        UserResponse userResponse = userMapper.toUserResponse(user);

        // map roles of each user
        userResponse.setRoles(
                user.getRoles().stream().map(roleMapper::toRoleResponse).collect(Collectors.toSet()));

        return userResponse;
    }
}
