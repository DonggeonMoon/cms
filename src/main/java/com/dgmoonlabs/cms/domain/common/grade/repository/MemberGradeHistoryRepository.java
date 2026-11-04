package com.dgmoonlabs.cms.domain.common.grade.repository;

import com.dgmoonlabs.cms.domain.common.grade.entity.MemberGradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberGradeHistoryRepository extends JpaRepository<MemberGradeHistory, Long> {
    List<MemberGradeHistory> findTop50ByMemberIdOrderByIdDesc(Long memberId);

    Optional<MemberGradeHistory> findFirstByMemberIdOrderByIdDesc(Long memberId);
}
