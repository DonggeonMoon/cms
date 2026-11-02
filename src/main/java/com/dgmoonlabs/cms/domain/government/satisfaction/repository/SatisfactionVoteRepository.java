package com.dgmoonlabs.cms.domain.government.satisfaction.repository;

import com.dgmoonlabs.cms.domain.government.satisfaction.entity.SatisfactionVote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SatisfactionVoteRepository extends JpaRepository<SatisfactionVote, Long> {
    Optional<SatisfactionVote> findByMenuIdAndMemberId(Long menuId, Long memberId);

    Page<SatisfactionVote> findAllByMenuIdAndOpinionIsNotNullOrderByIdDesc(Long menuId, Pageable pageable);

    void deleteAllByMenuId(Long menuId);
}
