package com.dgmoonlabs.cms.domain.admin.statistics.service;

import com.dgmoonlabs.cms.domain.admin.statistics.dto.StatisticsDtos.*;
import com.dgmoonlabs.cms.domain.admin.statistics.entity.Statistics;
import com.dgmoonlabs.cms.domain.admin.statistics.entity.StatisticsLog;
import com.dgmoonlabs.cms.domain.admin.statistics.mapper.StatisticsMapper;
import com.dgmoonlabs.cms.domain.admin.statistics.repository.StatisticsLogRepository;
import com.dgmoonlabs.cms.domain.admin.statistics.repository.StatisticsRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import com.dgmoonlabs.cms.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua_parser.Client;
import ua_parser.Parser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private static final int MAX_RANGE_DAYS = 366;
    private static final int BREAKDOWN_LIMIT = 20;
    private static final String UNKNOWN_NATION = "";

    private final Parser userAgentParser = new Parser();
    private final StatisticsRepository statisticsRepository;
    private final StatisticsLogRepository statisticsLogRepository;
    private final StatisticsMapper statisticsMapper;

    @Transactional
    public void recordVisit(final VisitRequest request, final String userAgent, final String ipAddress) {
        Client client = userAgentParser.parse(userAgent == null ? "" : userAgent);
        String os = client.os.family;
        String browser = client.userAgent.family;
        String path = normalize(request.path());
        LocalDate today = LocalDate.now();

        statisticsRepository.findFirstByDateAndNationCodeAndOsAndBrowserAndUrl(today, UNKNOWN_NATION, os, browser, path)
                .ifPresentOrElse(Statistics::increaseCount, () -> statisticsRepository.save(Statistics.builder()
                        .date(today)
                        .nationCode(UNKNOWN_NATION)
                        .os(os)
                        .browser(browser)
                        .url(path)
                        .count(1)
                        .build()));

        statisticsLogRepository.save(StatisticsLog.builder()
                .memberUsername(CurrentUser.get().map(LoginUser::username).orElse(null))
                .url(path)
                .requestMethod("VIEW")
                .referer(request.referrer())
                .ipAddress(ipAddress)
                .build());
    }

    @Transactional(readOnly = true)
    public List<DailyRow> dailyVisits(final LocalDate from, final LocalDate to) {
        validateRange(from, to);
        Map<LocalDate, Long> visits = statisticsMapper.dailyVisits(from, to).stream()
                .collect(Collectors.toMap(DailyRow::date, DailyRow::visits));
        List<DailyRow> series = new ArrayList<>();
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            series.add(new DailyRow(day, visits.getOrDefault(day, 0L)));
        }
        return series;
    }

    @Transactional(readOnly = true)
    public List<CountRow> breakdown(final Dimension dimension, final LocalDate from, final LocalDate to) {
        validateRange(from, to);
        Map<Dimension, Function<Integer, List<CountRow>>> queries = Map.of(
                Dimension.BROWSER, limit -> statisticsMapper.byBrowser(from, to, limit),
                Dimension.OS, limit -> statisticsMapper.byOs(from, to, limit),
                Dimension.URL, limit -> statisticsMapper.byUrl(from, to, limit)
        );
        return queries.get(dimension).apply(BREAKDOWN_LIMIT);
    }

    @Transactional(readOnly = true)
    public Page<VisitLogResponse> visitLogs(final Pageable pageable) {
        return statisticsLogRepository.findAllByOrderByIdDesc(pageable).map(VisitLogResponse::from);
    }

    private void validateRange(final LocalDate from, final LocalDate to) {
        if (from.isAfter(to) || from.plusDays(MAX_RANGE_DAYS).isBefore(to)) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD);
        }
    }

    private static String normalize(final String path) {
        String withoutQuery = path.split("[?#]", 2)[0];
        return withoutQuery.isBlank() ? "/" : withoutQuery;
    }
}
