package org.url.urlshortenerbe.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
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
            @RequestHeader("Referer") String referer,
            @RequestHeader("User-Agent") String userAgent) {
        String longUrl = redirectionService.getUrlAndCountClick(shortUrl, referer, userAgent);

        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(URI.create(longUrl))
                .build();
    }
}
