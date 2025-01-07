package org.url.urlshortenerbe.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.url.urlshortenerbe.dtos.requests.CampaignCreationRequest;
import org.url.urlshortenerbe.dtos.requests.CampaignUpdateRequest;
import org.url.urlshortenerbe.dtos.responses.CampaignResponse;
import org.url.urlshortenerbe.dtos.responses.PageResponse;
import org.url.urlshortenerbe.dtos.responses.Response;
import org.url.urlshortenerbe.services.CampaignService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users/{userId}/campaigns")
@RequiredArgsConstructor
public class UserCampaignController {
    private final CampaignService campaignService;

    // Campaign manager view for creating new campaign
    @PostMapping
    public Response<CampaignResponse> createCampaignWithUserId(
            @PathVariable String userId, @RequestBody @Valid CampaignCreationRequest campaignCreationRequest) {
        return Response.<CampaignResponse>builder()
                .success(true)
                .data(campaignService.createByUserId(userId, campaignCreationRequest))
                .build();
    }

    // Campaign manager view for getting all campaigns
    @GetMapping
    public Response<PageResponse<CampaignResponse>> getCampaignsWithUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "1", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "not_deleted", required = false) String type) {
        return Response.<PageResponse<CampaignResponse>>builder()
                .success(true)
                .data(campaignService.getAllByUserId(userId, page, size, type))
                .build();
    }

    // Campaign manager view for getting campaign by name and user id
    @GetMapping("/search")
    public Response<List<CampaignResponse>> getCampaignWithUserIdAndName(
            @PathVariable String userId, @RequestParam String name) {
        return Response.<List<CampaignResponse>>builder()
                .success(true)
                .data(campaignService.searchCampaignByNameWithinUserId(userId, name))
                .build();
    }

    // Campaign manager view for getting by campaign id and user id
    @GetMapping("/{campaignId}")
    public Response<CampaignResponse> getCampaignWithUserId(
            @PathVariable String userId, @PathVariable String campaignId) {
        return Response.<CampaignResponse>builder()
                .success(true)
                .data(campaignService.getOneByUserIdAndCampaignId(userId, campaignId))
                .build();
    }

    // Campaign manager view for updating by campaign id and user id
    @PutMapping("/{campaignId}")
    public Response<CampaignResponse> updateCampaignWithUserId(
            @PathVariable String userId,
            @PathVariable String campaignId,
            @RequestBody @Valid CampaignUpdateRequest campaignUpdateRequest) {
        return Response.<CampaignResponse>builder()
                .success(true)
                .data(campaignService.updateOneWithIdAndUserId(campaignId, userId, campaignUpdateRequest))
                .build();
    }

    // Campaign manager view for deleting by campaign id and user id
    @DeleteMapping("/{campaignId}")
    public Response<Void> deleteCampaignWithUserId(@PathVariable String userId, @PathVariable String campaignId) {
        campaignService.deleteByIdAndUserId(campaignId, userId);

        return Response.<Void>builder().success(true).build();
    }
}
