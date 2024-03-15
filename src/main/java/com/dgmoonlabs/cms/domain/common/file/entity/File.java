package com.dgmoonlabs.cms.domain.common.file.entity;

import com.dgmoonlabs.cms.domain.common.file.constant.MediaType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.springframework.util.MimeType;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class File {
    @Id
    private Long id;

    @Comment("파일 원본 이름")
    private String originalName;

    @Comment("파일 UUID")
    private String uuid;

    @Comment("파일 경로")
    private String path;

    @Comment("확장자")
    private String extension;

    @Comment("MIME 타입")
    private MimeType mimeType;

    @Comment("미디어 종류")
    private MediaType mediaType;

    @Comment("파일 사이즈")
    private Integer size;

    @Comment("대체 텍스트")
    private String alternativeText;

    @Comment("다운로드 수")
    private String downloadCount;
}
