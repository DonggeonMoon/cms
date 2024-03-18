package com.dgmoonlabs.cms.domain.admin.logging.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class WatchdogLog extends BaseEntity {
    @Id
    private Long id;

    @Comment("회원 아이디")
    private String memberUsername;

    @Comment("요청 URI")
    private String uri;

    @Comment("메서드명")
    private String methodName;

    @Comment("로그 내용")
    private String content;

    @Comment("IP 주소")
    private String ipAddress;
}
