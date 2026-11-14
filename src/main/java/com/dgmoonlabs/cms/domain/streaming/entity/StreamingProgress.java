package com.dgmoonlabs.cms.domain.streaming.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_streaming_progress", columnNames = {"streamingId", "memberId"}))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class StreamingProgress extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("스트리밍 ID")
    private Long streamingId;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "text")
    @ColumnDefault("''")
    @Comment("실제로 재생한 구간(초) 목록. 예: 0-120,300-360")
    private String watchedRanges = "";

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("재생한 시간 합계(초, 겹치는 구간 제외)")
    private int watchedSeconds;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("마지막 재생 위치(초)")
    private int lastPositionSeconds;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("진도율(%)")
    private int progressRate;

    @Comment("마지막으로 진도를 반영한 일시 (재생 시간보다 비정상적으로 빠른 요청을 걸러내는 기준)")
    private LocalDateTime lastRecordedAt;

    /** 실제 재생 속도로는 나올 수 없을 만큼 빠르게 늘어난 진도 요청은 무시한다(최대 4배속 허용). */
    private static final double MAX_PLAUSIBLE_RATE = 4.0;
    /** 클라이언트는 보통 10초(REPORT_INTERVAL_MS)마다 보고하므로, 호출 간 실제 간격이 그보다 짧아도 최소 이만큼은 지난 것으로 봐준다. */
    private static final double MIN_ELAPSED_SECONDS = 10.0;

    public static StreamingProgress start(final Long streamingId, final Long memberId) {
        return StreamingProgress.builder().streamingId(streamingId).memberId(memberId).build();
    }

    public void record(final WatchedRanges ranges, final int lastPositionSeconds, final int durationSeconds, final LocalDateTime now) {
        int candidateWatched = Math.min(ranges.totalSeconds(), durationSeconds);
        int addedSeconds = Math.max(0, candidateWatched - this.watchedSeconds);
        double elapsedSeconds = lastRecordedAt == null ? Double.MAX_VALUE
                : Math.max(MIN_ELAPSED_SECONDS, Duration.between(lastRecordedAt, now).toMillis() / 1000.0);
        this.lastPositionSeconds = Math.clamp(lastPositionSeconds, 0, durationSeconds);
        this.lastRecordedAt = now;
        if (addedSeconds > elapsedSeconds * MAX_PLAUSIBLE_RATE) {
            return;
        }
        this.watchedRanges = ranges.format();
        this.watchedSeconds = candidateWatched;
        this.progressRate = durationSeconds == 0 ? 0 : watchedSeconds * 100 / durationSeconds;
    }
}
