package com.dgmoonlabs.cms.domain.course.dto;

import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.domain.course.constant.AccessPeriodMode;
import com.dgmoonlabs.cms.domain.course.constant.CourseApplicationStatus;
import com.dgmoonlabs.cms.domain.course.constant.EnrollmentMode;
import com.dgmoonlabs.cms.domain.course.entity.CourseApplication;
import com.dgmoonlabs.cms.domain.course.entity.CourseLessonComment;
import com.dgmoonlabs.cms.domain.course.entity.QuizQuestion;
import com.dgmoonlabs.cms.domain.streaming.constant.EnrollmentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class CourseDtos {
    private CourseDtos() {
    }

    public record LessonRequest(
            Long id,
            @NotBlank @Size(max = 200) String title,
            @Size(max = 200) String unitName,
            Long streamingId,
            Long quizId,
            @Size(max = 20) List<Long> materialFileIds,
            boolean mandatory,
            Boolean preventSkippingOverride
    ) {
        public List<Long> materialFileIdsOrEmpty() {
            return materialFileIds == null ? List.of() : materialFileIds;
        }
    }

    public record CourseSaveRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 1000) String description,
            Long thumbnailFileId,
            Long categoryId,
            boolean sequential,
            Long accessGradeId,
            boolean isUsing,
            boolean isCourse,
            EnrollmentMode enrollmentMode,
            LocalDateTime applicationStartAt,
            LocalDateTime applicationEndAt,
            AccessPeriodMode accessPeriodMode,
            LocalDateTime accessStartAt,
            LocalDateTime accessEndAt,
            @Positive @Max(36_500) Integer accessDays,
            @Size(max = 20) List<Long> materialFileIds,
            @DecimalMin("1.0") @DecimalMax("16.0") Double maxPlaybackRate,
            boolean preventSkipping,
            @Min(1) @Max(100) Integer completionThresholdPercent,
            boolean attendanceRequired,
            boolean paid,
            @PositiveOrZero BigDecimal price,
            @Min(1) @Max(100) Integer refundableProgressBelowPercent,
            Boolean trackProgress,
            @Min(1) @Max(100) Integer completionRate,
            @NotEmpty @Size(max = 100) List<@Valid LessonRequest> lessons
    ) {
        public EnrollmentMode enrollmentModeOrDefault() {
            return enrollmentMode == null ? EnrollmentMode.FREE : enrollmentMode;
        }

        public boolean trackProgressOrDefault() {
            return trackProgress == null || trackProgress;
        }

        public int completionRateOrDefault() {
            return completionRate == null ? 90 : completionRate;
        }

        public AccessPeriodMode accessPeriodModeOrDefault() {
            return accessPeriodMode == null ? AccessPeriodMode.UNLIMITED : accessPeriodMode;
        }

        public List<Long> materialFileIdsOrEmpty() {
            return materialFileIds == null ? List.of() : materialFileIds;
        }
    }

    public record ExtendAccessRequest(LocalDateTime accessUntil) {
    }

    public record PublicQuestion(String question, List<String> options) {
        public static PublicQuestion of(final QuizQuestion question) {
            return new PublicQuestion(question.question(), question.options());
        }
    }

    public record MaterialResponse(Long id, String title, String mimeType, Long size, String downloadUrl) {
    }

    public record LessonResponse(Long id, int sequence, String title, String unitName, Long streamingId, String streamingTitle,
                                 Integer durationSeconds, Long quizId, String quizTitle, List<?> quiz, Integer passScore,
                                 boolean streamingCompleted, Integer progressRate, Integer lastPositionSeconds,
                                 boolean quizPassed, Integer quizScore, boolean completed, boolean locked,
                                 List<MaterialResponse> materials, boolean mandatory, boolean preventSkipping,
                                 Boolean preventSkippingOverride, Integer attemptsUsed, Integer attemptsAllowed) {
    }

    public record MyCourseProgress(EnrollmentStatus status, int completedLessons, LocalDateTime enrolledAt,
                                   LocalDateTime completedAt) {
    }

    public record CourseResponse(Long id, String title, String description, Long thumbnailFileId, String thumbnailUrl,
                                 Long categoryId, boolean sequential, Long accessGradeId, boolean isUsing, boolean isCourse,
                                 EnrollmentMode enrollmentMode, boolean applicationOpen, LocalDateTime applicationStartAt,
                                 LocalDateTime applicationEndAt, AccessPeriodMode accessPeriodMode, LocalDateTime accessStartAt,
                                 LocalDateTime accessEndAt, Integer accessDays, LocalDateTime myAccessUntil, Boolean myAccessOpen,
                                 Double maxPlaybackRate, boolean preventSkipping, int completionThresholdPercent,
                                 boolean attendanceRequired, Boolean myAttendanceOk, boolean paid, BigDecimal price, Long productId,
                                 Integer refundableProgressBelowPercent, boolean trackProgress, int completionRate,
                                 int lessonCount, List<LessonResponse> lessons,
                                 List<MaterialResponse> materials, MyCourseProgress myProgress,
                                 CourseApplicationStatus myApplicationStatus, LocalDateTime createdAt) {
    }

    public record ApplicationResponse(Long id, Long memberId, String username, CourseApplicationStatus status,
                                      LocalDateTime appliedAt, LocalDateTime decidedAt) {
        public static ApplicationResponse of(final CourseApplication application, final String username) {
            return new ApplicationResponse(application.getId(), application.getMemberId(), username, application.getStatus(),
                    application.getCreatedAt(), application.getDecidedAt());
        }
    }

    public record QuizSubmitRequest(@NotNull @Size(max = 30) List<@NotNull Integer> answers) {
    }

    public record QuizResult(int score, int passScore, boolean passed, List<Boolean> correct, int durationSeconds,
                             long attempts, Integer attemptsRemaining, boolean revealed, List<Integer> answerIndexes,
                             List<String> explanations, MyCourseProgress myProgress) {
    }

    public record StartQuestion(String question, List<String> options) {
    }

    public record QuizStartResponse(List<StartQuestion> questions, Integer timeLimitMinutes, int attemptsUsed,
                                    Integer attemptsAllowed, LocalDateTime startedAt) {
    }

    public record GrantAttemptsRequest(@Positive int count) {
    }

    public record LearnerResponse(Long memberId, String username, EnrollmentStatus status, int completedLessons,
                                  int totalLessons, LocalDateTime enrolledAt, LocalDateTime completedAt,
                                  LocalDateTime accessUntil, LocalDateTime accessExtendedUntil, Boolean attendanceOk) {
    }

    public record CourseStatisticsRow(
            Long courseId, String title, boolean isCourse, boolean paid,
            int totalEnrolled, int inProgress, int completed, int revoked, double completionRate,
            long pendingApplications, long approvedApplications, long rejectedApplications, long canceledApplications,
            BigDecimal revenue, long paidCount, long refundedCount, long pendingPaymentCount
    ) {
    }

    public record CourseStatisticsResponse(List<CourseStatisticsRow> rows, BigDecimal totalRevenue, int totalEnrolled, int totalCompleted) {
    }

    public record LessonCommentSaveRequest(Long parentId, @NotBlank @Size(max = 2000) String content) {
    }

    public record LessonCommentEditRequest(@NotBlank @Size(max = 2000) String content) {
    }

    public record UnansweredQuestionResponse(Long commentId, Long courseId, String courseTitle, boolean isCourse, Long lessonId,
                                             String lessonTitle, String authorName, String content, LocalDateTime createdAt) {
    }

    public record LessonCommentResponse(Long id, Long parentId, int depth, String content, String authorName,
                                        UserType authorType, boolean deleted, boolean canDelete, boolean canEdit,
                                        LocalDateTime createdAt) {
        private static final String REMOVED = "삭제된 질문/댓글입니다.";

        public static LessonCommentResponse of(final CourseLessonComment comment, final boolean canDelete, final boolean canEdit) {
            boolean deleted = comment.getDeletedAt() != null;
            return new LessonCommentResponse(comment.getId(), comment.getParentId(), comment.getDepth(),
                    deleted ? REMOVED : comment.getContent(), deleted ? null : comment.getAuthorName(), comment.getUserType(),
                    deleted, canDelete && !deleted, canEdit && !deleted, comment.getCreatedAt());
        }
    }
}
