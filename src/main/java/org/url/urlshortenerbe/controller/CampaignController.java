package org.url.urlshortenerbe.controller;

import java.security.NoSuchAlgorithmException;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.url.urlshortenerbe.dtos.requests.UrlCreationRequest;
import org.url.urlshortenerbe.dtos.responses.CampaignResponse;
import org.url.urlshortenerbe.dtos.responses.PageResponse;
import org.url.urlshortenerbe.dtos.responses.Response;
import org.url.urlshortenerbe.dtos.responses.UrlResponse;
import org.url.urlshortenerbe.services.CampaignService;
import org.url.urlshortenerbe.services.UrlService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/campaigns")
@RequiredArgsConstructor
public class CampaignController {
    private final CampaignService campaignService;
    private final UrlService urlService;

    // Admin view
    @GetMapping
    public Response<PageResponse<CampaignResponse>> getAll(
            @RequestParam(defaultValue = "1", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size) {
        return Response.<PageResponse<CampaignResponse>>builder()
                .success(true)
                .data(campaignService.getAll(page, size))
                .build();
    }

    // Admin view
    @GetMapping("/{campaignId}")
    public Response<CampaignResponse> getById(@PathVariable String campaignId) {
        return Response.<CampaignResponse>builder()
                .success(true)
                .data(campaignService.getOneById(campaignId))
                .build();
    }

    /*
     * URL
     * */
    @PostMapping("/{userId}/campaigns/{campaignId}/urls")
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
}
