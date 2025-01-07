package org.url.urlshortenerbe.dtos.responses;

import java.util.Date;

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
public class UrlResponse {
    private String longUrl;

    private Date createdAt;

    private Date expiresAt;

    private String userId;

    private String hash;

    private String campaignId;

    private Boolean deleted;

    private Integer clickCount;
}
