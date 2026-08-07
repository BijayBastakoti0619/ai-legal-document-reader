package com.aidocumentreader.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest (
    @NotBlank(message="Email cannot be blank")
    @Email(message="Invalid email format")
    @Size(max=320, message="Email must not exceed 320 characters")
    String email,

    @NotBlank(message="Password cannot be blank")
    @Size(max=72, message="Password must not exceed 128 characters")
    String password
){}
