package com.pythogorean_apis.pythogorean_apis.classmanagement;

import com.pythogorean_apis.pythogorean_apis.auth.entity.Role;
import com.pythogorean_apis.pythogorean_apis.auth.entity.User;
import com.pythogorean_apis.pythogorean_apis.auth.service.CurrentUserService;
import com.pythogorean_apis.pythogorean_apis.teachermanagement.TeacherEntity;
import com.pythogorean_apis.pythogorean_apis.teachermanagement.TeacherRepository;

import org.springframework.stereotype.Service;

@Service
public class ClassService {

    private final ClassRepository classRepository;
    private final CurrentUserService currentUserService;
    private final TeacherRepository teacherRepository;

    public ClassService(
            ClassRepository classRepository,
            CurrentUserService currentUserService,
            TeacherRepository teacherRepository) {

        this.classRepository = classRepository;
        this.currentUserService = currentUserService;
        this.teacherRepository = teacherRepository;
    }

    public ClassEntity createClass(String name) {

        User user = currentUserService.getCurrentUser();

        if (user.getRole() != Role.TEACHER) {
            throw new RuntimeException("Only teachers can create classes");
        }

        TeacherEntity teacher = teacherRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found"));

        ClassEntity classEntity = new ClassEntity();
        classEntity.setName(name);
        classEntity.setTeacher(teacher);
        classEntity.setBranch(teacher.getBranch());

        return classRepository.save(classEntity);
    }
}