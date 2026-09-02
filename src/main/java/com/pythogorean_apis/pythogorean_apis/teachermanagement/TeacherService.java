package com.pythogorean_apis.pythogorean_apis.teachermanagement;

import com.pythogorean_apis.pythogorean_apis.auth.entity.User;
import com.pythogorean_apis.pythogorean_apis.auth.service.CurrentUserService;
import com.pythogorean_apis.pythogorean_apis.common.exception.ForbiddenException;
import org.springframework.stereotype.Service;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final CurrentUserService currentUserService;

    public TeacherService(
            TeacherRepository teacherRepository,
            CurrentUserService currentUserService) {

        this.teacherRepository = teacherRepository;
        this.currentUserService = currentUserService;
    }

    public TeacherEntity getCurrentTeacher() {

        User user = currentUserService.getCurrentUser();

        return teacherRepository.findByUser(user)
                .orElseThrow(() -> new ForbiddenException("Only a teacher can perform this action"));
    }
}
