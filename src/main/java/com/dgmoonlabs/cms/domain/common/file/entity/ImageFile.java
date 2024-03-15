package com.dgmoonlabs.cms.domain.common.file.entity;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

@Entity
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ImageFile extends File {
    @Comment("썸네일 경로")
    private String thumbnailPath;

    @Comment("원본 가로 길이")
    private int originalWidth;

    @Comment("원본 세로 길이")
    private int originalHeight;

    @Comment("썸네일 가로 길이")
    private int thumbnailWidth;

    @Comment("썸네일 세로 길이")
    private int thumbnailHeight;
}
