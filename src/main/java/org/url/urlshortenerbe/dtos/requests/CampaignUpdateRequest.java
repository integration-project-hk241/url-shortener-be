package org.url.urlshortenerbe.dtos.requests;

import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignUpdateRequest {
    @Size(min = 4, max = 50, message = "Campaign name length must be between 4 and 50 characters")
    @NotBlank(message = "Campaign name cannot be blank")
    private String name;

    @NotNull(message = "The end date is required and cannot be blank")
    private Date endDate;

    private String description;
}
