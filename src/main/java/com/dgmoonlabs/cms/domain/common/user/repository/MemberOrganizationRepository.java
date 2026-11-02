package com.dgmoonlabs.cms.domain.common.user.repository;

import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberOrganization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberOrganizationRepository extends JpaRepository<MemberOrganization, Long> {
    Optional<MemberOrganization> findByMemberId(Long memberId);
}
