package com.dgmoonlabs.cms.domain.common.notification.entity;

import com.dgmoonlabs.cms.domain.common.notification.constant.NotificationType;
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
public class NotificationTypeSetting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "varchar(30)")
    @Enumerated(EnumType.STRING)
    @Comment("알림 종류")
    private NotificationType type;

    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("이벤트가 생기면 자동으로 보낼지 여부")
    private boolean enabled;

    public void update(final boolean enabled) {
        this.enabled = enabled;
    }
}
