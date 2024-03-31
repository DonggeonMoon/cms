package com.dgmoonlabs.cms.domain.common.user.service;

import com.dgmoonlabs.cms.domain.admin.statistics.dto.StatisticsRequest;
import com.dgmoonlabs.cms.domain.admin.statistics.entity.Statistics;
import com.dgmoonlabs.cms.domain.admin.statistics.repository.StatisticsRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua_parser.Client;
import ua_parser.Parser;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final StatisticsRepository statisticsRepository;

    @Transactional(readOnly = true)
    public Page<Statistics> getStatistics(StatisticsRequest statistics, Pageable pageable) {
        return statisticsRepository.find(statistics, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Statistics> getStatisticsWithoutPaging(Statistics statistics, Pageable pageable) {
        return statisticsRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Statistics getStatistics(long id) {
        return statisticsRepository.findById(id).orElseThrow(RuntimeException::new);
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

        List<Statistics> results = statisticsRepository.find(
                StatisticsRequest.builder()
                        .date(LocalDate.now())
                        .nationCode(nationCode)
                        .os(os)
                        .browser(browser)
                        .url(url)
                        .build()
        );

        if (results.isEmpty()) {
            statisticsRepository.save(
                    Statistics.builder()
                            .date(LocalDate.now())
                            .nationCode(nationCode)
                            .os(os)
                            .browser(browser)
                            .url(url)
                            .count(1)
                            .build()
            );
            return;
        }

        Statistics result = results.get(0);
        result.increaseCount();
    }
}
