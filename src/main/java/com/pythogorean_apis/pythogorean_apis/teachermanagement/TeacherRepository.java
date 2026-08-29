package com.pythogorean_apis.pythogorean_apis.teachermanagement;

import com.pythogorean_apis.pythogorean_apis.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<TeacherEntity, Long> {

    Optional<TeacherEntity> findByUser(User user);
}