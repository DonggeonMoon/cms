package com.dgmoonlabs.cms.domain.admin.statistics.service;

import com.dgmoonlabs.cms.domain.admin.statistics.dto.StatisticsRequest;
import com.dgmoonlabs.cms.domain.admin.statistics.entity.Statistics;
import com.dgmoonlabs.cms.domain.admin.statistics.repository.StatisticsRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua_parser.Client;
import ua_parser.Parser;

import java.time.LocalDate;

import static com.dgmoonlabs.cms.domain.admin.statistics.entity.QStatistics.statistics;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final StatisticsRepository statisticsRepository;
    private final JPAQueryFactory queryFactory;

    @Transactional
    public void saveStatistics(StatisticsRequest request) {
        statisticsRepository.save(request.toEntity());
    }

    @Transactional
    public void getStatistics(Statistics statistics) {
        statisticsRepository.findAll();
    }

    @Transactional
    public void getStatistics(long id) {
        statisticsRepository.findById(id);
    }

    @Transactional
    public void updateStatistics(HttpServletRequest request) {
        String userAgent = request.getHeader("USER-AGENT");

        Parser uaParser = new Parser();
        Client client = uaParser.parse(userAgent);

        String nationCode = "";
        String os = client.os.family;
        String browser = client.userAgent.family;
        String url = request.getRequestURI();

        Statistics result = queryFactory.select(statistics)
                .from(statistics)
                .where(
                        statistics.date.eq(LocalDate.now()),
                        statistics.nationCode.eq(nationCode),
                        statistics.os.eq(os),
                        statistics.browser.eq(browser),
                        statistics.url.eq(url)
                ).fetchOne();
        if (result == null) {
            statisticsRepository.save(
                    Statistics.builder()
                            .date(LocalDate.now())
                            .nationCode(nationCode)
                            .os(os)
                            .browser(browser)
                            .count(1)
                            .build()
            );
            return;
        }
        result.increaseCount();
    }
}
