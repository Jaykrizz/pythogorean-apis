package com.pythogorean_apis.pythogorean_apis.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.pythogorean_apis.pythogorean_apis.auth.AuthDtos.LoginRequest;
import com.pythogorean_apis.pythogorean_apis.auth.AuthDtos.LoginResponse;
import com.pythogorean_apis.pythogorean_apis.auth.AuthDtos.RegisterRequest;
import com.pythogorean_apis.pythogorean_apis.auth.AuthDtos.RegisterResponse;
import com.pythogorean_apis.pythogorean_apis.classroom.EnrollmentService;
import com.pythogorean_apis.pythogorean_apis.domain.AppUser;
import com.pythogorean_apis.pythogorean_apis.domain.Role;
import com.pythogorean_apis.pythogorean_apis.repository.AppUserRepository;
import com.pythogorean_apis.pythogorean_apis.security.CurrentUser;
import com.pythogorean_apis.pythogorean_apis.security.JwtService;

@Service
public class AuthService {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUser currentUser;
    private final EnrollmentService enrollmentService;

    public AuthService(
            AppUserRepository users,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            CurrentUser currentUser,
            EnrollmentService enrollmentService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.currentUser = currentUser;
        this.enrollmentService = enrollmentService;
    }

    public LoginResponse login(LoginRequest request) {
        AppUser user = users.findByEmailIgnoreCase(normalize(request.email()))
                .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect"));

        return new LoginResponse(jwtService.createToken(user), user.getName(), user.getEmail(), user.getRole());
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = normalize(request.email());

        if (users.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        if (request.classId() != null && request.role() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only a student can be added to a class");
        }

        AppUser teacher = currentUser.requireTeacher();
        AppUser created = users.save(new AppUser(
                request.name().trim(),
                email,
                passwordEncoder.encode(request.password()),
                request.role(),
                teacher.getBranch()));

        Integer markerId = null;
        if (request.classId() != null) {
            markerId = enrollmentService.enroll(teacher, request.classId(), created).getMarkerId();
        }

        return new RegisterResponse(created.getId(), created.getName(), created.getEmail(), created.getRole(), markerId);
    }

    public RegisterResponse currentUser() {
        AppUser user = currentUser.require();
        return new RegisterResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), null);
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
