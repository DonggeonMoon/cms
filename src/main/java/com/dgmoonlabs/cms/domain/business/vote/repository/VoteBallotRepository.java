package com.dgmoonlabs.cms.domain.business.vote.repository;

import com.dgmoonlabs.cms.domain.business.vote.entity.VoteBallot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoteBallotRepository extends JpaRepository<VoteBallot, Long> {
    Optional<VoteBallot> findByVoteIdAndMemberId(Long voteId, Long memberId);

    List<VoteBallot> findAllByVoteId(Long voteId);
}
