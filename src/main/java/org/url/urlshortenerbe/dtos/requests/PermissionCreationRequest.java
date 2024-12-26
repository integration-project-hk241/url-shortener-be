package org.url.urlshortenerbe.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionCreationRequest {
    @Size(min = 4, max = 50, message = "Permission name length must be between 4 and 50 characters")
    @NotBlank(message = "Permission name cannot be blank")
    String name;

    String description;
}
