package org.url.urlshortenerbe.dtos.responses;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PageResponse<T> {
    private Long records;
    private List<T> items;
    private int page;
    private int totalPages;
}
