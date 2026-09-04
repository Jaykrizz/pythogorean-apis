package com.pythogorean_apis.pythogorean_apis.student;

import com.pythogorean_apis.pythogorean_apis.auth.entity.Role;
import com.pythogorean_apis.pythogorean_apis.auth.entity.User;
import com.pythogorean_apis.pythogorean_apis.auth.repository.UserRepository;
import com.pythogorean_apis.pythogorean_apis.classmanagement.ClassEntity;
import com.pythogorean_apis.pythogorean_apis.classmanagement.ClassService;
import com.pythogorean_apis.pythogorean_apis.common.exception.BadRequestException;
import com.pythogorean_apis.pythogorean_apis.common.exception.ConflictException;
import com.pythogorean_apis.pythogorean_apis.common.exception.NotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class StudentService {

    private static final int FIRST_ARUCO_MARKER_ID = 1;

    private final StudentRepository studentRepository;
    private final ArucoCardRepository arucoCardRepository;
    private final UserRepository userRepository;
    private final ClassService classService;
    private final StudentCardService studentCardService;
    private final PasswordEncoder passwordEncoder;

    public StudentService(
            StudentRepository studentRepository,
            ArucoCardRepository arucoCardRepository,
            UserRepository userRepository,
            ClassService classService,
            StudentCardService studentCardService,
            PasswordEncoder passwordEncoder) {

        this.studentRepository = studentRepository;
        this.arucoCardRepository = arucoCardRepository;
        this.userRepository = userRepository;
        this.classService = classService;
        this.studentCardService = studentCardService;
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
        student.setArucoMarkerId(nextArucoMarkerId());

        StudentEntity saved = studentRepository.save(student);

        return StudentResponse.from(saved, studentCardService.tryGenerateFor(saved));
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsInClass(Long classId) {

        ClassEntity classEntity = classService.getOwnedClass(classId);

        Set<Long> studentsWithCards = arucoCardRepository.findStudentIdsWithCardInClass(classId);

        return studentRepository.findByClassEntityOrderByArucoMarkerIdAsc(classEntity).stream()
                .map(student -> StudentResponse.from(student, studentsWithCards.contains(student.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ArucoCardEntity getCard(Long studentId) {

        StudentEntity student = getOwnedStudent(studentId);

        return studentCardService.findFor(student.getId())
                .orElseThrow(() -> new NotFoundException(
                        "No card has been generated for this student yet"));
    }

    @Transactional
    public StudentResponse regenerateCard(Long studentId) {

        StudentEntity student = getOwnedStudent(studentId);

        if (student.getArucoMarkerId() == null) {
            student.setArucoMarkerId(nextArucoMarkerId());
            studentRepository.save(student);
        }

        studentCardService.generateFor(student);

        return StudentResponse.from(student, true);
    }

    private StudentEntity getOwnedStudent(Long studentId) {

        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found"));

        classService.getOwnedClass(student.getClassEntity().getId());

        return student;
    }

    private int nextArucoMarkerId() {

        Integer highest = studentRepository.findHighestArucoMarkerId();

        return highest == null ? FIRST_ARUCO_MARKER_ID : highest + 1;
    }

    private String require(String value, String message) {

        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }

        return value.trim();
    }
}
