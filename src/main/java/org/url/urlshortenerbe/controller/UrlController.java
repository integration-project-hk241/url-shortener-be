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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/urls")
@RequiredArgsConstructor
public class UrlController {
    private final UrlService urlService;

    // Create url for guest
    @PostMapping
    public Response<UrlResponse> create(@RequestBody @Valid UrlCreationRequest urlCreationRequest)
            throws NoSuchAlgorithmException {
        log.error(urlCreationRequest.toString());
        return Response.<UrlResponse>builder()
                .success(true)
                .data(urlService.createForGuest(urlCreationRequest))
                .build();
    }

    // Get all urls for admin
    @GetMapping
    public Response<PageResponse<UrlResponse>> getAll(
            @RequestParam(defaultValue = "1", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "not_deleted", required = false) String type) {
        return Response.<PageResponse<UrlResponse>>builder()
                .success(true)
                .data(urlService.getAll(page, size, type))
                .build();
    }

    // Get an url for admin view
    @GetMapping("/{hash}")
    public Response<UrlResponse> get(@PathVariable String hash) {
        return Response.<UrlResponse>builder()
                .success(true)
                .data(urlService.getOne(hash))
                .build();
    }

    @GetMapping("/search")
    public Response<List<UrlResponse>> searchUrl(@RequestParam String q) {
        return Response.<List<UrlResponse>>builder()
                .success(true)
                .data(urlService.searchForUrl(q))
                .build();
    }

    // Update an url for admin view
    @PutMapping("/{hash}")
    public Response<UrlResponse> update(
            @PathVariable String hash, @RequestBody @Valid UrlUpdateRequest urlUpdateRequest) {
        return Response.<UrlResponse>builder()
                .success(true)
                .data(urlService.update(hash, urlUpdateRequest))
                .build();
    }

    // Delete an url for admin view
    @DeleteMapping("/{hash}")
    public Response<Void> delete(@PathVariable String hash) {
        urlService.delete(hash);

        return Response.<Void>builder().success(true).build();
    }
}
