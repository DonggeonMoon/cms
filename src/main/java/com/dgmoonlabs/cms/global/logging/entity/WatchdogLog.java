package com.dgmoonlabs.cms.global.logging.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class WatchdogLog {
    @Id
    private Long id;

    private String memberUsername;

    private String uri;

    private String methodName;

    private String content;

    private String ipAddress;
}
