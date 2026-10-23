package com.pythogorean_apis.pythogorean_apis.classroom;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.ClassDetailResponse;
import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.ClassSummary;
import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.PersonOption;
import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.StudentCard;
import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.SubjectResponse;
import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.TopicResponse;
import com.pythogorean_apis.pythogorean_apis.domain.AppUser;
import com.pythogorean_apis.pythogorean_apis.domain.ClassRoom;
import com.pythogorean_apis.pythogorean_apis.domain.Enrollment;
import com.pythogorean_apis.pythogorean_apis.domain.Role;
import com.pythogorean_apis.pythogorean_apis.domain.Subject;
import com.pythogorean_apis.pythogorean_apis.repository.AppUserRepository;
import com.pythogorean_apis.pythogorean_apis.repository.ClassRoomRepository;
import com.pythogorean_apis.pythogorean_apis.repository.EnrollmentRepository;
import com.pythogorean_apis.pythogorean_apis.repository.SubjectRepository;
import com.pythogorean_apis.pythogorean_apis.repository.TopicRepository;
import com.pythogorean_apis.pythogorean_apis.security.CurrentUser;

@Service
public class ClassroomService {

    private final CurrentUser currentUser;
    private final ClassRoomRepository classes;
    private final SubjectRepository subjects;
    private final TopicRepository topics;
    private final EnrollmentRepository enrollments;
    private final EnrollmentService enrollmentService;
    private final AppUserRepository users;
    private final RestClient vision;

    public ClassroomService(
            CurrentUser currentUser,
            ClassRoomRepository classes,
            SubjectRepository subjects,
            TopicRepository topics,
            EnrollmentRepository enrollments,
            EnrollmentService enrollmentService,
            AppUserRepository users,
            RestClient vision) {
        this.currentUser = currentUser;
        this.classes = classes;
        this.subjects = subjects;
        this.topics = topics;
        this.enrollments = enrollments;
        this.enrollmentService = enrollmentService;
        this.users = users;
        this.vision = vision;
    }

    @Transactional(readOnly = true)
    public List<ClassSummary> list() {
        AppUser teacher = currentUser.requireTeacher();
        return classes.findByBranchIdOrderByNameAsc(teacher.getBranch().getId()).stream()
                .map(classRoom -> new ClassSummary(
                        classRoom.getId(),
                        classRoom.getName(),
                        enrollments.findByClassRoomIdOrderByStudent_NameAsc(classRoom.getId()).size(),
                        subjects.findByClassRoomIdOrderByNameAsc(classRoom.getId()).size()))
                .toList();
    }

    @Transactional
    public ClassSummary create(String name) {
        AppUser teacher = currentUser.requireTeacher();
        String trimmed = name.trim();

        if (classes.existsByBranchIdAndNameIgnoreCase(teacher.getBranch().getId(), trimmed)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A class with this name already exists");
        }

        ClassRoom created = classes.save(new ClassRoom(teacher.getBranch(), trimmed, teacher));
        return new ClassSummary(created.getId(), created.getName(), 0, 0);
    }

    @Transactional(readOnly = true)
    public ClassDetailResponse detail(Long classId) {
        ClassRoom classRoom = classForTeacher(classId);
        List<SubjectResponse> subjectResponses = subjects.findByClassRoomIdOrderByNameAsc(classRoom.getId()).stream()
                .map(subject -> new SubjectResponse(
                        subject.getId(),
                        subject.getName(),
                        topics.findBySubjectIdOrderByNameAsc(subject.getId()).stream()
                                .map(topic -> new TopicResponse(topic.getId(), topic.getName()))
                                .toList()))
                .toList();
        List<StudentCard> students = enrollments.findByClassRoomIdOrderByStudent_NameAsc(classRoom.getId()).stream()
                .map(enrollment -> new StudentCard(
                        enrollment.getStudent().getId(),
                        enrollment.getStudent().getName(),
                        enrollment.getStudent().getEmail(),
                        enrollment.getMarkerId()))
                .toList();
        return new ClassDetailResponse(classRoom.getId(), classRoom.getName(), subjectResponses, students);
    }

    @Transactional
    public SubjectResponse addSubject(Long classId, String name) {
        ClassRoom classRoom = classForTeacher(classId);
        String trimmed = name.trim();

        if (subjects.existsByClassRoomIdAndNameIgnoreCase(classRoom.getId(), trimmed)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This class already has that subject");
        }

        Subject created = subjects.save(new Subject(classRoom, trimmed));
        return new SubjectResponse(created.getId(), created.getName(), List.of());
    }

    @Transactional
    public TopicResponse addTopic(Long subjectId, String name) {
        AppUser teacher = currentUser.requireTeacher();
        Subject subject = subjects.findByIdAndClassRoom_Branch_Id(subjectId, teacher.getBranch().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));
        String trimmed = name.trim();

        if (topics.existsBySubjectIdAndNameIgnoreCase(subject.getId(), trimmed)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This subject already has that topic");
        }

        var created = topics.save(new com.pythogorean_apis.pythogorean_apis.domain.Topic(subject, trimmed));
        return new TopicResponse(created.getId(), created.getName());
    }

    @Transactional(readOnly = true)
    public List<PersonOption> availableStudents(Long classId) {
        ClassRoom classRoom = classForTeacher(classId);
        return users.findByBranch_IdAndRoleOrderByNameAsc(classRoom.getBranch().getId(), Role.STUDENT).stream()
                .filter(student -> !enrollments.existsByStudentId(student.getId()))
                .map(student -> new PersonOption(student.getId(), student.getName(), student.getEmail()))
                .toList();
    }

    @Transactional
    public StudentCard enroll(Long classId, Long studentId) {
        AppUser teacher = currentUser.requireTeacher();
        AppUser student = users.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        Enrollment enrollment = enrollmentService.enroll(teacher, classId, student);
        return new StudentCard(student.getId(), student.getName(), student.getEmail(), enrollment.getMarkerId());
    }

    @Transactional(readOnly = true)
    public byte[] cardPng(Long studentId) {
        AppUser teacher = currentUser.requireTeacher();
        Enrollment enrollment = enrollments.findByStudentId(studentId)
                .filter(found -> found.getClassRoom().getBranch().getId().equals(teacher.getBranch().getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "This student has no card yet"));

        String studentName = enrollment.getStudent().getName();
        String label = studentName.length() > 60 ? studentName.substring(0, 60) : studentName;

        try {
            byte[] png = vision.get()
                    .uri(uri -> uri.path("/cards/{markerId}.png")
                            .queryParam("label", label)
                            .queryParam("dictionary", enrollment.getDictionary())
                            .build(enrollment.getMarkerId()))
                    .retrieve()
                    .body(byte[].class);

            if (png == null || png.length == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Vision layer returned an empty card");
            }

            return png;
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Vision layer is not running on port 8000");
        }
    }

    private ClassRoom classForTeacher(Long classId) {
        AppUser teacher = currentUser.requireTeacher();
        return classes.findByIdAndBranchId(classId, teacher.getBranch().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
    }
}
