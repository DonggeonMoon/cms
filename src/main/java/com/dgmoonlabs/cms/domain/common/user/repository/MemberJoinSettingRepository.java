package com.dgmoonlabs.cms.domain.common.user.repository;

import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberJoinSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberJoinSettingRepository extends JpaRepository<MemberJoinSetting, Long> {
    Optional<MemberJoinSetting> findFirstByOrderByIdAsc();
}
