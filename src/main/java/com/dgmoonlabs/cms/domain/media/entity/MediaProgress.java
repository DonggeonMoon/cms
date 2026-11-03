package com.dgmoonlabs.cms.domain.media.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_media_progress", columnNames = {"mediaId", "memberId"}))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class MediaProgress extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("스트리밍 ID")
    private Long mediaId;

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

    public static MediaProgress start(final Long mediaId, final Long memberId) {
        return MediaProgress.builder().mediaId(mediaId).memberId(memberId).build();
    }

    public void record(final WatchedRanges ranges, final int lastPositionSeconds, final int durationSeconds) {
        this.watchedRanges = ranges.format();
        this.watchedSeconds = Math.min(ranges.totalSeconds(), durationSeconds);
        this.lastPositionSeconds = Math.clamp(lastPositionSeconds, 0, durationSeconds);
        this.progressRate = durationSeconds == 0 ? 0 : watchedSeconds * 100 / durationSeconds;
    }
}
