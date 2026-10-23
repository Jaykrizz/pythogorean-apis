package com.pythogorean_apis.pythogorean_apis.seed;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.pythogorean_apis.pythogorean_apis.domain.AppUser;
import com.pythogorean_apis.pythogorean_apis.domain.Branch;
import com.pythogorean_apis.pythogorean_apis.domain.Role;
import com.pythogorean_apis.pythogorean_apis.repository.AppUserRepository;
import com.pythogorean_apis.pythogorean_apis.repository.BranchRepository;

@Configuration
public class DataSeeder {

    @Bean
    ApplicationRunner seedTeacher(
            BranchRepository branches,
            AppUserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.branch-name}") String branchName,
            @Value("${app.seed.teacher-name}") String teacherName,
            @Value("${app.seed.teacher-email}") String teacherEmail,
            @Value("${app.seed.teacher-password}") String teacherPassword) {
        return args -> {
            if (users.existsByEmailIgnoreCase(teacherEmail)) {
                return;
            }

            Branch branch = branches.findByName(branchName).orElseGet(() -> branches.save(new Branch(branchName)));
            users.save(new AppUser(
                    teacherName,
                    teacherEmail.toLowerCase(),
                    passwordEncoder.encode(teacherPassword),
                    Role.TEACHER,
                    branch));
        };
    }
}
