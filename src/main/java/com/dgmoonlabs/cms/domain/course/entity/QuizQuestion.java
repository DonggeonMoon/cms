package com.dgmoonlabs.cms.domain.course.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

/** answerIndex 는 options 에서 정답의 순번(0부터). 수강생에게는 내려보내지 않는다. */
public record QuizQuestion(
        @NotBlank @Size(max = 500) String question,
        @NotNull @Size(min = 2, max = 10) List<@NotBlank @Size(max = 200) String> options,
        @NotNull @PositiveOrZero Integer answerIndex
) {
}
