package com.pythogorean_apis.pythogorean_apis.classroom;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.pythogorean_apis.pythogorean_apis.domain.AppUser;
import com.pythogorean_apis.pythogorean_apis.domain.ClassRoom;
import com.pythogorean_apis.pythogorean_apis.domain.Enrollment;
import com.pythogorean_apis.pythogorean_apis.domain.Role;
import com.pythogorean_apis.pythogorean_apis.repository.ClassRoomRepository;
import com.pythogorean_apis.pythogorean_apis.repository.EnrollmentRepository;

@Service
public class EnrollmentService {

    private final ClassRoomRepository classes;
    private final EnrollmentRepository enrollments;

    public EnrollmentService(ClassRoomRepository classes, EnrollmentRepository enrollments) {
        this.classes = classes;
        this.enrollments = enrollments;
    }

    @Transactional
    public Enrollment enroll(AppUser teacher, Long classId, AppUser student) {
        ClassRoom classRoom = classes.findByIdAndBranchId(classId, teacher.getBranch().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
        return enroll(classRoom, student);
    }

    @Transactional
    public Enrollment enroll(ClassRoom classRoom, AppUser student) {
        if (student.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only a student can join a class");
        }

        if (!student.getBranch().getId().equals(classRoom.getBranch().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }

        if (enrollments.existsByStudentId(student.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This student already has a class");
        }

        int markerId = enrollments.findTopByOrderByMarkerIdDesc()
                .map(existing -> existing.getMarkerId() + 1)
                .orElse(0);

        if (markerId > Enrollment.MAX_MARKER_ID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No ArUco markers left in this dictionary");
        }

        return enrollments.save(new Enrollment(classRoom, student, markerId));
    }
}
