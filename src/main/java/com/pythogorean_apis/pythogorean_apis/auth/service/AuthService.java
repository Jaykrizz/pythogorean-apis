package com.pythogorean_apis.pythogorean_apis.auth.service;

import com.pythogorean_apis.pythogorean_apis.auth.dto.LoginRequest;
import com.pythogorean_apis.pythogorean_apis.auth.dto.RegisterRequest;
import com.pythogorean_apis.pythogorean_apis.auth.entity.User;
import com.pythogorean_apis.pythogorean_apis.auth.repository.UserRepository;
import com.pythogorean_apis.pythogorean_apis.branchmanagement.BranchEntity;
import com.pythogorean_apis.pythogorean_apis.branchmanagement.BranchRepository;
import com.pythogorean_apis.pythogorean_apis.classmanagement.ClassEntity;
import com.pythogorean_apis.pythogorean_apis.classmanagement.ClassRepository;
import com.pythogorean_apis.pythogorean_apis.common.exception.BadRequestException;
import com.pythogorean_apis.pythogorean_apis.common.exception.ConflictException;
import com.pythogorean_apis.pythogorean_apis.common.exception.NotFoundException;
import com.pythogorean_apis.pythogorean_apis.common.exception.UnauthorizedException;
import com.pythogorean_apis.pythogorean_apis.parentmanagement.ParentEntity;
import com.pythogorean_apis.pythogorean_apis.parentmanagement.ParentRepository;
import com.pythogorean_apis.pythogorean_apis.student.StudentEntity;
import com.pythogorean_apis.pythogorean_apis.student.StudentRepository;
import com.pythogorean_apis.pythogorean_apis.teachermanagement.TeacherEntity;
import com.pythogorean_apis.pythogorean_apis.teachermanagement.TeacherRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final BranchRepository branchRepository;
    private final ClassRepository classRepository;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TeacherRepository teacherRepository,
            StudentRepository studentRepository,
            ParentRepository parentRepository,
            BranchRepository branchRepository,
            ClassRepository classRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.parentRepository = parentRepository;
        this.branchRepository = branchRepository;
        this.classRepository = classRepository;
    }

    @Transactional
    public User register(RegisterRequest request) {

        if (request.getRole() == null) {
            throw new BadRequestException("Role is required");
        }

        if (request.getBranchId() == null) {
            throw new BadRequestException("Branch is required");
        }

        String name = require(request.getName(), "Name is required");
        String email = require(request.getEmail(), "Email is required");
        String password = require(request.getPassword(), "Password is required");

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already registered");
        }

        BranchEntity branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new NotFoundException("Branch not found"));

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        switch (request.getRole()) {

            case TEACHER -> saveUserAsTeacher(savedUser, branch);

            case PARENT -> saveUserAsParent(
                    savedUser,
                    branch,
                    request.getClassId(),
                    request.getStudentIds());

            case STUDENT -> throw new BadRequestException(
                    "Students are added to a class by their teacher, not through registration");

            default -> throw new BadRequestException("Unsupported role");
        }

        return savedUser;
    }

    public User authenticate(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return user;
    }

    private String require(String value, String message) {

        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }

        return value.trim();
    }

    private void saveUserAsTeacher(User user, BranchEntity branch) {

        TeacherEntity teacher = new TeacherEntity();
        teacher.setUser(user);
        teacher.setBranch(branch);

        teacherRepository.save(teacher);
    }

    private void saveUserAsParent(
            User user,
            BranchEntity branch,
            Long classId,
            List<Long> studentIds) {

        if (classId == null) {
            throw new BadRequestException("Class is required");
        }

        if (studentIds == null || studentIds.isEmpty()) {
            throw new BadRequestException("At least one student is required");
        }

        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new NotFoundException("Class not found"));

        if (!classEntity.getBranch().getId().equals(branch.getId())) {
            throw new BadRequestException("Class does not belong to selected branch");
        }

        List<StudentEntity> students = studentRepository.findAllById(studentIds);

        if (students.size() != studentIds.size()) {
            throw new NotFoundException("One or more students not found");
        }

        for (StudentEntity student : students) {

            if (!student.getClassEntity().getId().equals(classId)) {
                throw new BadRequestException("Student does not belong to selected class");
            }

            if (student.getParent() != null) {
                throw new ConflictException("Student already has a parent");
            }
        }

        ParentEntity parent = new ParentEntity();
        parent.setUser(user);
        parent.setStudents(students);

        parentRepository.save(parent);

        for (StudentEntity student : students) {
            student.setParent(parent);
        }

        studentRepository.saveAll(students);
    }
}
