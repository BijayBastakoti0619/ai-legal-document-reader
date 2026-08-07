package com.aidocumentreader.backend.auth.controller;

import com.aidocumentreader.backend.auth.dto.RegisterRequest;
import com.aidocumentreader.backend.auth.dto.RegisterResponse;
import com.aidocumentreader.backend.auth.service.RegistrationService;
import com.aidocumentreader.backend.auth.dto.LoginRequest;
import com.aidocumentreader.backend.auth.dto.LoginResponse;
import com.aidocumentreader.backend.auth.service.LoginService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegistrationService registrationService;

    private final LoginService loginService;

    public AuthController(RegistrationService registrationService, LoginService loginService) {
        this.registrationService = registrationService;
        this.loginService = loginService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return registrationService.register(request);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return loginService.login(request);
    }
}