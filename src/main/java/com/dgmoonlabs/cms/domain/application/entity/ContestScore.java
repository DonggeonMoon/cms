package com.dgmoonlabs.cms.domain.application.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

@Entity
@Table(indexes = @Index(name = "idx_contest_score_submission", columnList = "submissionId"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"submissionId", "judgeMemberId"}))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ContestScore extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("접수 ID")
    private Long submissionId;

    @Column(nullable = false)
    @Comment("심사위원 회원 ID")
    private Long judgeMemberId;

    @Column(nullable = false, precision = 5, scale = 2)
    @Comment("점수 (0~100)")
    private BigDecimal score;

    @Column(columnDefinition = "text")
    @Comment("심사평")
    private String comment;

    public void update(final BigDecimal score, final String comment) {
        this.score = score;
        this.comment = comment;
    }
}
