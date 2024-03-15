package com.dgmoonlabs.cms.domain.common.user.entity.guest.entity;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

@Entity
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class AuthenticatedGuest extends Guest {
    @Comment("연계 정보(CI)")
    private String connectionInfo;

    @Comment("중복 정보(DI)")
    private String duplicationInfo;
}
