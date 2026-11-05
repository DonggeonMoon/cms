package com.dgmoonlabs.cms.domain.business.vote.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(indexes = @Index(name = "idx_vote_ballot_vote", columnList = "voteId"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"voteId", "memberId"}))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class VoteBallot extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("투표 ID")
    private Long voteId;

    @Column(nullable = false)
    @Comment("투표한 회원 ID")
    private Long memberId;

    @Column(nullable = false)
    @Comment("선택한 선택지 인덱스")
    private int optionIndex;
}
