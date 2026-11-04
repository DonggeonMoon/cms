package com.dgmoonlabs.cms.domain.media.dto;

import com.dgmoonlabs.cms.domain.common.file.constant.MediaType;
import com.dgmoonlabs.cms.domain.common.file.dto.FileResponse;
import com.dgmoonlabs.cms.domain.media.constant.EnrollmentStatus;
import com.dgmoonlabs.cms.domain.media.entity.Media;
import com.dgmoonlabs.cms.domain.media.entity.MediaEnrollment;
import com.dgmoonlabs.cms.domain.media.entity.MediaProgress;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public final class MediaDtos {
    private MediaDtos() {
    }

    public record MediaSaveRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 20_000) String description,
            @NotNull Long fileId,
            Long thumbnailFileId,
            @NotNull @Min(1) @Max(86_400) Integer durationSeconds,
            @Min(1) @Max(100) Integer completionRate,
            Boolean isUsing,
            Long accessGradeId
    ) {
        public int completionRateOrDefault() {
            return completionRate == null ? Media.DEFAULT_COMPLETION_RATE : completionRate;
        }

        public boolean isUsingOrDefault() {
            return isUsing == null || isUsing;
        }
    }

    public record MyProgress(EnrollmentStatus status, int progressRate, int watchedSeconds, int lastPositionSeconds,
                             LocalDateTime startedAt, LocalDateTime lastWatchedAt, LocalDateTime completedAt) {
        public static MyProgress of(final MediaEnrollment enrollment, final MediaProgress progress) {
            if (enrollment == null || progress == null) {
                return null;
            }
            return new MyProgress(enrollment.getStatus(), progress.getProgressRate(), progress.getWatchedSeconds(),
                    progress.getLastPositionSeconds(), enrollment.getCreatedAt(), progress.getModifiedAt(),
                    enrollment.getCompletedAt());
        }
    }

    public record MediaResponse(Long id, String title, String description, Long fileId, String fileName,
                                MediaType mediaType, String mediaUrl, Long thumbnailFileId, String thumbnailUrl,
                                String posterUrl, int durationSeconds, int completionRate, boolean isUsing,
                                LocalDateTime createdAt, MyProgress myProgress, Long accessGradeId) {
        public static MediaResponse of(final Media media, final FileResponse file, final MyProgress myProgress) {
            return new MediaResponse(media.getId(), media.getTitle(), media.getDescription(), media.getFileId(),
                    file == null ? null : file.originalName(), media.getMediaType(),
                    "/api/files/" + media.getFileId() + "/view", media.getThumbnailFileId(), MediaDtos.thumbnailUrl(media),
                    MediaDtos.posterUrl(media), media.getDurationSeconds(), media.getCompletionRate(), media.isUsing(),
                    media.getCreatedAt(), myProgress, media.getAccessGradeId());
        }
    }

    public static String thumbnailUrl(final Media media) {
        return media.getThumbnailFileId() == null ? null : "/api/files/" + media.getThumbnailFileId() + "/thumbnail";
    }

    public static String posterUrl(final Media media) {
        return media.getThumbnailFileId() == null ? null : "/api/files/" + media.getThumbnailFileId() + "/view";
    }

    public record WatchedRange(@NotNull @Min(0) Double start, @NotNull @Min(0) Double end) {
    }

    public record ProgressRequest(@NotNull @Size(max = 100) List<@Valid WatchedRange> ranges,
                                  @NotNull @Min(0) Double position) {
    }

    public record MyMedia(Long mediaId, String title, MediaType mediaType, String thumbnailUrl, int durationSeconds,
                          boolean available, MyProgress progress) {
    }

    public record LearnerResponse(Long memberId, String username, EnrollmentStatus status, int progressRate,
                                  int watchedSeconds, LocalDateTime startedAt, LocalDateTime lastWatchedAt,
                                  LocalDateTime completedAt) {
        public static LearnerResponse of(final MediaEnrollment enrollment, final MediaProgress progress,
                                         final String username) {
            return new LearnerResponse(enrollment.getMemberId(), username, enrollment.getStatus(),
                    progress == null ? 0 : progress.getProgressRate(), progress == null ? 0 : progress.getWatchedSeconds(),
                    enrollment.getCreatedAt(), progress == null ? null : progress.getModifiedAt(), enrollment.getCompletedAt());
        }
    }
}
