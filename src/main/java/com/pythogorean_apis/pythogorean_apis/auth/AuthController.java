package com.pythogorean_apis.pythogorean_apis.auth;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pythogorean_apis.pythogorean_apis.auth.AuthDtos.LoginRequest;
import com.pythogorean_apis.pythogorean_apis.auth.AuthDtos.LoginResponse;
import com.pythogorean_apis.pythogorean_apis.auth.AuthDtos.RegisterRequest;
import com.pythogorean_apis.pythogorean_apis.auth.AuthDtos.RegisterResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('TEACHER')")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/me")
    public RegisterResponse me() {
        return authService.currentUser();
    }
}
