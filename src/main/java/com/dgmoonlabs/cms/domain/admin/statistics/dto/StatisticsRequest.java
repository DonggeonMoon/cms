package com.dgmoonlabs.cms.domain.admin.statistics.dto;

import com.dgmoonlabs.cms.domain.admin.statistics.entity.Statistics;
import lombok.*;

@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class StatisticsRequest {
    private Long id;
    private String nationCode;
    private String os;
    private String browser;
    private String url;
    private int count;

    public Statistics toEntity() {
        return Statistics.builder()
                .nationCode(nationCode)
                .os(os)
                .browser(browser)
                .url(url)
                .count(count)
                .build();
    }
}
