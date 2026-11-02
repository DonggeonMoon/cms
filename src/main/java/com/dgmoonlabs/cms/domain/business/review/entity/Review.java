package com.dgmoonlabs.cms.domain.business.review.entity;

import com.dgmoonlabs.cms.domain.business.review.constant.ReviewTargetType;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(indexes = {
        @Index(name = "idx_review_product", columnList = "productId"),
        @Index(name = "idx_review_program", columnList = "programId")
})
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Review extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // rows written before program reviews existed get PRODUCT through the column default
    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @ColumnDefault("'PRODUCT'")
    @Enumerated(EnumType.STRING)
    @Comment("리뷰 대상 종류")
    private ReviewTargetType targetType = ReviewTargetType.PRODUCT;

    @Comment("상품 ID")
    private Long productId;

    @Comment("프로그램 ID")
    private Long programId;

    @Column(nullable = false)
    @ColumnDefault("5")
    @Comment("평점 (1~5)")
    private int rating;

    @ColumnDefault("'제목 없음'")
    @Comment("제목")
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    @ColumnDefault("''")
    @Comment("내용 텍스트")
    private String contentText;

    @Column(nullable = false, columnDefinition = "text")
    @ColumnDefault("''")
    @Comment("내용 HTML")
    private String contentHtml;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("추가 필드")
    private String fields;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("사용자 타입")
    private UserType userType;

    @Comment("사용자 ID")
    private Long userId;

    @Comment("작성자 이름")
    private String authorName;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("비밀글 여부")
    private boolean isSecret;

    @Comment("비밀번호")
    private String password;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("조회 수")
    private int hit;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("추천/좋아요/하트 수")
    private int likes;

    @Comment("IP 주소")
    private String ipAddress;
}
