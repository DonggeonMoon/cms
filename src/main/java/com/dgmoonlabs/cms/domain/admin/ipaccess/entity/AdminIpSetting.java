package com.dgmoonlabs.cms.domain.admin.ipaccess.entity;

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
public class AdminIpSetting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("허용 IP에서만 관리자 접속")
    private boolean enabled;

    public void changeEnabled(final boolean enabled) {
        this.enabled = enabled;
    }
}
