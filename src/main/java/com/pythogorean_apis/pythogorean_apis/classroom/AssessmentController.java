package com.pythogorean_apis.pythogorean_apis.classroom;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pythogorean_apis.pythogorean_apis.classroom.AssessmentDtos.AssessmentResponse;
import com.pythogorean_apis.pythogorean_apis.classroom.AssessmentDtos.CreateAssessmentRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/assessments")
@PreAuthorize("hasRole('TEACHER')")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping
    public List<AssessmentResponse> list() {
        return assessmentService.list();
    }

    @PostMapping
    public AssessmentResponse create(@Valid @RequestBody CreateAssessmentRequest request) {
        return assessmentService.create(request.name(), request.topicId());
    }
}
