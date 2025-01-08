package org.url.urlshortenerbe.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.url.urlshortenerbe.dtos.requests.CampaignUpdateRequest;
import org.url.urlshortenerbe.dtos.responses.CampaignResponse;
import org.url.urlshortenerbe.dtos.responses.PageResponse;
import org.url.urlshortenerbe.dtos.responses.Response;
import org.url.urlshortenerbe.services.CampaignService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/campaigns")
@RequiredArgsConstructor
public class CampaignController {
    private final CampaignService campaignService;

    // The campaign controller doesn't have post endpoint because all campaigns must be created by manager not admin
    // or anyone else

    // Admin view
    @GetMapping
    public Response<PageResponse<CampaignResponse>> getAll(
            @RequestParam(defaultValue = "1", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "not_deleted", required = false) String type) {
        return Response.<PageResponse<CampaignResponse>>builder()
                .success(true)
                .data(campaignService.getAll(page, size, type))
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

    // Admin view
    @PutMapping("/{campaignId}")
    public Response<CampaignResponse> update(
            @PathVariable String campaignId, @RequestBody @Valid CampaignUpdateRequest campaignUpdateRequest) {
        return Response.<CampaignResponse>builder()
                .success(true)
                .data(campaignService.updateOneById(campaignId, campaignUpdateRequest))
                .build();
    }

    // Admin view
    @DeleteMapping("/{campaignId}")
    public Response<Void> deleteById(@PathVariable String campaignId) {
        campaignService.deleteOneById(campaignId);

        return Response.<Void>builder().success(true).build();
    }
}
