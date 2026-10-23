package com.pythogorean_apis.pythogorean_apis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pythogorean_apis.pythogorean_apis.domain.Topic;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findBySubjectIdOrderByNameAsc(Long subjectId);

    boolean existsBySubjectIdAndNameIgnoreCase(Long subjectId, String name);
}
