package com.pythogorean_apis.pythogorean_apis.student;

public record StudentResponse(
        Long id,
        Long userId,
        String name,
        String email,
        Long classId,
        Integer arucoMarkerId,
        boolean cardGenerated) {

    public static StudentResponse from(StudentEntity student, boolean cardGenerated) {
        return new StudentResponse(
                student.getId(),
                student.getUser().getId(),
                student.getUser().getName(),
                student.getUser().getEmail(),
                student.getClassEntity().getId(),
                student.getArucoMarkerId(),
                cardGenerated);
    }
}
