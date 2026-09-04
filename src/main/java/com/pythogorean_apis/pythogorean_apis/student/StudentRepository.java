package com.pythogorean_apis.pythogorean_apis.student;

import com.pythogorean_apis.pythogorean_apis.auth.entity.User;
import com.pythogorean_apis.pythogorean_apis.classmanagement.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<StudentEntity, Long> {

    Optional<StudentEntity> findByUser(User user);

    Optional<StudentEntity> findByArucoMarkerId(Integer arucoMarkerId);

    List<StudentEntity> findByClassEntityOrderByArucoMarkerIdAsc(ClassEntity classEntity);

    @Query("select max(student.arucoMarkerId) from StudentEntity student")
    Integer findHighestArucoMarkerId();
}
