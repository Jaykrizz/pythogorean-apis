package com.pythogorean_apis.pythogorean_apis.classroom;

import java.util.List;

import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.QuestionResponse;
import com.pythogorean_apis.pythogorean_apis.classroom.ClassroomDtos.StudentCard;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AssessmentDtos {

    private AssessmentDtos() {
    }

    public record CreateAssessmentRequest(
            @NotBlank @Size(max = 80) String name,
            @NotNull Long classId,
            @NotNull Long topicId) {
    }

    public record AssessmentResponse(
            Long id,
            String name,
            String className,
            String subjectName,
            String topicName) {
    }

    public record SessionResponse(
            Long id,
            String name,
            String className,
            List<QuestionResponse> questions,
            List<StudentCard> students) {
    }

    public record CaptureResponse(List<CapturedAnswer> answers) {
    }

    public record CapturedAnswer(Long studentId, int markerId, String letter) {
    }

    public record VisionDetectResponse(List<VisionAnswer> answers) {
        public record VisionAnswer(int markerId, String letter) {
        }
    }
}
