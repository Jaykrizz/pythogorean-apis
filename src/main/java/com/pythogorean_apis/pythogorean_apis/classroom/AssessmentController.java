package com.pythogorean_apis.pythogorean_apis.classroom;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pythogorean_apis.pythogorean_apis.classroom.AssessmentDtos.AssessmentResponse;
import com.pythogorean_apis.pythogorean_apis.classroom.AssessmentDtos.CaptureResponse;
import com.pythogorean_apis.pythogorean_apis.classroom.AssessmentDtos.CreateAssessmentRequest;
import com.pythogorean_apis.pythogorean_apis.classroom.AssessmentDtos.SessionResponse;

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
        return assessmentService.create(request.name(), request.classId(), request.topicId());
    }

    @GetMapping("/{id}")
    public SessionResponse session(@PathVariable Long id) {
        return assessmentService.session(id);
    }

    @PostMapping(value = "/{id}/capture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CaptureResponse capture(@PathVariable Long id, @RequestParam("image") MultipartFile image) {
        return assessmentService.capture(id, image);
    }
}
