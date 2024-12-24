package org.url.urlshortenerbe.dtos.requests;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UrlCreationRequest {
    @NotBlank(message = "Url is required to shorten")
    private String longUrl;

    private String alias;
}
