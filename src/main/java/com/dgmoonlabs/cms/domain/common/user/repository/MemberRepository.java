package com.dgmoonlabs.cms.domain.common.user.repository;

import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
