package com.pythogorean_apis.pythogorean_apis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pythogorean_apis.pythogorean_apis.domain.Enrollment;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByClassRoomIdOrderByStudent_NameAsc(Long classRoomId);

    Optional<Enrollment> findByStudentId(Long studentId);

    Optional<Enrollment> findTopByOrderByMarkerIdDesc();

    boolean existsByStudentId(Long studentId);
}
