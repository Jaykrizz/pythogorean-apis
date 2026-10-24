package com.pythogorean_apis.pythogorean_apis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pythogorean_apis.pythogorean_apis.domain.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findByBranch_IdOrderByNameAsc(Long branchId);

    Optional<Subject> findByIdAndBranch_Id(Long id, Long branchId);

    boolean existsByBranch_IdAndNameIgnoreCase(Long branchId, String name);
}
