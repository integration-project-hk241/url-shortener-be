package org.url.urlshortenerbe.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {
    private Boolean success;
    private ErrorResponse error;
    private T data;
}
