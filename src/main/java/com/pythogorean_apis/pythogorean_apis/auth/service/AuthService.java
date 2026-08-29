package com.pythogorean_apis.pythogorean_apis.auth.service;

import com.pythogorean_apis.pythogorean_apis.auth.dto.LoginRequest;
import com.pythogorean_apis.pythogorean_apis.auth.dto.RegisterRequest;
import com.pythogorean_apis.pythogorean_apis.auth.entity.Role;
import com.pythogorean_apis.pythogorean_apis.auth.entity.User;
import com.pythogorean_apis.pythogorean_apis.auth.repository.UserRepository;
import com.pythogorean_apis.pythogorean_apis.branchmanagement.BranchEntity;
import com.pythogorean_apis.pythogorean_apis.branchmanagement.BranchRepository;
import com.pythogorean_apis.pythogorean_apis.teachermanagement.TeacherEntity;
import com.pythogorean_apis.pythogorean_apis.teachermanagement.TeacherRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TeacherRepository teacherRepository;
    private final BranchRepository branchRepository;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TeacherRepository teacherRepository,
            BranchRepository branchRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.teacherRepository = teacherRepository;
        this.branchRepository = branchRepository;
    }

    @Transactional
    public User register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        BranchEntity branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.TEACHER);

        User savedUser = userRepository.save(user);

        TeacherEntity teacher = new TeacherEntity();

        teacher.setUser(savedUser);
        teacher.setBranch(branch);

        teacherRepository.save(teacher);

        return savedUser;
    }

    public User authenticate(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid email or password");
        }

        return user;
    }
}