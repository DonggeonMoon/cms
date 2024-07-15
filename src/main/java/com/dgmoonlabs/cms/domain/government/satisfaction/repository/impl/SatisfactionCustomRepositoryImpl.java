package com.dgmoonlabs.cms.domain.government.satisfaction.repository.impl;

import com.dgmoonlabs.cms.domain.government.satisfaction.dto.SatisfactionRequest;
import com.dgmoonlabs.cms.domain.government.satisfaction.entity.Satisfaction;
import com.dgmoonlabs.cms.domain.government.satisfaction.repository.SatisfactionCustomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dgmoonlabs.cms.domain.government.satisfaction.entity.QSatisfaction.satisfaction;


@Repository
@RequiredArgsConstructor
public class SatisfactionCustomRepositoryImpl implements SatisfactionCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Satisfaction> find(SatisfactionRequest satisfactionRequest, Pageable pageable) {
        List<Satisfaction> content = queryFactory.select(satisfaction)
                .from(satisfaction)
                .where(
                        menuIdEquals(satisfactionRequest.getMenuId())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(satisfaction.count())
                .from(satisfaction)
                .where(
                        menuIdEquals(satisfactionRequest.getMenuId())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<Satisfaction> find(SatisfactionRequest satisfactionRequest) {
        return queryFactory.select(satisfaction)
                .from(satisfaction)
                .where(
                        menuIdEquals(satisfactionRequest.getMenuId())
                )
                .fetch();
    }

    private BooleanExpression menuIdEquals(final Long menuId) {
        return checkIfEmpty(menuId) ? satisfaction.menuId.eq(menuId) : null;
    }

    private boolean checkIfEmpty(final Long input) {
        return input == null;
    }
}
