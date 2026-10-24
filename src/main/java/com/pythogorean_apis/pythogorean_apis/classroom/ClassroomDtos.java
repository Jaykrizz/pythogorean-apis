package com.pythogorean_apis.pythogorean_apis.classroom;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ClassroomDtos {

    private ClassroomDtos() {
    }

    public record NameRequest(@NotBlank @Size(max = 80) String name) {
    }

    public record EnrollRequest(@NotNull Long studentId) {
    }

    public record LinkSubjectRequest(@NotNull Long subjectId) {
    }

    public record ClassSummary(Long id, String name, int studentCount, int subjectCount) {
    }

    public record QuestionResponse(
            Long id,
            String prompt,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String correctLetter,
            int marks) {
    }

    public record TopicResponse(Long id, String name, boolean shown, List<QuestionResponse> questions) {
    }

    public record SubjectResponse(Long id, String name, List<TopicResponse> topics) {
    }

    public record StudentCard(Long studentId, String name, String email, int markerId) {
    }

    public record ClassDetailResponse(
            Long id,
            String name,
            List<SubjectResponse> subjects,
            List<StudentCard> students) {
    }

    public record PersonOption(Long id, String name, String email) {
    }
}
