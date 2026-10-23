package com.pythogorean_apis.pythogorean_apis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pythogorean_apis.pythogorean_apis.domain.Assessment;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findByClassRoom_Branch_IdOrderByIdDesc(Long branchId);

    boolean existsByClassRoomIdAndNameIgnoreCase(Long classRoomId, String name);
}
