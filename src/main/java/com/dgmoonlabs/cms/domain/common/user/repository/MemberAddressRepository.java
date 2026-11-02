package com.dgmoonlabs.cms.domain.common.user.repository;

import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberAddressRepository extends JpaRepository<MemberAddress, Long> {
    Optional<MemberAddress> findByMemberId(Long memberId);
}
