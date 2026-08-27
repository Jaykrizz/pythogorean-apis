package com.pythogorean_apis.pythogorean_apis.auth.controller;

import com.pythogorean_apis.pythogorean_apis.auth.dto.LoginRequest;
import com.pythogorean_apis.pythogorean_apis.auth.dto.LoginResponse;
import com.pythogorean_apis.pythogorean_apis.auth.dto.RegisterRequest;
import com.pythogorean_apis.pythogorean_apis.auth.entity.User;
import com.pythogorean_apis.pythogorean_apis.auth.service.AuthService;
import com.pythogorean_apis.pythogorean_apis.auth.service.JwtService;
import com.pythogorean_apis.pythogorean_apis.auth.dto.RegisterResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(
            AuthService authService,
            JwtService jwtService) {

        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@RequestBody RegisterRequest requestBody) {
        User registeredUser = authService.register(requestBody);

        RegisterResponse data = new RegisterResponse(
                registeredUser.getId(),
                registeredUser.getName(),
                registeredUser.getEmail(),
                registeredUser.getRole().name());

        return ResponseEntity.ok(data);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest requestBody) {

        User user = authService.authenticate(requestBody);

        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(
                new LoginResponse(token));
    }
}