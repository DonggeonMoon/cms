package com.dgmoonlabs.cms.domain.admin.statistics.entity;

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
public class StatisticsLog extends BaseEntity {
    @Id
    private Long id;

    @Comment("회원 아이디")
    private String memberUsername;

    @Comment("방문 URL")
    private String url;

    @Comment("HTTP 요청 메서드")
    private String requestMethod;

    @Comment("리퍼러")
    private String referer;

    @Comment("IP 주소")
    private String ipAddress;

    public void update(final String memberUsername, final String url, final String requestMethod, final String referer, final String ipAddress) {
        this.memberUsername = memberUsername;
        this.url = url;
        this.requestMethod = requestMethod;
        this.referer = referer;
        this.ipAddress = ipAddress;
    }
}
