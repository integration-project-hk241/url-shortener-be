package org.url.urlshortenerbe.utils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.url.urlshortenerbe.entities.User;
import org.url.urlshortenerbe.exceptions.AppException;
import org.url.urlshortenerbe.exceptions.ErrorCode;
import org.url.urlshortenerbe.repositories.RevokedTokenRepository;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    @Value("${jwt.signer-key}")
    private String SIGNER_KEY;

    @Value("${jwt.expiration-time}")
    private Long EXPIRATION_TIME;

    @Value("${jwt.refresh_time}")
    private Long REFRESH_TIME;

    private final RevokedTokenRepository revokedTokenRepository;

    public boolean isTokenRefreshable(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean isNotExpired = expiryTime.after(new Date());

        boolean isVerified = signedJWT.verify(verifier);

        // In the case if not valid token then throw UNAUTHENTICATED
        if (!(isNotExpired && isVerified)) {
            return false;
        }

        return !revokedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID());
    }

    public SignedJWT verifyJWT(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = isRefresh
                ? new Date(signedJWT
                        .getJWTClaimsSet()
                        .getIssueTime()
                        .toInstant()
                        .plus(REFRESH_TIME, ChronoUnit.SECONDS)
                        .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean isNotExpired = expiryTime.after(new Date());

        boolean isVerified = signedJWT.verify(verifier);

        // In the case if not valid token then throw UNAUTHENTICATED
        if (!(isNotExpired && isVerified)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (revokedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    public String generateToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                //                .issuer("phankhai5004.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(EXPIRATION_TIME, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
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
                scopeJoiner.add("ROLE_" + role.getName());

                if (!role.getPermissions().isEmpty()) {
                    role.getPermissions().forEach(permission -> scopeJoiner.add(permission.getName()));
                }
            });
        }

        return scopeJoiner.toString();
    }
}
