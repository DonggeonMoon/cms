package com.dgmoonlabs.cms.domain.archive.entity.article;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(indexes = @Index(name = "idx_archive_article_file_article", columnList = "articleId"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ArchiveArticleFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("아카이브 글 ID")
    private Long articleId;

    @Column(nullable = false)
    @Comment("파일 ID")
    private Long fileId;

    @Column(nullable = false)
    @Comment("첨부 순서")
    private int sequence;
}
