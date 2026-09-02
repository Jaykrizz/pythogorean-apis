package com.pythogorean_apis.pythogorean_apis.student;

public record StudentResponse(
        Long id,
        Long userId,
        String name,
        String email,
        Long classId) {

    public static StudentResponse from(StudentEntity student) {
        return new StudentResponse(
                student.getId(),
                student.getUser().getId(),
                student.getUser().getName(),
                student.getUser().getEmail(),
                student.getClassEntity().getId());
    }
}
