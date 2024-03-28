package com.dgmoonlabs.cms.domain.admin.logging.dto;

import lombok.*;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class WatchdogLogRequest {
    private Long id;
    private String memberUsername;
    private String uri;
    private String methodName;
    private String content;
    private String ipAddress;
}
