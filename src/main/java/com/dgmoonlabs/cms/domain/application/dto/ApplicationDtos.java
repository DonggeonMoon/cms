package com.dgmoonlabs.cms.domain.application.dto;

import com.dgmoonlabs.cms.domain.application.constant.SubmissionStatus;
import com.dgmoonlabs.cms.domain.application.entity.ApplicationForm;
import com.dgmoonlabs.cms.domain.application.entity.ApplicationSubmission;
import com.dgmoonlabs.cms.domain.application.entity.FormField;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class ApplicationDtos {
    private ApplicationDtos() {
    }

    public record FormSaveRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 200_000) String contentHtml,
            @NotNull @Size(min = 1, max = 50) List<@Valid FormField> fields,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            @Positive Integer maximumSubmissions,
            boolean allowMultiple,
            boolean isEnabled
    ) {
    }

    public record FormResponse(Long id, String title, String contentHtml, List<FormField> fields, LocalDateTime startDateTime,
                               LocalDateTime endDateTime, Integer maximumSubmissions, long submissionCount,
                               boolean allowMultiple, boolean isEnabled, boolean isOpen, boolean applied, LocalDateTime createdAt) {
        public static FormResponse of(final ApplicationForm form, final List<FormField> fields, final long submissionCount,
                                      final boolean isOpen, final boolean applied) {
            return new FormResponse(form.getId(), form.getTitle(), form.getContentHtml(), fields, form.getStartDateTime(),
                    form.getEndDateTime(), form.getMaximumSubmissions(), submissionCount, form.isAllowMultiple(),
                    form.isEnabled(), isOpen, applied, form.getCreatedAt());
        }
    }

    public record SubmitRequest(@NotNull Map<String, Object> answers) {
    }

    public record ReviewRequest(@NotNull SubmissionStatus status, @Size(max = 2000) String resultMessage,
                                @Size(max = 2000) String adminMemo) {
    }

    public record SubmissionResponse(Long id, Long formId, String formTitle, Long memberId, String username,
                                     Map<String, Object> answers, SubmissionStatus status, String resultMessage,
                                     String adminMemo, LocalDateTime createdAt) {
        public static SubmissionResponse of(final ApplicationSubmission submission, final Map<String, Object> answers,
                                            final String username, final boolean forAdmin) {
            return new SubmissionResponse(submission.getId(), submission.getFormId(), submission.getFormTitle(),
                    submission.getMemberId(), username, answers, submission.getStatus(), submission.getResultMessage(),
                    forAdmin ? submission.getAdminMemo() : null, submission.getCreatedAt());
        }
    }
}
