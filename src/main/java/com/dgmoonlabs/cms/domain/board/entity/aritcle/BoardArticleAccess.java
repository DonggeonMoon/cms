package com.dgmoonlabs.cms.domain.board.entity.aritcle;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(indexes = @Index(name = "idx_board_article_access_member_article", columnList = "memberId, articleId"),
        uniqueConstraints = @UniqueConstraint(name = "uk_board_article_access_order_article", columnNames = {"orderId", "articleId"}))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class BoardArticleAccess extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Column(nullable = false)
    @Comment("게시글 ID")
    private Long articleId;

    @Column(nullable = false)
    @Comment("구매 주문 ID")
    private Long orderId;

    @Comment("주문 취소로 회수한 일시")
    private LocalDateTime revokedAt;

    public void revoke(final LocalDateTime now) {
        if (revokedAt == null) {
            revokedAt = now;
        }
    }
}
