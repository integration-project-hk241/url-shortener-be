package org.url.urlshortenerbe.controller;

import java.security.NoSuchAlgorithmException;
import java.util.List;

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
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "not_deleted", required = false) String type) {
        return Response.<PageResponse<UrlResponse>>builder()
                .success(true)
                .data(urlService.getAllByUserId(userId, page, size, type))
                .build();
    }

    @GetMapping("/{hash}")
    public Response<UrlResponse> getOneByHashAndUserId(@PathVariable String hash, @PathVariable String userId) {
        return Response.<UrlResponse>builder()
                .success(true)
                .data(urlService.getOneByHashAndUserId(hash, userId))
                .build();
    }

    @GetMapping("/search")
    public Response<List<UrlResponse>> searchUrlsWithinUserId(@PathVariable String userId, @RequestParam String q) {
        return Response.<List<UrlResponse>>builder()
                .success(true)
                .data(urlService.searchUrlsWithinUserId(userId, q))
                .build();
    }

    @PutMapping("/{hash}")
    public Response<UrlResponse> updateOneByHashAndUserId(
            @PathVariable String hash,
            @PathVariable String userId,
            @RequestBody @Valid UrlUpdateRequest urlUpdateRequest) {
        return Response.<UrlResponse>builder()
                .success(true)
                .data(urlService.updateOneByHashAndUserId(hash, userId, urlUpdateRequest))
                .build();
    }

    @DeleteMapping("/{hash}")
    public Response<Void> deleteOneByHash(@PathVariable String hash, @PathVariable String userId) {
        urlService.deleteOneByHashAndUserId(hash, userId);

        return Response.<Void>builder().success(true).build();
    }
}
