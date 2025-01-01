package org.url.urlshortenerbe.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    // CODING ERROR CODE
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),

    // LOGIN/ACCESS ERROR CODES
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1002, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1002, "Unauthorized", HttpStatus.FORBIDDEN),
    USER_BANNED(1002, "Sorry but you are banned due to policy violation", HttpStatus.UNAUTHORIZED),

    // REGISTER ERROR CODES
    USERNAME_INVALID(1002, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1002, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),

    // AUTHORIZATION ERROR CODES
    USER_NOTFOUND(1003, "User not found", HttpStatus.NOT_FOUND),
    PERMISSION_NOTFOUND(1003, "Permission not found", HttpStatus.NOT_FOUND),
    ROLE_NOTFOUND(1003, "Role not found", HttpStatus.BAD_REQUEST),

    // SERVICE ERROR CODES
    ALIAS_EXISTED(1004, "Alias already existed", HttpStatus.CONFLICT),
    URL_NOTFOUND(1004, "URL not found", HttpStatus.BAD_REQUEST),
    ALIAS_INVALID(1004, "Invalid alias", HttpStatus.BAD_REQUEST),
    CAMPAIGN_NOTFOUND(1004, "Campaign not found", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(final int code, final String message, final HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
