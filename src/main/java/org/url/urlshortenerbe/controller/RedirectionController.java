package org.url.urlshortenerbe.controller;

import java.net.URI;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.url.urlshortenerbe.services.RedirectionService;
import org.url.urlshortenerbe.services.UrlService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RedirectionController {
    private final UrlService urlService;
    private final RedirectionService redirectionService;

    @GetMapping("/{shortUrl}")
    public ResponseEntity<?> redirect(
            @PathVariable String shortUrl,
            @RequestHeader(HttpHeaders.REFERER) Optional<String> referer,
            @RequestHeader(HttpHeaders.USER_AGENT) String userAgent,
            @RequestParam(defaultValue = "Others", required = false, value = "r") String refererHeader) {

        String longUrl = redirectionService.getUrlAndCountClick(shortUrl, referer.orElse(refererHeader), userAgent);

        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(URI.create(longUrl))
                .build();
    }
}
