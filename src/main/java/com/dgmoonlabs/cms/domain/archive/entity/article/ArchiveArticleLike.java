package com.dgmoonlabs.cms.domain.archive.entity.article;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_archive_article_like", columnNames = {"articleId", "memberId"}))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ArchiveArticleLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("아카이브 글 ID")
    private Long articleId;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;
}
