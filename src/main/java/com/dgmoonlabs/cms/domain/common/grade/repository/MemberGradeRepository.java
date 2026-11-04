package com.dgmoonlabs.cms.domain.common.grade.repository;

import com.dgmoonlabs.cms.domain.common.grade.entity.MemberGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberGradeRepository extends JpaRepository<MemberGrade, Long> {
    List<MemberGrade> findAllByOrderByRankAscIdAsc();

    Optional<MemberGrade> findFirstByIsDefaultTrue();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
