package org.url.urlshortenerbe.services;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.url.urlshortenerbe.dtos.requests.AuthenticationRequest;
import org.url.urlshortenerbe.dtos.requests.IntrospectTokenRequest;
import org.url.urlshortenerbe.dtos.responses.AuthenticationResponse;
import org.url.urlshortenerbe.dtos.responses.IntrospectTokenResponse;
import org.url.urlshortenerbe.dtos.responses.UserResponse;
import org.url.urlshortenerbe.entities.User;
import org.url.urlshortenerbe.exceptions.AppException;
import org.url.urlshortenerbe.exceptions.ErrorCode;
import org.url.urlshortenerbe.mappers.RoleMapper;
import org.url.urlshortenerbe.mappers.UserMapper;
import org.url.urlshortenerbe.repositories.UserRepository;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    @Value("${jwt.signer-key}")
    protected String SIGNER_KEY;

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        User user = userRepository
                .findByUsername(authenticationRequest.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND));

        boolean authenticated = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());

        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String token = generateToken(user);

        return AuthenticationResponse.builder().token(token).build();
    }

    public IntrospectTokenResponse introspectToken(@Valid IntrospectTokenRequest introspectTokenRequest) {
        String token = introspectTokenRequest.getToken();

        JWSVerifier verifier = null;
        try {
            verifier = new MACVerifier(SIGNER_KEY.getBytes());
        } catch (JOSEException e) {
            // todo: handle this later
            log.error("JWT verification failed", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        SignedJWT signedJWT = null;
        try {
            signedJWT = SignedJWT.parse(token);
        } catch (ParseException e) {
            // todo: handle this later
            log.error("JWT verification failed", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        Date expiryTime = null;
        try {
            expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        } catch (ParseException e) {
            // todo: handle this later
            log.error("JWT verification failed", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        boolean isNotExpired = expiryTime.after(new Date());

        boolean isVerified = false;
        try {
            isVerified = signedJWT.verify(verifier);
        } catch (JOSEException e) {
            // todo: handle this later
            log.error("JWT verification failed", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // if not expired and is verified
        boolean isValid = isNotExpired && isVerified;

        return IntrospectTokenResponse.builder().isValid(isValid).build();
    }

    private String generateToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("phankhai5004.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
        } catch (JOSEException e) {
            log.error("Could not sign JWT", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        return jwsObject.serialize();
    }

    private String buildScope(User user) {
        StringJoiner scopeJoiner = new StringJoiner(" ");

        if (!(user.getRoles().isEmpty())) {
            user.getRoles().forEach(role -> {
                scopeJoiner.add(role.getName());

                if (!role.getPermissions().isEmpty()) {
                    role.getPermissions().forEach(permission -> scopeJoiner.add(permission.getName()));
                }
            });
        }

        return scopeJoiner.toString();
    }

    public UserResponse getCurrentUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();

        User user =
                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND));

        UserResponse userResponse = userMapper.toUserResponse(user);

        // map roles of each user
        userResponse.setRoles(
                user.getRoles().stream().map(roleMapper::toRoleResponse).collect(Collectors.toSet()));

        return userResponse;
    }
}
