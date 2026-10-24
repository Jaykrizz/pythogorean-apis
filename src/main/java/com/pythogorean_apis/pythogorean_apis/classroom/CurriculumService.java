package com.pythogorean_apis.pythogorean_apis.classroom;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.QuestionResponse;
import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.SubjectResponse;
import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.TopicResponse;
import com.pythogorean_apis.pythogorean_apis.classroom.CurriculumDtos.CreateQuestionRequest;
import com.pythogorean_apis.pythogorean_apis.domain.AppUser;
import com.pythogorean_apis.pythogorean_apis.domain.Question;
import com.pythogorean_apis.pythogorean_apis.domain.Subject;
import com.pythogorean_apis.pythogorean_apis.domain.Topic;
import com.pythogorean_apis.pythogorean_apis.repository.QuestionRepository;
import com.pythogorean_apis.pythogorean_apis.repository.SubjectRepository;
import com.pythogorean_apis.pythogorean_apis.repository.TopicRepository;
import com.pythogorean_apis.pythogorean_apis.security.CurrentUser;

@Service
public class CurriculumService {

    private final CurrentUser currentUser;
    private final SubjectRepository subjects;
    private final TopicRepository topics;
    private final QuestionRepository questions;

    public CurriculumService(
            CurrentUser currentUser,
            SubjectRepository subjects,
            TopicRepository topics,
            QuestionRepository questions) {
        this.currentUser = currentUser;
        this.subjects = subjects;
        this.topics = topics;
        this.questions = questions;
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> list() {
        AppUser teacher = currentUser.requireTeacher();
        return subjects.findByBranch_IdOrderByNameAsc(teacher.getBranch().getId()).stream()
                .map(this::toSubject)
                .toList();
    }

    @Transactional
    public SubjectResponse createSubject(String name) {
        AppUser teacher = currentUser.requireTeacher();
        String trimmed = name.trim();

        if (subjects.existsByBranch_IdAndNameIgnoreCase(teacher.getBranch().getId(), trimmed)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This subject already exists");
        }

        Subject created = subjects.save(new Subject(teacher.getBranch(), trimmed));
        return toSubject(created);
    }

    @Transactional
    public TopicResponse createTopic(Long subjectId, String name) {
        Subject subject = subjectForTeacher(subjectId);
        String trimmed = name.trim();

        if (topics.existsBySubjectIdAndNameIgnoreCase(subject.getId(), trimmed)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This subject already has that topic");
        }

        Topic created = topics.save(new Topic(subject, trimmed));
        return toTopic(created);
    }

    @Transactional
    public TopicResponse setShown(Long topicId, boolean shown) {
        Topic topic = topicForTeacher(topicId);
        topic.setShown(shown);
        return toTopic(topic);
    }

    @Transactional
    public QuestionResponse createQuestion(Long topicId, CreateQuestionRequest request) {
        Topic topic = topicForTeacher(topicId);
        Question created = questions.save(new Question(
                topic,
                request.prompt().trim(),
                request.optionA().trim(),
                request.optionB().trim(),
                request.optionC().trim(),
                request.optionD().trim(),
                request.correctLetter(),
                request.marks(),
                questions.countByTopicId(topic.getId()) + 1));
        return toQuestion(created);
    }

    @Transactional(readOnly = true)
    public SubjectResponse toSubject(Subject subject) {
        List<TopicResponse> topicResponses = topics.findBySubjectIdOrderByNameAsc(subject.getId()).stream()
                .map(this::toTopic)
                .toList();
        return new SubjectResponse(subject.getId(), subject.getName(), topicResponses);
    }

    private TopicResponse toTopic(Topic topic) {
        List<QuestionResponse> questionResponses = questions.findByTopicIdOrderBySortOrderAscIdAsc(topic.getId()).stream()
                .map(CurriculumService::toQuestion)
                .toList();
        return new TopicResponse(topic.getId(), topic.getName(), topic.isShown(), questionResponses);
    }

    private Subject subjectForTeacher(Long subjectId) {
        AppUser teacher = currentUser.requireTeacher();
        return subjects.findByIdAndBranch_Id(subjectId, teacher.getBranch().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));
    }

    private Topic topicForTeacher(Long topicId) {
        Topic topic = topics.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));
        subjectForTeacher(topic.getSubject().getId());
        return topic;
    }

    private static QuestionResponse toQuestion(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getPrompt(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD(),
                question.getCorrectLetter(),
                question.getMarks());
    }
}
