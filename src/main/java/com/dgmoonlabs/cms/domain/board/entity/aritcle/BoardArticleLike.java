package com.dgmoonlabs.cms.domain.board.entity.aritcle;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_board_article_like", columnNames = {"articleId", "memberId"}))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class BoardArticleLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("게시글 ID")
    private Long articleId;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;
}
