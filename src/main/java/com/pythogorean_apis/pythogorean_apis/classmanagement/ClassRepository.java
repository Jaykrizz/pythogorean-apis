package com.pythogorean_apis.pythogorean_apis.classmanagement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassRepository extends JpaRepository<ClassEntity, Long> {

    List<ClassEntity> findByTeacherId(Long teacherId);
}