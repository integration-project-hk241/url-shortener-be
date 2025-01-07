package org.url.urlshortenerbe.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.url.urlshortenerbe.dtos.requests.UserCreationRequest;
import org.url.urlshortenerbe.dtos.requests.UserUpdateRequest;
import org.url.urlshortenerbe.dtos.responses.UserResponse;
import org.url.urlshortenerbe.entities.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User toUser(UserCreationRequest userCreationRequest);

    @Mapping(target = "roles", ignore = true)
    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest userUpdateRequest);
}
