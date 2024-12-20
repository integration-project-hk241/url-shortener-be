package org.url.urlshortenerbe.dtos.responses;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String id;

    private String username;
    private String firstName;
    private String lastName;
    private Set<RoleResponse> roles;
}
