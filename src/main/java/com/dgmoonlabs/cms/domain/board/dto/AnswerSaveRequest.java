package com.dgmoonlabs.cms.domain.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnswerSaveRequest(@NotBlank @Size(max = 100_000) String contentHtml) {
}
