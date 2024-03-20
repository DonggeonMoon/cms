package com.dgmoonlabs.cms.domain.admin.statistics.repository.impl;

import com.dgmoonlabs.cms.domain.admin.statistics.dto.StatisticsRequest;
import com.dgmoonlabs.cms.domain.admin.statistics.entity.Statistics;
import com.dgmoonlabs.cms.domain.admin.statistics.repository.StatisticsCustomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dgmoonlabs.cms.domain.admin.statistics.entity.QStatistics.statistics;


@Repository
@RequiredArgsConstructor
public class StatisticsCustomRepositoryImpl implements StatisticsCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Statistics> find(StatisticsRequest statisticsRequest, Pageable pageable) {
        return queryFactory.select(statistics)
                .from(statistics)
                .where(
                        nationCodeEquals(statisticsRequest.getNationCode()),
                        osEquals(statisticsRequest.getOs()),
                        urlEquals(statisticsRequest.getUrl()),
                        browserEquals(statisticsRequest.getBrowser()),
                        countEquals(statisticsRequest.getCount())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<Statistics> find(StatisticsRequest statisticsRequest) {
        return queryFactory.select(statistics)
                .from(statistics)
                .where(
                        nationCodeEquals(statisticsRequest.getNationCode()),
                        osEquals(statisticsRequest.getOs()),
                        urlEquals(statisticsRequest.getUrl()),
                        browserEquals(statisticsRequest.getBrowser()),
                        countEquals(statisticsRequest.getCount())
                )
                .fetch();
    }

    private BooleanExpression osEquals(final String os) {
        return checkIfEmpty(os) ? statistics.os.eq(os) : null;
    }

    private BooleanExpression nationCodeEquals(final String nationCode) {
        return checkIfEmpty(nationCode) ? statistics.nationCode.eq(nationCode) : null;
    }

    private BooleanExpression browserEquals(final String browser) {
        return checkIfEmpty(browser) ? statistics.browser.eq(browser) : null;
    }

    private BooleanExpression urlEquals(final String url) {
        return checkIfEmpty(url) ? statistics.os.eq(url) : null;
    }

    private BooleanExpression countEquals(final Integer count) {
        return checkIfNull(count) ? statistics.count.eq(count) : null;
    }

    private boolean checkIfEmpty(final String input) {
        return input == null || input.isEmpty();
    }

    private boolean checkIfNull(final Integer input) {
        return input == null;
    }
}
