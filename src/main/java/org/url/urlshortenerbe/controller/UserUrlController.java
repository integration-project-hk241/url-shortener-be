package org.url.urlshortenerbe.controller;

import java.security.NoSuchAlgorithmException;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.url.urlshortenerbe.dtos.requests.UrlCreationRequest;
import org.url.urlshortenerbe.dtos.requests.UrlUpdateRequest;
import org.url.urlshortenerbe.dtos.responses.PageResponse;
import org.url.urlshortenerbe.dtos.responses.Response;
import org.url.urlshortenerbe.dtos.responses.UrlResponse;
import org.url.urlshortenerbe.services.UrlService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users/{userId}/urls")
@RequiredArgsConstructor
public class UserUrlController {
    private final UrlService urlService;

    @PostMapping
    public Response<UrlResponse> createWithUserId(
            @PathVariable String userId, @RequestBody @Valid UrlCreationRequest urlCreationRequest)
            throws NoSuchAlgorithmException {
        return Response.<UrlResponse>builder()
                .success(true)
                .data(urlService.createWithUserId(userId, urlCreationRequest))
                .build();
    }

    @GetMapping
    public Response<PageResponse<UrlResponse>> getAllByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Response.<PageResponse<UrlResponse>>builder()
                .success(true)
                .data(urlService.getAllByUserId(userId, page, size))
                .build();
    }

    @GetMapping("/{shortUrl}")
    public Response<UrlResponse> getOneByShortUrlAndUserId(@PathVariable String shortUrl, @PathVariable String userId) {
        return Response.<UrlResponse>builder()
                .success(true)
                .data(urlService.getOneByIdAndUserId(shortUrl, userId))
                .build();
    }

    @PutMapping("/{shortUrl}")
    public Response<UrlResponse> updateONeByShortUrlAndUserId(
            @PathVariable String shortUrl,
            @PathVariable String userId,
            @RequestBody @Valid UrlUpdateRequest urlUpdateRequest)
            throws NoSuchAlgorithmException {
        return Response.<UrlResponse>builder()
                .success(true)
                .data(urlService.updateOneByShortUrlAndUserId(shortUrl, userId, urlUpdateRequest))
                .build();
    }

    @DeleteMapping("/{shortUrl}")
    public Response<Void> deleteOneByShortUrl(@PathVariable String shortUrl, @PathVariable String userId) {
        urlService.deleteOneByShortUrlAndUserId(shortUrl, userId);

        return Response.<Void>builder().success(true).build();
    }
}
