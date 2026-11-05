package com.dgmoonlabs.cms.domain.business.vote.repository;

import com.dgmoonlabs.cms.domain.business.vote.entity.Vote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Page<Vote> findAllByOrderByIdDesc(Pageable pageable);
}
