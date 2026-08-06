package com.aidocumentreader.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest (
    @NotBlank(message="Email cannot be blank")
    @Email(message="Invalid email format")
    String email,

    @NotBlank(message="Password cannot be blank")
    String password
){}
