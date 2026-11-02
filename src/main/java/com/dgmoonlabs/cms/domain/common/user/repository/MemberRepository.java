package com.dgmoonlabs.cms.domain.common.user.repository;

import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberCustomRepository {
    Optional<Member> findByUsernameAndDeletedAtIsNull(String username);

    Optional<Member> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByUsername(String username);

    long countByDeletedAtIsNull();
}
