package com.pythogorean_apis.pythogorean_apis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pythogorean_apis.pythogorean_apis.domain.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findByClassRoomIdOrderByNameAsc(Long classRoomId);

    Optional<Subject> findByIdAndClassRoom_Branch_Id(Long id, Long branchId);

    boolean existsByClassRoomIdAndNameIgnoreCase(Long classRoomId, String name);
}
