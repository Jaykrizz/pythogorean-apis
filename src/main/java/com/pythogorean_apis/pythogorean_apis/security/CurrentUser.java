package com.pythogorean_apis.pythogorean_apis.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.pythogorean_apis.pythogorean_apis.domain.AppUser;
import com.pythogorean_apis.pythogorean_apis.domain.Role;

@Component
public class CurrentUser {

    public AppUser require() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof AppUserDetails details) {
            return details.user();
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in required");
    }

    public AppUser requireTeacher() {
        AppUser user = require();

        if (user.getRole() != Role.TEACHER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a teacher can do that");
        }

        return user;
    }
}
