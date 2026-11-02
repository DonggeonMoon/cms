package com.dgmoonlabs.cms.domain.common.user.repository;

import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberAgreementRepository extends JpaRepository<MemberAgreement, Long> {
    Optional<MemberAgreement> findByMemberId(Long memberId);
}
