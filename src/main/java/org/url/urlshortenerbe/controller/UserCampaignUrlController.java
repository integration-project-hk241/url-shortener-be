package org.url.urlshortenerbe.controller;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

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
@RequestMapping("${api.prefix}/users/{userId}/campaigns/{campaignId}/urls")
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
            @RequestParam(defaultValue = "1", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "not_deleted", required = false) String type) {
        return Response.<PageResponse<UrlResponse>>builder()
                .success(true)
                .data(urlService.getAllByCampaignIdAndUserId(campaignId, userId, page, size, type))
                .build();
    }

    @GetMapping("/{hash}")
    public Response<UrlResponse> getOneByIdAndCampaignIdAndUserId(
            @PathVariable("userId") String userId,
            @PathVariable("campaignId") String campaignId,
            @PathVariable("hash") String hash) {
        return Response.<UrlResponse>builder()
                .success(true)
                .data(urlService.getOneByIdAndCampaignIdAndUserId(hash, campaignId, userId))
                .build();
    }

    @PutMapping("/{hash}")
    public Response<UrlResponse> updateOneByIdAndCampaignIdAndUserId(
            @PathVariable("userId") String userId,
            @PathVariable("campaignId") String campaignId,
            @PathVariable("hash") String hash,
            @RequestBody @Valid UrlUpdateRequest urlUpdateRequest) {
        return Response.<UrlResponse>builder()
                .success(true)
                .data(urlService.updateOneByHashAndCampaignIdAndUserId(hash, campaignId, userId, urlUpdateRequest))
                .build();
    }

    @DeleteMapping("/{hash}")
    public Response<UrlResponse> deleteOneByIdAndCampaignIdAndUserId(
            @PathVariable String hash, @PathVariable String userId, @PathVariable String campaignId) {
        urlService.deleteOneByHashAndCampaignIdAndUserId(hash, campaignId, userId);

        return Response.<UrlResponse>builder().success(true).build();
    }

    @GetMapping("/stats")
    public Response<Map<Object, Object>> getMostClickedUrlsByCampaign(
            @PathVariable String userId, @PathVariable String campaignId) {
        return urlService.getMostClickedUrlsByCampaign(campaignId, userId);
    }
}
