package com.dgmoonlabs.cms.domain.streaming.dto;

import com.dgmoonlabs.cms.domain.common.file.constant.MediaType;
import com.dgmoonlabs.cms.domain.common.file.dto.FileResponse;
import com.dgmoonlabs.cms.domain.streaming.constant.EnrollmentStatus;
import com.dgmoonlabs.cms.domain.streaming.entity.Streaming;
import com.dgmoonlabs.cms.domain.streaming.entity.StreamingEnrollment;
import com.dgmoonlabs.cms.domain.streaming.entity.StreamingProgress;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class StreamingDtos {
    private StreamingDtos() {
    }

    public record StreamingSaveRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 20_000) String description,
            @NotNull Long fileId,
            Long thumbnailFileId,
            @NotNull @Min(1) @Max(86_400) Integer durationSeconds,
            @Min(1) @Max(100) Integer completionRate,
            Boolean trackProgress,
            Boolean isUsing,
            Long accessGradeId,
            Boolean paid,
            @PositiveOrZero BigDecimal price,
            @Positive @Max(36_500) Integer accessDays
    ) {
        public boolean paidOrDefault() {
            return Boolean.TRUE.equals(paid);
        }

        public int completionRateOrDefault() {
            return completionRate == null ? Streaming.DEFAULT_COMPLETION_RATE : completionRate;
        }

        public boolean trackProgressOrDefault() {
            return trackProgress == null || trackProgress;
        }

        public boolean isUsingOrDefault() {
            return isUsing == null || isUsing;
        }
    }

    public record MyProgress(EnrollmentStatus status, int progressRate, int watchedSeconds, int lastPositionSeconds,
                             LocalDateTime startedAt, LocalDateTime lastWatchedAt, LocalDateTime completedAt) {
        public static MyProgress of(final StreamingEnrollment enrollment, final StreamingProgress progress) {
            if (enrollment == null || progress == null) {
                return null;
            }
            return new MyProgress(enrollment.getStatus(), progress.getProgressRate(), progress.getWatchedSeconds(),
                    progress.getLastPositionSeconds(), enrollment.getCreatedAt(), progress.getModifiedAt(),
                    enrollment.getCompletedAt());
        }
    }

    public record StreamingResponse(Long id, String title, String description, Long fileId, String fileName,
                                    MediaType mediaType, String streamingUrl, Long thumbnailFileId, String thumbnailUrl,
                                    String posterUrl, int durationSeconds, int completionRate, boolean trackProgress,
                                    boolean isUsing, LocalDateTime createdAt, MyProgress myProgress, Long accessGradeId,
                                    boolean paid, BigDecimal price, Integer accessDays, Long productId, boolean playable,
                                    LocalDateTime accessExpiresAt) {
        /** 재생 주소는 권한을 확인하는 스트림 주소를 쓰고, 볼 수 없으면(유료 미구매) 비워 둔다. */
        public static StreamingResponse of(final Streaming streaming, final FileResponse file, final MyProgress myProgress,
                                           final boolean playable, final LocalDateTime accessExpiresAt) {
            return new StreamingResponse(streaming.getId(), streaming.getTitle(), streaming.getDescription(), streaming.getFileId(),
                    file == null ? null : file.originalName(), streaming.getMediaType(),
                    playable ? "/api/streaming/" + streaming.getId() + "/stream" : null, streaming.getThumbnailFileId(),
                    StreamingDtos.thumbnailUrl(streaming), StreamingDtos.posterUrl(streaming), streaming.getDurationSeconds(),
                    streaming.getCompletionRate(), streaming.isTrackProgress(), streaming.isUsing(), streaming.getCreatedAt(),
                    streaming.isTrackProgress() ? myProgress : null, streaming.getAccessGradeId(), streaming.isPaid(),
                    streaming.getPrice(), streaming.getAccessDays(), streaming.getProductId(), playable, accessExpiresAt);
        }
    }

    public static String thumbnailUrl(final Streaming streaming) {
        return streaming.getThumbnailFileId() == null ? null : "/api/files/" + streaming.getThumbnailFileId() + "/thumbnail";
    }

    public static String posterUrl(final Streaming streaming) {
        return streaming.getThumbnailFileId() == null ? null : "/api/files/" + streaming.getThumbnailFileId() + "/view";
    }

    public record WatchedRange(@NotNull @Min(0) Double start, @NotNull @Min(0) Double end) {
    }

    public record ProgressRequest(@NotNull @Size(max = 100) List<@Valid WatchedRange> ranges,
                                  @NotNull @Min(0) Double position) {
    }

    public record MyStreaming(Long streamingId, String title, MediaType mediaType, String thumbnailUrl, int durationSeconds,
                              boolean available, MyProgress progress) {
    }

    public record LearnerResponse(Long memberId, String username, EnrollmentStatus status, int progressRate,
                                  int watchedSeconds, LocalDateTime startedAt, LocalDateTime lastWatchedAt,
                                  LocalDateTime completedAt) {
        public static LearnerResponse of(final StreamingEnrollment enrollment, final StreamingProgress progress,
                                         final String username) {
            return new LearnerResponse(enrollment.getMemberId(), username, enrollment.getStatus(),
                    progress == null ? 0 : progress.getProgressRate(), progress == null ? 0 : progress.getWatchedSeconds(),
                    enrollment.getCreatedAt(), progress == null ? null : progress.getModifiedAt(), enrollment.getCompletedAt());
        }
    }
}
