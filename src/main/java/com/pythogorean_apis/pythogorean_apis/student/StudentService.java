package com.pythogorean_apis.pythogorean_apis.student;

import com.pythogorean_apis.pythogorean_apis.auth.entity.Role;
import com.pythogorean_apis.pythogorean_apis.auth.entity.User;
import com.pythogorean_apis.pythogorean_apis.auth.repository.UserRepository;
import com.pythogorean_apis.pythogorean_apis.classmanagement.ClassEntity;
import com.pythogorean_apis.pythogorean_apis.classmanagement.ClassService;
import com.pythogorean_apis.pythogorean_apis.common.exception.BadRequestException;
import com.pythogorean_apis.pythogorean_apis.common.exception.ConflictException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final ClassService classService;
    private final PasswordEncoder passwordEncoder;

    public StudentService(
            StudentRepository studentRepository,
            UserRepository userRepository,
            ClassService classService,
            PasswordEncoder passwordEncoder) {

        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.classService = classService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public StudentResponse addStudentToClass(Long classId, AddStudentRequest request) {

        ClassEntity classEntity = classService.getOwnedClass(classId);

        String name = require(request.getName(), "Student name is required");
        String email = require(request.getEmail(), "Student email is required");

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already registered");
        }

        String rawPassword = request.getPassword() == null || request.getPassword().isBlank()
                ? UUID.randomUUID().toString()
                : request.getPassword();

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.STUDENT);

        StudentEntity student = new StudentEntity();
        student.setUser(userRepository.save(user));
        student.setClassEntity(classEntity);

        return StudentResponse.from(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsInClass(Long classId) {

        ClassEntity classEntity = classService.getOwnedClass(classId);

        return studentRepository.findByClassEntityOrderByIdAsc(classEntity).stream()
                .map(StudentResponse::from)
                .toList();
    }

    private String require(String value, String message) {

        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }

        return value.trim();
    }
}
