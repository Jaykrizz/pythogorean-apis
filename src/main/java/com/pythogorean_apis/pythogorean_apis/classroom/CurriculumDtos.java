package com.pythogorean_apis.pythogorean_apis.classroom;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class CurriculumDtos {

    private CurriculumDtos() {
    }

    public record CreateQuestionRequest(
            @NotBlank @Size(max = 500) String prompt,
            @NotBlank @Size(max = 200) String optionA,
            @NotBlank @Size(max = 200) String optionB,
            @NotBlank @Size(max = 200) String optionC,
            @NotBlank @Size(max = 200) String optionD,
            @NotBlank @Pattern(regexp = "[ABCD]") String correctLetter,
            @Min(1) int marks) {
    }

    public record ShowTopicRequest(@NotNull Boolean shown) {
    }
}
