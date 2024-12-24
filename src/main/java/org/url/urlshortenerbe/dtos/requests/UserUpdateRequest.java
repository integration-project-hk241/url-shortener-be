package org.url.urlshortenerbe.dtos.requests;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {
    @Email(message = "Email is not valid", regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    @NotBlank(message = "Password cannot be blank")
    private String password;

    @Size(min = 3, message = "Firstname must be at least 8 characters")
    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    @Size(min = 3, message = "Lastname must be at least 8 characters")
    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    @NotEmpty(message = "Roles are required")
    private List<@Size(min = 4, max = 50, message = "Role name length must be between 4 and 50") String> roles;
}