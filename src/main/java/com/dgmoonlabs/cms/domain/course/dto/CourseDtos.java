package com.dgmoonlabs.cms.domain.course.dto;

import com.dgmoonlabs.cms.domain.course.entity.QuizQuestion;
import com.dgmoonlabs.cms.domain.media.constant.EnrollmentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public final class CourseDtos {
    private CourseDtos() {
    }

    public record LessonRequest(
            Long id,
            @NotBlank @Size(max = 200) String title,
            Long mediaId,
            @Size(max = 30) List<@Valid QuizQuestion> quiz,
            @Min(1) @Max(100) Integer passScore
    ) {
        public List<QuizQuestion> quizOrEmpty() {
            return quiz == null ? List.of() : quiz;
        }
    }

    public record CourseSaveRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 1000) String description,
            Long thumbnailFileId,
            boolean sequential,
            Long accessGradeId,
            boolean isUsing,
            @NotEmpty @Size(max = 100) List<@Valid LessonRequest> lessons
    ) {
    }

    public record PublicQuestion(String question, List<String> options) {
        public static PublicQuestion of(final QuizQuestion question) {
            return new PublicQuestion(question.question(), question.options());
        }
    }

    public record LessonResponse(Long id, int sequence, String title, Long mediaId, String mediaTitle, Integer durationSeconds,
                                 List<?> quiz, Integer passScore, boolean mediaCompleted, boolean quizPassed,
                                 boolean completed, boolean locked) {
    }

    public record MyCourseProgress(EnrollmentStatus status, int completedLessons, LocalDateTime enrolledAt,
                                   LocalDateTime completedAt) {
    }

    public record CourseResponse(Long id, String title, String description, Long thumbnailFileId, String thumbnailUrl,
                                 boolean sequential, Long accessGradeId, boolean isUsing, int lessonCount,
                                 List<LessonResponse> lessons, MyCourseProgress myProgress, LocalDateTime createdAt) {
    }

    public record QuizSubmitRequest(@NotNull @Size(max = 30) List<@NotNull Integer> answers) {
    }

    public record QuizResult(int score, int passScore, boolean passed, List<Boolean> correct, long attempts,
                             MyCourseProgress myProgress) {
    }

    public record LearnerResponse(Long memberId, String username, EnrollmentStatus status, int completedLessons,
                                  int totalLessons, LocalDateTime enrolledAt, LocalDateTime completedAt) {
    }
}
