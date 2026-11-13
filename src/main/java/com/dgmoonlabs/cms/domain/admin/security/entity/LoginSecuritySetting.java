package com.dgmoonlabs.cms.domain.admin.security.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class LoginSecuritySetting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @ColumnDefault("5")
    @Comment("로그인 실패 허용 횟수 (초과하면 계정 잠김)")
    private int maxLoginTryCount;

    public void changeMaxLoginTryCount(final int maxLoginTryCount) {
        this.maxLoginTryCount = maxLoginTryCount;
    }
}
