package org.url.urlshortenerbe.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.url.urlshortenerbe.dtos.requests.CampaignCreationRequest;
import org.url.urlshortenerbe.dtos.requests.CampaignUpdateRequest;
import org.url.urlshortenerbe.dtos.responses.CampaignResponse;
import org.url.urlshortenerbe.entities.Campaign;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CampaignMapper {
    Campaign toCampaign(CampaignCreationRequest campaignCreationRequest);

    @Mapping(target = "userId", source = "user.id")
    CampaignResponse toCampaignResponse(Campaign campaign);

    void updateCampaign(@MappingTarget Campaign campaign, CampaignUpdateRequest campaignUpdateRequest);
}
