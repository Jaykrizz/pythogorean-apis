package com.pythogorean_apis.pythogorean_apis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pythogorean_apis.pythogorean_apis.domain.ClassRoom;

public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long> {

    List<ClassRoom> findByBranchIdOrderByNameAsc(Long branchId);

    Optional<ClassRoom> findByIdAndBranchId(Long id, Long branchId);

    boolean existsByBranchIdAndNameIgnoreCase(Long branchId, String name);
}
