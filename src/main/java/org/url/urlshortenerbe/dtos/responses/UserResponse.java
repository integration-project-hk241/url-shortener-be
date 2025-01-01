package org.url.urlshortenerbe.dtos.responses;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private String id;

    private String email;
    private String firstName;
    private String lastName;
    private Boolean banned;
    private Set<RoleResponse> roles;
}
