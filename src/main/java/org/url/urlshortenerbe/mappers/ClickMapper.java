package org.url.urlshortenerbe.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.url.urlshortenerbe.dtos.responses.ClickResponse;
import org.url.urlshortenerbe.entities.Click;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClickMapper {
    ClickResponse toResponse(Click click);
}
