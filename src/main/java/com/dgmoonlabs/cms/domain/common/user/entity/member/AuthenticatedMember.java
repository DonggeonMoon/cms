package com.dgmoonlabs.cms.domain.common.user.entity.member;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class AuthenticatedMember extends Member {
    @Comment("연계 정보(CI)")
    private String connectionInfo;

    @Comment("연계 정보(CI)")
    private String duplicationInfo;
}
