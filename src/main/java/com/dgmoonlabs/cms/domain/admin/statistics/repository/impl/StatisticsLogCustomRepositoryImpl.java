package com.dgmoonlabs.cms.domain.admin.statistics.repository.impl;

import com.dgmoonlabs.cms.domain.admin.statistics.dto.StatisticsLogRequest;
import com.dgmoonlabs.cms.domain.admin.statistics.entity.StatisticsLog;
import com.dgmoonlabs.cms.domain.admin.statistics.repository.StatisticsLogCustomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dgmoonlabs.cms.domain.admin.statistics.entity.QStatisticsLog.statisticsLog;

@Repository
@RequiredArgsConstructor
public class StatisticsLogCustomRepositoryImpl implements StatisticsLogCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<StatisticsLog> find(StatisticsLogRequest statisticsLogRequest, Pageable pageable) {
        List<StatisticsLog> content = queryFactory.select(statisticsLog)
                .from(statisticsLog)
                .where(
                        memberUsernameEquals(statisticsLogRequest.getMemberUsername()),
                        urlEquals(statisticsLogRequest.getUrl()),
                        requestMethodEquals(statisticsLogRequest.getRequestMethod()),
                        refererEquals(statisticsLogRequest.getReferer()),
                        ipAddressEquals(statisticsLogRequest.getIpAddress())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(statisticsLog.count())
                .from(statisticsLog)
                .where(
                        memberUsernameEquals(statisticsLogRequest.getMemberUsername()),
                        urlEquals(statisticsLogRequest.getUrl()),
                        requestMethodEquals(statisticsLogRequest.getRequestMethod()),
                        refererEquals(statisticsLogRequest.getReferer()),
                        ipAddressEquals(statisticsLogRequest.getIpAddress())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<StatisticsLog> find(StatisticsLogRequest statisticsLogRequest) {
        return queryFactory.select(statisticsLog)
                .from(statisticsLog)
                .where(
                        memberUsernameEquals(statisticsLogRequest.getMemberUsername()),
                        urlEquals(statisticsLogRequest.getUrl()),
                        requestMethodEquals(statisticsLogRequest.getRequestMethod()),
                        refererEquals(statisticsLogRequest.getReferer()),
                        ipAddressEquals(statisticsLogRequest.getIpAddress())
                )
                .fetch();
    }

    private BooleanExpression memberUsernameEquals(final String memberUsername) {
        return checkIfEmpty(memberUsername) ? statisticsLog.memberUsername.eq(memberUsername) : null;
    }

    private BooleanExpression urlEquals(final String url) {
        return checkIfEmpty(url) ? statisticsLog.url.eq(url) : null;
    }

    private BooleanExpression requestMethodEquals(final String requestMethod) {
        return checkIfEmpty(requestMethod) ? statisticsLog.requestMethod.eq(requestMethod) : null;
    }

    private BooleanExpression refererEquals(final String referer) {
        return checkIfEmpty(referer) ? statisticsLog.referer.eq(referer) : null;
    }

    private BooleanExpression ipAddressEquals(final String ipAddress) {
        return checkIfEmpty(ipAddress) ? statisticsLog.ipAddress.eq(ipAddress) : null;
    }

    private boolean checkIfEmpty(final String input) {
        return input != null && input.isEmpty();
    }
}
