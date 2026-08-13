package com.aidocumentreader.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Email is required.")
        @Email(message = "Enter a valid email address.")
        @Size(max = 320, message = "Email must not exceed 320 characters.")
        String email,

        @NotBlank(message = "Password is required.")
        @Size(
                min = 8,
                max = 72,
                message = "Password must contain between 8 and 72 characters."
        )
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).+$",
                message = "Password must include uppercase, lowercase, number, and special character."
        )
        String password,

        @NotBlank(message = "Display name is required.")
        @Size(
                min = 2,
                max = 100,
                message = "Display name must contain between 2 and 100 characters."
        )
        @Pattern(
                regexp = "^[\\p{L}\\p{M}][\\p{L}\\p{M} .'-]*$",
                message = "Display name contains invalid characters."
        )
        String displayName
) {
}
