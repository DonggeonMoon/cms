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
        String gradeName,
        LocalDateTime gradeExpiresAt,
        LocalDateTime lastLoginDateTime,
        LocalDateTime createdAt
) {
    public static MemberSummaryResponse of(final Member member, final int maxLoginTryCount, final String gradeName) {
        return new MemberSummaryResponse(
                member.getId(),
                member.getUsername(),
                member.getNickname(),
                member.getName(),
                member.getEmail(),
                member.isLocked(maxLoginTryCount),
                gradeName,
                member.isGradeExpired(LocalDateTime.now()) ? null : member.getGradeExpiresAt(),
                member.getLastLoginDateTime(),
                member.getCreatedAt()
        );
    }
}
