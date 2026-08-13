package com.aidocumentreader.backend.user.controller;

import com.aidocumentreader.backend.user.dto.UserProfileResponse;
import com.aidocumentreader.backend.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserProfileResponse getCurrentUser(Principal principal) {
        // principal.getName() extracts the user email from the validated JWT Access Token
        String email = principal.getName();
        return userService.getCurrentUserProfile(email);
    }
}