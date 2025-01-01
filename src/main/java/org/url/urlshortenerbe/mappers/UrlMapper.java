package org.url.urlshortenerbe.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.url.urlshortenerbe.dtos.requests.UrlCreationRequest;
import org.url.urlshortenerbe.dtos.requests.UrlUpdateRequest;
import org.url.urlshortenerbe.dtos.responses.UrlResponse;
import org.url.urlshortenerbe.entities.Url;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UrlMapper {
    Url toUrl(UrlCreationRequest urlCreationRequest);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "campaignId", source = "campaign.id")
    UrlResponse toUrlResponse(Url url);

    void updateUrl(@MappingTarget Url url, UrlUpdateRequest urlUpdateRequest);
}
