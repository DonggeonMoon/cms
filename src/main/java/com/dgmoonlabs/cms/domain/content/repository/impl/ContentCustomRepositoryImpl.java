package com.dgmoonlabs.cms.domain.content.repository.impl;

import com.dgmoonlabs.cms.domain.admin.statistics.dto.StatisticsRequest;
import com.dgmoonlabs.cms.domain.admin.statistics.entity.Statistics;
import com.dgmoonlabs.cms.domain.admin.statistics.repository.StatisticsCustomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dgmoonlabs.cms.domain.admin.statistics.entity.QStatistics.statistics;


@Repository
@RequiredArgsConstructor
public class ContentCustomRepositoryImpl implements StatisticsCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Statistics> find(StatisticsRequest statisticsRequest, Pageable pageable) {
        List<Statistics> content = queryFactory.select(statistics)
                .from(statistics)
                .where(
                        nationCodeEquals(statisticsRequest.getNationCode()),
                        osEquals(statisticsRequest.getOs()),
                        urlEquals(statisticsRequest.getUrl()), browserEquals(statisticsRequest.getBrowser())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(statistics.count())
                .from(statistics)
                .where(
                        nationCodeEquals(statisticsRequest.getNationCode()),
                        osEquals(statisticsRequest.getOs()),
                        urlEquals(statisticsRequest.getUrl()), browserEquals(statisticsRequest.getBrowser())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<Statistics> find(StatisticsRequest statisticsRequest) {
        return queryFactory.select(statistics)
                .from(statistics)
                .where(
                        nationCodeEquals(statisticsRequest.getNationCode()),
                        osEquals(statisticsRequest.getOs()),
                        urlEquals(statisticsRequest.getUrl()), browserEquals(statisticsRequest.getBrowser())
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


    private boolean checkIfEmpty(final String input) {
        return input == null || input.isEmpty();
    }
}
