package com.pythogorean_apis.pythogorean_apis.student;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/classes/{classId}/students")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<StudentResponse> addStudent(
            @PathVariable Long classId,
            @RequestBody AddStudentRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studentService.addStudentToClass(classId, request));
    }

    @GetMapping
    public List<StudentResponse> getStudents(@PathVariable Long classId) {
        return studentService.getStudentsInClass(classId);
    }
}
