package com.pythogorean_apis.pythogorean_apis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pythogorean_apis.pythogorean_apis.domain.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByTopicIdOrderBySortOrderAscIdAsc(Long topicId);

    int countByTopicId(Long topicId);
}
