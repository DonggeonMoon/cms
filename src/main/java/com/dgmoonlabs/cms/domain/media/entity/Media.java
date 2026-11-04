package com.dgmoonlabs.cms.domain.media.entity;

import com.dgmoonlabs.cms.domain.common.file.constant.MediaType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Media extends BaseEntity {
    public static final int DEFAULT_COMPLETION_RATE = 90;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("제목")
    private String title;

    @Column(columnDefinition = "text")
    @Comment("설명")
    private String description;

    @Column(nullable = false)
    @Comment("동영상/음악 파일 ID")
    private Long fileId;

    @Comment("썸네일 이미지 파일 ID")
    private Long thumbnailFileId;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("종류 (VIDEO 동영상, AUDIO 음악)")
    private MediaType mediaType;

    @Column(nullable = false)
    @Comment("재생 시간(초)")
    private int durationSeconds;

    @Column(nullable = false)
    @ColumnDefault("90")
    @Comment("수강 완료 진도율(%)")
    private int completionRate;

    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("공개 여부")
    private boolean isUsing;

    @Comment("시청 가능 등급 ID (없으면 누구나)")
    private Long accessGradeId;

    public void changeAccessGrade(final Long accessGradeId) {
        this.accessGradeId = accessGradeId;
    }

    public void update(final String title, final String description, final Long fileId, final Long thumbnailFileId,
                       final MediaType mediaType, final int durationSeconds, final int completionRate, final boolean isUsing) {
        this.title = title;
        this.description = description;
        this.fileId = fileId;
        this.thumbnailFileId = thumbnailFileId;
        this.mediaType = mediaType;
        this.durationSeconds = durationSeconds;
        this.completionRate = completionRate;
        this.isUsing = isUsing;
    }
}
