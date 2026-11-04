package com.dgmoonlabs.cms.domain.common.grade.dto;

import com.dgmoonlabs.cms.domain.common.grade.constant.GradeChangeReason;
import com.dgmoonlabs.cms.domain.common.grade.entity.MemberGrade;
import com.dgmoonlabs.cms.domain.common.grade.entity.MemberGradeHistory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;

public final class GradeDtos {
    private GradeDtos() {
    }

    public record GradeSaveRequest(
            @NotBlank @Size(max = 50) String name,
            @Size(max = 255) String description,
            @Min(0) @Max(1000) int rank,
            boolean isDefault
    ) {
    }

    public record GradeResponse(Long id, String name, String description, int rank, boolean isDefault) {
        public static GradeResponse from(final MemberGrade grade) {
            return new GradeResponse(grade.getId(), grade.getName(), grade.getDescription(), grade.getRank(), grade.isDefault());
        }
    }

    public record MemberGradeRequest(Long gradeId, LocalDateTime expiresAt) {
    }

    public record MyGradeResponse(Long gradeId, String gradeName, int rank, LocalDateTime expiresAt, String baseGradeName) {
    }

    public record GradeHistoryResponse(Long id, String fromGradeName, String toGradeName, LocalDateTime expiresAt,
                                       GradeChangeReason reason, Long orderId, String changedBy, LocalDateTime createdAt) {
        public static GradeHistoryResponse of(final MemberGradeHistory history, final Map<Long, String> names) {
            return new GradeHistoryResponse(history.getId(), names.get(history.getFromGradeId()),
                    names.get(history.getToGradeId()), history.getExpiresAt(), history.getReason(), history.getOrderId(),
                    history.getChangedBy(), history.getCreatedAt());
        }
    }
}
