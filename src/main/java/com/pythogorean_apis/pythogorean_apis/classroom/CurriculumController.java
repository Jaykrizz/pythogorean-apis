package com.pythogorean_apis.pythogorean_apis.classroom;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.NameRequest;
import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.QuestionResponse;
import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.SubjectResponse;
import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.TopicResponse;
import com.pythogorean_apis.pythogorean_apis.classroom.CurriculumDtos.CreateQuestionRequest;
import com.pythogorean_apis.pythogorean_apis.classroom.CurriculumDtos.ShowTopicRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('TEACHER')")
public class CurriculumController {

    private final CurriculumService curriculumService;

    public CurriculumController(CurriculumService curriculumService) {
        this.curriculumService = curriculumService;
    }

    @GetMapping("/subjects")
    public List<SubjectResponse> list() {
        return curriculumService.list();
    }

    @PostMapping("/subjects")
    public SubjectResponse createSubject(@Valid @RequestBody NameRequest request) {
        return curriculumService.createSubject(request.name());
    }

    @PostMapping("/subjects/{id}/topics")
    public TopicResponse createTopic(@PathVariable Long id, @Valid @RequestBody NameRequest request) {
        return curriculumService.createTopic(id, request.name());
    }

    @PatchMapping("/topics/{id}")
    public TopicResponse setShown(@PathVariable Long id, @Valid @RequestBody ShowTopicRequest request) {
        return curriculumService.setShown(id, request.shown());
    }

    @PostMapping("/topics/{id}/questions")
    public QuestionResponse createQuestion(@PathVariable Long id, @Valid @RequestBody CreateQuestionRequest request) {
        return curriculumService.createQuestion(id, request);
    }
}
