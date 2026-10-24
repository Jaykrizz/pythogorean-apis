package com.pythogorean_apis.pythogorean_apis.classroom;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.pythogorean_apis.pythogorean_apis.classroom.AssessmentDtos.AssessmentResponse;
import com.pythogorean_apis.pythogorean_apis.classroom.AssessmentDtos.CapturedAnswer;
import com.pythogorean_apis.pythogorean_apis.classroom.AssessmentDtos.CaptureResponse;
import com.pythogorean_apis.pythogorean_apis.classroom.AssessmentDtos.SessionResponse;
import com.pythogorean_apis.pythogorean_apis.classroom.AssessmentDtos.VisionDetectResponse;
import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.QuestionResponse;
import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.StudentCard;
import com.pythogorean_apis.pythogorean_apis.domain.AppUser;
import com.pythogorean_apis.pythogorean_apis.domain.Assessment;
import com.pythogorean_apis.pythogorean_apis.domain.ClassRoom;
import com.pythogorean_apis.pythogorean_apis.domain.Enrollment;
import com.pythogorean_apis.pythogorean_apis.domain.Question;
import com.pythogorean_apis.pythogorean_apis.domain.Subject;
import com.pythogorean_apis.pythogorean_apis.domain.Topic;
import com.pythogorean_apis.pythogorean_apis.repository.AssessmentRepository;
import com.pythogorean_apis.pythogorean_apis.repository.ClassRoomRepository;
import com.pythogorean_apis.pythogorean_apis.repository.ClassSubjectRepository;
import com.pythogorean_apis.pythogorean_apis.repository.EnrollmentRepository;
import com.pythogorean_apis.pythogorean_apis.repository.QuestionRepository;
import com.pythogorean_apis.pythogorean_apis.repository.TopicRepository;
import com.pythogorean_apis.pythogorean_apis.security.CurrentUser;

@Service
public class AssessmentService {

    private final CurrentUser currentUser;
    private final TopicRepository topics;
    private final ClassRoomRepository classes;
    private final ClassSubjectRepository classSubjects;
    private final AssessmentRepository assessments;
    private final QuestionRepository questions;
    private final EnrollmentRepository enrollments;
    private final String visionBaseUrl;

