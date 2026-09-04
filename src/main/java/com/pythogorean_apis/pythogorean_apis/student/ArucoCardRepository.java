package com.pythogorean_apis.pythogorean_apis.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface ArucoCardRepository extends JpaRepository<ArucoCardEntity, Long> {

    Optional<ArucoCardEntity> findByStudentId(Long studentId);

    @Query("select card.student.id from ArucoCardEntity card where card.student.classEntity.id = :classId")
    Set<Long> findStudentIdsWithCardInClass(@Param("classId") Long classId);
}
