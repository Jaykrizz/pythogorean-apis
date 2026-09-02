package com.pythogorean_apis.pythogorean_apis.classmanagement;

import com.pythogorean_apis.pythogorean_apis.common.exception.BadRequestException;
import com.pythogorean_apis.pythogorean_apis.common.exception.ForbiddenException;
import com.pythogorean_apis.pythogorean_apis.common.exception.NotFoundException;
import com.pythogorean_apis.pythogorean_apis.teachermanagement.TeacherEntity;
import com.pythogorean_apis.pythogorean_apis.teachermanagement.TeacherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClassService {

    private final ClassRepository classRepository;
    private final TeacherService teacherService;

    public ClassService(
            ClassRepository classRepository,
            TeacherService teacherService) {

        this.classRepository = classRepository;
        this.teacherService = teacherService;
    }

    @Transactional
    public ClassEntity createClass(String name) {

        if (name == null || name.isBlank()) {
            throw new BadRequestException("Class name is required");
        }

        TeacherEntity teacher = teacherService.getCurrentTeacher();

        ClassEntity classEntity = new ClassEntity();
        classEntity.setName(name.trim());
        classEntity.setTeacher(teacher);
        classEntity.setBranch(teacher.getBranch());

        return classRepository.save(classEntity);
    }

    @Transactional(readOnly = true)
    public List<ClassEntity> getMyClasses() {
        return classRepository.findByTeacherId(teacherService.getCurrentTeacher().getId());
    }

    @Transactional(readOnly = true)
    public ClassEntity getOwnedClass(Long classId) {

        TeacherEntity teacher = teacherService.getCurrentTeacher();

        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new NotFoundException("Class not found"));

        if (!classEntity.getTeacher().getId().equals(teacher.getId())) {
            throw new ForbiddenException("This class belongs to another teacher");
        }

        return classEntity;
    }
}
