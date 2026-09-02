package com.pythogorean_apis.pythogorean_apis.auth.dto;

import com.pythogorean_apis.pythogorean_apis.auth.entity.User;

public record LoginResponse(
        String token,
        String name,
        String email,
        String role) {

    public static LoginResponse of(String token, User user) {
        return new LoginResponse(
                token,
                user.getName(),
                user.getEmail(),
                user.getRole().name());
    }
}
