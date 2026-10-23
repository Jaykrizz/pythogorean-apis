package com.pythogorean_apis.pythogorean_apis.classroom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AssessmentDtos {

    private AssessmentDtos() {
    }

    public record CreateAssessmentRequest(@NotBlank @Size(max = 80) String name, @NotNull Long topicId) {
    }

    public record AssessmentResponse(
            Long id,
            String name,
            String className,
            String subjectName,
            String topicName) {
    }
}
