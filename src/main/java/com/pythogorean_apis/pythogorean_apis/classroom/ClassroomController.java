package com.pythogorean_apis.pythogorean_apis.classroom;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.ClassDetailResponse;
import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.ClassSummary;
import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.EnrollRequest;
import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.LinkSubjectRequest;
import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.NameRequest;
import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.PersonOption;
import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.StudentCard;
import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.SubjectResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('TEACHER')")
public class ClassroomController {

    private final ClassroomService classroomService;

    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @GetMapping("/classes")
    public List<ClassSummary> list() {
        return classroomService.list();
    }

    @PostMapping("/classes")
    public ClassSummary create(@Valid @RequestBody NameRequest request) {
        return classroomService.create(request.name());
    }

    @GetMapping("/classes/{id}")
    public ClassDetailResponse detail(@PathVariable Long id) {
        return classroomService.detail(id);
    }

    @PostMapping("/classes/{id}/subjects")
    public SubjectResponse linkSubject(@PathVariable Long id, @Valid @RequestBody LinkSubjectRequest request) {
        return classroomService.linkSubject(id, request.subjectId());
    }

    @GetMapping("/classes/{id}/available-subjects")
    public List<PersonOption> availableSubjects(@PathVariable Long id) {
        return classroomService.availableSubjects(id);
    }

    @GetMapping("/classes/{id}/available-students")
    public List<PersonOption> availableStudents(@PathVariable Long id) {
        return classroomService.availableStudents(id);
    }

    @PostMapping("/classes/{id}/students")
    public StudentCard enroll(@PathVariable Long id, @Valid @RequestBody EnrollRequest request) {
        return classroomService.enroll(id, request.studentId());
    }

    @GetMapping("/students/{id}/card")
    public ResponseEntity<byte[]> card(@PathVariable Long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(classroomService.cardPng(id));
    }
}
