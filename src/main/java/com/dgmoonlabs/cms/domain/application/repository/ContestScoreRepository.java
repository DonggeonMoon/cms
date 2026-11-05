package com.dgmoonlabs.cms.domain.application.repository;

import com.dgmoonlabs.cms.domain.application.entity.ContestScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ContestScoreRepository extends JpaRepository<ContestScore, Long> {
    Optional<ContestScore> findBySubmissionIdAndJudgeMemberId(Long submissionId, Long judgeMemberId);

    List<ContestScore> findAllBySubmissionId(Long submissionId);

    List<ContestScore> findAllBySubmissionIdIn(Collection<Long> submissionIds);
}
