package com.pythogorean_apis.pythogorean_apis.classroom;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.pythogorean_apis.pythogorean_apis.classroom.AssessmentDtos.AssessmentResponse;
import com.pythogorean_apis.pythogorean_apis.domain.AppUser;
import com.pythogorean_apis.pythogorean_apis.domain.Assessment;
import com.pythogorean_apis.pythogorean_apis.domain.ClassRoom;
import com.pythogorean_apis.pythogorean_apis.domain.Subject;
import com.pythogorean_apis.pythogorean_apis.domain.Topic;
import com.pythogorean_apis.pythogorean_apis.repository.AssessmentRepository;
import com.pythogorean_apis.pythogorean_apis.repository.TopicRepository;
import com.pythogorean_apis.pythogorean_apis.security.CurrentUser;

@Service
public class AssessmentService {

    private final CurrentUser currentUser;
    private final TopicRepository topics;
    private final AssessmentRepository assessments;

    public AssessmentService(CurrentUser currentUser, TopicRepository topics, AssessmentRepository assessments) {
        this.currentUser = currentUser;
        this.topics = topics;
        this.assessments = assessments;
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> list() {
        AppUser teacher = currentUser.requireTeacher();
        return assessments.findByClassRoom_Branch_IdOrderByIdDesc(teacher.getBranch().getId()).stream()
                .map(AssessmentService::toResponse)
                .toList();
    }

    @Transactional
    public AssessmentResponse create(String name, Long topicId) {
        AppUser teacher = currentUser.requireTeacher();
        Topic topic = topics.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));
        Subject subject = topic.getSubject();
        ClassRoom classRoom = subject.getClassRoom();

        if (!classRoom.getBranch().getId().equals(teacher.getBranch().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found");
        }

        String trimmed = name.trim();
        if (assessments.existsByClassRoomIdAndNameIgnoreCase(classRoom.getId(), trimmed)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This class already has an assessment with that name");
        }

        Assessment created = assessments.save(new Assessment(trimmed, classRoom, topic, teacher));
        return toResponse(created);
    }

    private static AssessmentResponse toResponse(Assessment assessment) {
        Subject subject = assessment.getTopic().getSubject();
        return new AssessmentResponse(
                assessment.getId(),
                assessment.getName(),
                assessment.getClassRoom().getName(),
                subject.getName(),
                assessment.getTopic().getName());
    }
}
