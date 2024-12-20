package org.url.urlshortenerbe.configs;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.url.urlshortenerbe.dtos.responses.ErrorResponse;
import org.url.urlshortenerbe.dtos.responses.Response;
import org.url.urlshortenerbe.exceptions.ErrorCode;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Set response status and content type
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
        response.setContentType("application/json");

        // Create the custom error response
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        Response<Void> customResponse =
                Response.<Void>builder().success(false).error(errorResponse).build();

        // Write the response as JSON
        response.getWriter().write(objectMapper.writeValueAsString(customResponse));
    }
}
