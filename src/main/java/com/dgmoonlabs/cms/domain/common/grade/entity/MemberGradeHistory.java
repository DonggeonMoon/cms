package com.dgmoonlabs.cms.domain.common.grade.entity;

import com.dgmoonlabs.cms.domain.common.grade.constant.GradeChangeReason;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(indexes = @Index(name = "idx_member_grade_history_member", columnList = "memberId"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class MemberGradeHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Comment("이전 등급 ID")
    private Long fromGradeId;

    @Comment("바뀐 등급 ID")
    private Long toGradeId;

    @Comment("이전 등급 만료 일시")
    private LocalDateTime fromExpiresAt;

    @Comment("이전 등급의 복귀 등급 ID")
    private Long fromBaseGradeId;

    @Comment("바뀐 등급 만료 일시")
    private LocalDateTime expiresAt;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("변경 사유")
    private GradeChangeReason reason;

    @Comment("등급을 준 주문 ID")
    private Long orderId;

    @Comment("변경한 관리자")
    private String changedBy;
}
