package com.dgmoonlabs.cms.domain.common.user.entity.capability.entity;

import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Capability {
    @Id
    private Long id;

    @Comment("권한 이름")
    private String name;

    @Comment("권한 설명")
    private String description;

    @Comment("기본 여부")
    private boolean isDefault;

    @Comment("우선순위")
    private int priority;

    @Enumerated(EnumType.STRING)
    @Comment("사용자 타입")
    private UserType userType;

    @Comment("사용자 ID")
    private Long userId;
}
