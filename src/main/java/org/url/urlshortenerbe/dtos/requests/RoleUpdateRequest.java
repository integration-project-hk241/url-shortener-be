package org.url.urlshortenerbe.dtos.requests;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleUpdateRequest {
    @Size(min = 4, max = 50, message = "Role name length must be between 4 and 50 characters")
    @NotBlank(message = "Role name cannot be blank")
    private String name;

    private String description;

    @NotEmpty(message = "Permissions are required")
    private List<@Size(min = 4, max = 50, message = "Permission name length must be between 4 and 50") String>
            permissions;
}