    public AssessmentService(
            CurrentUser currentUser,
            TopicRepository topics,
            ClassRoomRepository classes,
            ClassSubjectRepository classSubjects,
            AssessmentRepository assessments,
            QuestionRepository questions,
            EnrollmentRepository enrollments,
            @Value("${app.vision.base-url}") String visionBaseUrl) {
        this.currentUser = currentUser;
        this.topics = topics;
        this.classes = classes;
        this.classSubjects = classSubjects;
        this.assessments = assessments;
        this.questions = questions;
        this.enrollments = enrollments;
        this.visionBaseUrl = visionBaseUrl;
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> list() {
        AppUser teacher = currentUser.requireTeacher();
        return assessments.findByClassRoom_Branch_IdOrderByIdDesc(teacher.getBranch().getId()).stream()
                .map(AssessmentService::toResponse)
                .toList();
    }

    @Transactional
    public AssessmentResponse create(String name, Long classId, Long topicId) {
        AppUser teacher = currentUser.requireTeacher();
        ClassRoom classRoom = classes.findByIdAndBranchId(classId, teacher.getBranch().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
        Topic topic = topics.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));
        Subject subject = topic.getSubject();

        if (subject.getBranch() == null || !subject.getBranch().getId().equals(teacher.getBranch().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found");
        }

        if (!classSubjects.existsByClassRoomIdAndSubjectId(classRoom.getId(), subject.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Add this subject to the class first");
        }

        if (!topic.isShown()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check this question set before using it in an assessment");
        }

        String trimmed = name.trim();
        if (assessments.existsByClassRoomIdAndNameIgnoreCase(classRoom.getId(), trimmed)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This class already has an assessment with that name");
        }

        Assessment created = assessments.save(new Assessment(trimmed, classRoom, topic, teacher));
        return toResponse(created);
    }

    @Transactional(readOnly = true)
    public SessionResponse session(Long assessmentId) {
        Assessment assessment = assessmentForTeacher(assessmentId);
        List<QuestionResponse> questionResponses = questions
                .findByTopicIdOrderBySortOrderAscIdAsc(assessment.getTopic().getId()).stream()
                .map(AssessmentService::toQuestion)
                .toList();
        List<StudentCard> students = enrollments
                .findByClassRoomIdOrderByStudent_NameAsc(assessment.getClassRoom().getId()).stream()
                .map(enrollment -> new StudentCard(
                        enrollment.getStudent().getId(),
                        enrollment.getStudent().getName(),
                        enrollment.getStudent().getEmail(),
                        enrollment.getMarkerId()))
                .toList();
        return new SessionResponse(
                assessment.getId(),
                assessment.getName(),
                assessment.getClassRoom().getName(),
                questionResponses,
                students);
    }

    @Transactional(readOnly = true)
    public CaptureResponse capture(Long assessmentId, MultipartFile image) {
        Assessment assessment = assessmentForTeacher(assessmentId);
        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Capture an image first");
        }

        List<Enrollment> enrolled = enrollments.findByClassRoomIdOrderByStudent_NameAsc(
                assessment.getClassRoom().getId());
        String dictionary = enrolled.isEmpty() ? Enrollment.DICTIONARY : enrolled.get(0).getDictionary();
        Map<Integer, String> letters = detect(image, dictionary);

        List<CapturedAnswer> answers = new ArrayList<>();
        for (Enrollment enrollment : enrolled) {
            String letter = letters.get(enrollment.getMarkerId());
            if (letter != null) {
                answers.add(new CapturedAnswer(
                        enrollment.getStudent().getId(),
                        enrollment.getMarkerId(),
                        letter));
            }
        }
        return new CaptureResponse(answers);
    }

    private Map<Integer, String> detect(MultipartFile image, String dictionary) {
        byte[] bytes;
        try {
            bytes = image.getBytes();
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Capture an image first");
        }

        String boundary = "----PythogoreanCapture";
        byte[] payload = multipart(boundary, bytes);

        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(
                    visionBaseUrl + "/detect?dictionary=" + dictionary).toURL().openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.getOutputStream().write(payload);
            int status = connection.getResponseCode();
            byte[] responseBytes = (status >= 400 ? connection.getErrorStream() : connection.getInputStream()).readAllBytes();
            String responseBody = new String(responseBytes, StandardCharsets.UTF_8);
            if (status != 200) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Vision layer could not read that photo");
            }
            Map<Integer, String> letters = new HashMap<>();
            Object answers = JsonParserFactory.getJsonParser().parseMap(responseBody).get("answers");
            if (answers instanceof List<?> found) {
                for (Object item : found) {
                    if (item instanceof Map<?, ?> answer) {
                        letters.putIfAbsent(
                                ((Number) answer.get("markerId")).intValue(),
                                String.valueOf(answer.get("letter")));
                    }
                }
            }
            return letters;
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Vision layer is not running on port 8000");
        }
    }

    private static byte[] multipart(String boundary, byte[] image) {
        try {
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            body.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            body.write(("Content-Disposition: form-data; name=\"image\"; filename=\"capture.jpg\"\r\n").getBytes(StandardCharsets.UTF_8));
            body.write("Content-Type: image/jpeg\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            body.write(image);
            body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            return body.toByteArray();
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Capture an image first");
        }
    }

    private Assessment assessmentForTeacher(Long assessmentId) {
        AppUser teacher = currentUser.requireTeacher();
        Assessment assessment = assessments.findById(assessmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found"));
        if (!assessment.getClassRoom().getBranch().getId().equals(teacher.getBranch().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found");
        }
        return assessment;
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
