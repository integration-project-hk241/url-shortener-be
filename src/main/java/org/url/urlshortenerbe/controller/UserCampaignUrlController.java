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
@RequestMapping("/users/{userId}/campaigns/{campaignId}/urls")
@RequiredArgsConstructor
public class UserCampaignUrlController {
    private final UrlService urlService;

    @PostMapping
    public Response<UrlResponse> createUrlWithCampaignIdAndUserId(
            @PathVariable String userId,
            @PathVariable String campaignId,
            @RequestBody @Valid UrlCreationRequest urlCreationRequest)
            throws NoSuchAlgorithmException {
        return Response.<UrlResponse>builder()
                .success(true)
                .data(urlService.createWithCampaignIdAndUserId(campaignId, userId, urlCreationRequest))
                .build();
    }

    @GetMapping
    public Response<PageResponse<UrlResponse>> getAllByCampaignIdAndUserId(
            @PathVariable String userId,
            @PathVariable String campaignId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Response.<PageResponse<UrlResponse>>builder()
                .success(true)
                .data(urlService.getAllByCampaignIdAndUserId(campaignId, userId, page, size))
                .build();
    }

    @GetMapping("/{shortUrl}")
    public Response<UrlResponse> getOneByIdAndCampaignIdAndUserId(
            @PathVariable String shortUrl, @PathVariable String userId, @PathVariable String campaignId) {
        return Response.<UrlResponse>builder()
                .success(true)
                .data(urlService.getOneByIdAndCampaignIdAndUserId(shortUrl, campaignId, userId))
                .build();
    }

    @PutMapping("/{shortUrl}")
    public Response<UrlResponse> updateOneByIdAndCampaignIdAndUserId(
            @PathVariable String shortUrl,
            @PathVariable String userId,
            @PathVariable String campaignId,
            @RequestBody @Valid UrlUpdateRequest urlUpdateRequest) {
        return Response.<UrlResponse>builder()
                .success(true)
                .data(urlService.updateOneByShortUrlAndCampaignIdAndUserId(
                        shortUrl, campaignId, userId, urlUpdateRequest))
                .build();
    }

    @DeleteMapping("/{shortUrl}")
    public Response<UrlResponse> deleteOneByIdAndCampaignIdAndUserId(
            @PathVariable String shortUrl, @PathVariable String userId, @PathVariable String campaignId) {
        urlService.deleteOneByShortUrlAndCampaignIdAndUserId(shortUrl, campaignId, userId);

        return Response.<UrlResponse>builder().success(true).build();
    }
}
