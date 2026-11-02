package com.dgmoonlabs.cms.domain.government.satisfaction.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_satisfaction_vote", columnNames = {"menuId", "memberId"}))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class SatisfactionVote extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("메뉴 ID")
    private Long menuId;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Column(nullable = false)
    @Comment("점수 (1~5)")
    private int score;

    @Comment("의견")
    private String opinion;
}
