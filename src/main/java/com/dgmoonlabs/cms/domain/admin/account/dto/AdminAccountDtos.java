package com.dgmoonlabs.cms.domain.admin.account.dto;

import com.dgmoonlabs.cms.domain.admin.account.constant.AdminArea;
import com.dgmoonlabs.cms.domain.admin.account.entity.AdminGrade;
import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.validation.MatchesRegex;
import com.dgmoonlabs.cms.domain.common.user.entity.admin.entity.Admin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class AdminAccountDtos {
    private AdminAccountDtos() {
    }

    public record GradeSaveRequest(@NotBlank @Size(max = 50) String name, @Size(max = 300) String description,
                                   @NotNull List<@NotNull AdminArea> areas) {
    }

    public record GradeResponse(Long id, String name, String description, boolean superAdmin, List<AdminArea> areas,
                                long adminCount) {
        public static GradeResponse of(final AdminGrade grade, final long adminCount) {
            return new GradeResponse(grade.getId(), grade.getName(), grade.getDescription(), grade.isSuperAdmin(),
                    grade.isSuperAdmin() ? List.of(AdminArea.values()) : grade.getAreas() == null ? List.of() : grade.getAreas(),
                    adminCount);
        }
    }

    public record AdminCreateRequest(
            @NotBlank @MatchesRegex(RegexTarget.USERNAME) String username,
            @NotBlank @Size(max = 64) @MatchesRegex(RegexTarget.PASSWORD) String password,
            @NotBlank @Size(max = 50) String name,
            Long gradeId
    ) {
    }

    public record GradeAssignRequest(Long gradeId) {
    }

    public record AdminResponse(Long id, String username, String name, Long gradeId, String gradeName, boolean superAdmin,
                                boolean locked, LocalDateTime lastLoginDateTime, LocalDateTime createdAt) {
        public static AdminResponse of(final Admin admin, final AdminGrade grade, final boolean locked) {
            return new AdminResponse(admin.getId(), admin.getUsername(), admin.getName(), admin.getGradeId(),
                    grade == null ? null : grade.getName(), grade != null && grade.isSuperAdmin(), locked,
                    admin.getLastLoginDateTime(), admin.getCreatedAt());
        }
    }

    public record MyPermissions(boolean superAdmin, String gradeName, List<AdminArea> areas) {
    }

    public record AreaOption(AdminArea value, String label) {
    }
}
