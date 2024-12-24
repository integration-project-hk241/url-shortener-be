package org.url.urlshortenerbe.services;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.url.urlshortenerbe.dtos.requests.UrlCreationRequest;
import org.url.urlshortenerbe.dtos.requests.UrlUpdateRequest;
import org.url.urlshortenerbe.dtos.responses.PageResponse;
import org.url.urlshortenerbe.dtos.responses.UrlResponse;
import org.url.urlshortenerbe.entities.Url;
import org.url.urlshortenerbe.entities.User;
import org.url.urlshortenerbe.exceptions.AppException;
import org.url.urlshortenerbe.exceptions.ErrorCode;
import org.url.urlshortenerbe.mappers.UrlMapper;
import org.url.urlshortenerbe.repositories.UrlRepository;
import org.url.urlshortenerbe.repositories.UserRepository;
import org.url.urlshortenerbe.utils.Base62Encoder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {
    @Value("${url.length}")
    private int shortUrlLength;

    @Value("${url.expiration-time}")
    private int expirationTime;

    private final UrlRepository urlRepository;
    private final UserRepository userRepository;

    private final UrlMapper urlMapper;

    private final Base62Encoder base62Encoder;

    public UrlResponse create(UrlCreationRequest urlCreationRequest) throws NoSuchAlgorithmException {
        // longUrl, alias, userid
        Url url = urlMapper.toUrl(urlCreationRequest);

        String shortUrl;

        // If the user want to custom the alias
        if (urlCreationRequest.getAlias() != null) {
            shortUrl = urlCreationRequest.getAlias();

            if (urlRepository.existsById(shortUrl)) {
                throw new AppException(ErrorCode.ALIAS_EXISTED);
            }
        } else {
            shortUrl = generateHash(url.getLongUrl());

            // Handle potential collisions
            while (urlRepository.existsById(shortUrl)) {
                shortUrl = generateHash(shortUrl);
            }
        }

        url.setShortUrl(shortUrl);
        url.setCreatedAt(Date.from(Instant.now()));
        url.setExpiresAt(Date.from(Instant.now().plus(expirationTime, ChronoUnit.DAYS)));
        url.setLastVisit(null);

        // Find the user if the current is not guest

        SecurityContext securityContext = SecurityContextHolder.getContext();
        String email = securityContext.getAuthentication().getName();
        if (!email.equals("anonymousUser")) {
            User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND));
            url.setUser(user);
        } else {
            url.setUser(null);
        }

        // Save url
        url = urlRepository.save(url);

        return urlMapper.toUrlResponse(url);
    }

    public PageResponse<UrlResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Url> urls = urlRepository.findAll(pageable);

        List<UrlResponse> urlResponseList =
                urls.getContent().stream().map(urlMapper::toUrlResponse).toList();

        return PageResponse.<UrlResponse>builder()
                .items(urlResponseList)
                .page(page)
                .records(urls.getTotalElements())
                .totalPages(urls.getTotalPages())
                .build();
    }

    public UrlResponse getOne(String shortUrl) {
        Url url = getUrlById(shortUrl);

        return urlMapper.toUrlResponse(url);
    }

    public UrlResponse update(String shortUrl, UrlUpdateRequest urlUpdateRequest) {
        if (urlRepository.existsById(shortUrl)) {
            throw new AppException(ErrorCode.ALIAS_EXISTED);
        }

        Url url = getUrlById(shortUrl);

        urlMapper.updateUrl(url, urlUpdateRequest);

        return urlMapper.toUrlResponse(urlRepository.save(url));
    }

    public void delete(String shortUrl) {
        if (!urlRepository.existsById(shortUrl)) {
            throw new AppException(ErrorCode.URL_NOTFOUND);
        }

        urlRepository.deleteById(shortUrl);
    }

    private Url getUrlById(String shortUrl) {
        return urlRepository.findById(shortUrl).orElseThrow(() -> new AppException(ErrorCode.URL_NOTFOUND));
    }

    private String generateHash(String longUrl) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(longUrl.getBytes());

        // Extract the first 6 bytes (12 hex charactesr)
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            String hex = Integer.toHexString(0xFF & digest[i]);

            if (hex.length() == 1) {
                hexString.append('0');
            }

            hexString.append(hex);
        }

        String first6BytesHex = hexString.toString();

        // Convert hex to decimal
        long decimal = new BigInteger(first6BytesHex, 16).longValue();

        // Encode decimal to Base62
        String base62 = base62Encoder.encode(decimal);

        // Ensure the short Url meets the desired length
        if (base62.length() < shortUrlLength) {
            base62 = String.format("%" + shortUrlLength + "s", base62).replace(' ', '0');
        } else if (base62.length() > shortUrlLength) {
            base62 = base62.substring(0, shortUrlLength);
        }

        return base62;
    }
}
