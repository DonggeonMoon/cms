package com.dgmoonlabs.cms.domain.admin.logging.repository.impl;

import com.dgmoonlabs.cms.domain.admin.logging.dto.WatchdogLogRequest;
import com.dgmoonlabs.cms.domain.admin.logging.entity.WatchdogLog;
import com.dgmoonlabs.cms.domain.admin.logging.repository.WatchdogCustomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dgmoonlabs.cms.domain.admin.logging.entity.QWatchdogLog.watchdogLog;


@Repository
@RequiredArgsConstructor
public class WatchdogLogRepositoryImpl implements WatchdogCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<WatchdogLog> find(WatchdogLogRequest request, Pageable pageable) {
        List<WatchdogLog> content = queryFactory.select(watchdogLog)
                .from(watchdogLog)
                .where(
                        memberUsernameEquals(request.getMemberUsername()),
                        uriEquals(request.getUri()),
                        methodNameEquals(request.getMethodName()),
                        contentLike(request.getContent()),
                        ipAddressEquals(request.getIpAddress())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(watchdogLog.count())
                .from(watchdogLog)
                .where(
                        memberUsernameEquals(request.getMemberUsername()),
                        uriEquals(request.getUri()),
                        methodNameEquals(request.getMethodName()),
                        contentLike(request.getContent()),
                        ipAddressEquals(request.getIpAddress())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<WatchdogLog> find(WatchdogLogRequest request) {
        return queryFactory.select(watchdogLog)
                .from(watchdogLog)
                .where(
                        memberUsernameEquals(request.getMemberUsername()),
                        uriEquals(request.getUri()),
                        methodNameEquals(request.getMethodName()),
                        contentLike(request.getContent()),
                        ipAddressEquals(request.getIpAddress())
                )
                .fetch();
    }

    private BooleanExpression memberUsernameEquals(final String memberUsername) {
        return checkIfEmpty(memberUsername) ? watchdogLog.memberUsername.eq(memberUsername) : null;
    }

    private BooleanExpression uriEquals(final String uri) {
        return checkIfEmpty(uri) ? watchdogLog.uri.eq(uri) : null;
    }

    private BooleanExpression methodNameEquals(final String methodName) {
        return checkIfEmpty(methodName) ? watchdogLog.methodName.like(methodName) : null;
    }

    private BooleanExpression contentLike(final String content) {
        return watchdogLog.content.eq(content);
    }

    private BooleanExpression ipAddressEquals(final String ipAddress) {
        return watchdogLog.ipAddress.eq(ipAddress);
    }


    private boolean checkIfEmpty(final String input) {
        return input == null || input.isEmpty();
    }
}
