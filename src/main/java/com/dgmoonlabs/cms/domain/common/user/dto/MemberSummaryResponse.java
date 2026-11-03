package com.dgmoonlabs.cms.domain.common.user.dto;

import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;

import java.time.LocalDateTime;

public record MemberSummaryResponse(
        Long id,
        String username,
        String nickname,
        String name,
        String email,
        boolean isLocked,
        LocalDateTime lastLoginDateTime,
        LocalDateTime createdAt
) {
    public static MemberSummaryResponse of(final Member member, final int maxLoginTryCount) {
        return new MemberSummaryResponse(
                member.getId(),
                member.getUsername(),
                member.getNickname(),
                member.getName(),
                member.getEmail(),
                member.isLocked(maxLoginTryCount),
                member.getLastLoginDateTime(),
                member.getCreatedAt()
        );
    }
}
