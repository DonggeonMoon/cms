package com.dgmoonlabs.cms.domain.admin.statistics.dto;

import com.dgmoonlabs.cms.domain.admin.statistics.entity.StatisticsLog;
import lombok.*;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class StatisticsLogRequest {
    private Long id;
    private String memberUsername;
    private String url;
    private String requestMethod;
    private String referer;
    private String ipAddress;

    public StatisticsLog toEntity() {
        return StatisticsLog.builder()
                .id(id)
                .memberUsername(memberUsername)
                .url(url)
                .requestMethod(requestMethod)
                .referer(referer)
                .ipAddress(ipAddress)
                .build();
    }
}
