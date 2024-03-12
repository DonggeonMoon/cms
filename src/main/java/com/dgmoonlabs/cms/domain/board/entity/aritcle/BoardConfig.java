package com.dgmoonlabs.cms.domain.board.entity.aritcle;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class BoardConfig extends BaseEntity {
    @Id
    private Long id;

    private String authority;

    @Column(nullable = false)
    @ColumnDefault("0")
    private boolean isMemberAccessible;

    @Column(nullable = false)
    @ColumnDefault("0")
    private boolean isMemberReadable;

    @Column(nullable = false)
    @ColumnDefault("0")
    private boolean isMemberWritable;

    @Column(nullable = false)
    @ColumnDefault("0")
    private boolean isGuestAccessible;

    @Column(nullable = false)
    @ColumnDefault("0")
    private boolean isGuestReadable;

    @Column(nullable = false)
    @ColumnDefault("0")
    private boolean isGuestWritable;

    @Column(nullable = false)
    @ColumnDefault("0")
    private boolean supportsNotice;

    @Column(nullable = false)
    @ColumnDefault("0")
    private boolean supportsSecret;

    @Column(nullable = false)
    @ColumnDefault("0")
    private boolean supportsComment;

    @Column(nullable = false)
    @ColumnDefault("0")
    private boolean supportsLike;

    @Column(nullable = false)
    @ColumnDefault("0")
    private boolean supportThumbnail;

    @Column(nullable = false)
    @ColumnDefault("0")
    private boolean supportsThumbnailCrop;

    @Column(nullable = false)
    @ColumnDefault("0")
    private boolean thumbnailWidth;

    @Column(nullable = false)
    @ColumnDefault("0")
    private boolean thumbnailHeight;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int maximumFileCount = 5;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int maximumFileSize = 10;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int pageSize = 10;
}
