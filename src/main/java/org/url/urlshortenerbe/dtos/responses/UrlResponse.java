package org.url.urlshortenerbe.dtos.responses;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UrlResponse {
    private String shortUrl;

    private String longUrl;

    private Date createdAt;

    private Date lastVisit;
}
