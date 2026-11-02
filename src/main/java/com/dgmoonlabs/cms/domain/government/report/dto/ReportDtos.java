package com.dgmoonlabs.cms.domain.government.report.dto;

import com.dgmoonlabs.cms.domain.government.report.constant.ReportStatus;
import com.dgmoonlabs.cms.domain.government.report.entity.Report;
import com.dgmoonlabs.cms.domain.government.report.entity.answer.ReportAnswer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public final class ReportDtos {
    private ReportDtos() {
    }

    @Builder
    public record ReportCreateRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 100_000) String contentHtml,
            @Size(max = 50) String reporterName,
            @Email String email,
            @Size(max = 30) String phoneNumber,
            @Size(max = 64) String password
    ) {
    }

    public record ReportAccessRequest(String password) {
    }

    public record ReportStatusRequest(@NotNull ReportStatus status) {
    }

    public record ReportAnswerRequest(@NotBlank @Size(max = 200) String title, @NotBlank @Size(max = 100_000) String contentHtml) {
    }

    public record ReportSummary(Long id, String title, String reporterName, ReportStatus status, LocalDateTime createdAt) {
        public static ReportSummary from(final Report report) {
            return new ReportSummary(report.getId(), report.getTitle(), report.getReporterName(), report.getStatus(),
                    report.getCreatedAt());
        }
    }

    public record AnswerResponse(Long id, String title, String contentHtml, String authorName, LocalDateTime createdAt) {
        public static AnswerResponse from(final ReportAnswer answer) {
            return new AnswerResponse(answer.getId(), answer.getTitle(), answer.getContentHtml(), answer.getAuthorName(),
                    answer.getCreatedAt());
        }
    }

    public record ReportDetail(Long id, String title, String contentHtml, String reporterName, String email,
                               String phoneNumber, ReportStatus status, List<AnswerResponse> answers,
                               LocalDateTime createdAt) {
        public static ReportDetail of(final Report report, final List<AnswerResponse> answers) {
            return new ReportDetail(report.getId(), report.getTitle(), report.getContentHtml(), report.getReporterName(),
                    report.getEmail(), report.getPhoneNumber(), report.getStatus(), answers, report.getCreatedAt());
        }
    }
}
