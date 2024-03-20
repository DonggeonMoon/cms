package com.dgmoonlabs.cms.domain.admin.statistics.dto;

import com.dgmoonlabs.cms.domain.admin.statistics.entity.Statistics;
import lombok.*;

import java.time.LocalDate;

@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class StatisticsRequest {
    private Long id;
    private LocalDate date;
    private String nationCode;
    private String os;
    private String browser;
    private String url;

    public Statistics toEntity() {
        return Statistics.builder()
                .date(date)
                .nationCode(nationCode)
                .os(os)
                .browser(browser)
                .url(url)
                .build();
    }
}
