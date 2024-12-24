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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/urls")
@RequiredArgsConstructor
public class UrlController {
    private final UrlService urlService;

    @PostMapping
    public Response<UrlResponse> create(@RequestBody @Valid UrlCreationRequest urlCreationRequest)
            throws NoSuchAlgorithmException {
        log.error(urlCreationRequest.toString());
        return Response.<UrlResponse>builder()
                .success(true)
                .data(urlService.create(urlCreationRequest))
                .build();
    }

    @GetMapping
    public Response<PageResponse<UrlResponse>> getAll(
            @RequestParam(defaultValue = "1", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size) {
        return Response.<PageResponse<UrlResponse>>builder()
                .success(true)
                .data(urlService.getAll(page, size))
                .build();
    }

    @GetMapping("/{shortUrl}")
    public Response<UrlResponse> get(@PathVariable String shortUrl) {
        return Response.<UrlResponse>builder()
                .success(true)
                .data(urlService.getOne(shortUrl))
                .build();
    }

    @PutMapping("/{shortUrl}")
    public Response<UrlResponse> update(
            @PathVariable String shortUrl, @RequestBody @Valid UrlUpdateRequest urlUpdateRequest) {
        return Response.<UrlResponse>builder()
                .success(true)
                .data(urlService.update(shortUrl, urlUpdateRequest))
                .build();
    }

    @DeleteMapping("/{shortUrl}")
    public Response<Void> delete(@PathVariable String shortUrl) {
        urlService.delete(shortUrl);

        return Response.<Void>builder().success(true).build();
    }
}
