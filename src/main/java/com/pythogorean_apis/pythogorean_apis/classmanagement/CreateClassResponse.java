package com.pythogorean_apis.pythogorean_apis.classmanagement;

public record CreateClassResponse(
        Long id,
        String name) {

    public static CreateClassResponse from(ClassEntity classEntity) {
        return new CreateClassResponse(classEntity.getId(), classEntity.getName());
    }
}
