package com.pythogorean_apis.pythogorean_apis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pythogorean_apis.pythogorean_apis.domain.ClassSubject;

public interface ClassSubjectRepository extends JpaRepository<ClassSubject, Long> {

    List<ClassSubject> findByClassRoomIdOrderBySubject_NameAsc(Long classRoomId);

    boolean existsByClassRoomIdAndSubjectId(Long classRoomId, Long subjectId);
}
