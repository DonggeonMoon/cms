package com.dgmoonlabs.cms.domain.admin.config.repository.impl;

import com.dgmoonlabs.cms.domain.admin.config.constant.OptionType;
import com.dgmoonlabs.cms.domain.admin.config.dto.ConfigRequest;
import com.dgmoonlabs.cms.domain.admin.config.entity.Config;
import com.dgmoonlabs.cms.domain.admin.config.repository.ConfigCustomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dgmoonlabs.cms.domain.admin.config.entity.QConfig.config;


@Repository
@RequiredArgsConstructor
public class ConfigCustomRepositoryImpl implements ConfigCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Config> find(ConfigRequest request, Pageable pageable) {
        List<Config> content = queryFactory.select(config)
                .from(config)
                .where(
                        keyEquals(request.getKey()),
                        valueEquals(request.getValue()),
                        descriptionLike(request.getDescription()),
                        typeEquals(request.getType()),
                        isHiddenEquals(request.isHidden())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(config.count())
                .from(config)
                .where(
                        keyEquals(request.getKey()),
                        valueEquals(request.getValue()),
                        descriptionLike(request.getDescription()),
                        typeEquals(request.getType()),
                        isHiddenEquals(request.isHidden())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<Config> find(ConfigRequest request) {
        return queryFactory.select(config)
                .from(config)
                .where(
                        keyEquals(request.getKey()),
                        valueEquals(request.getValue()),
                        descriptionLike(request.getDescription()),
                        typeEquals(request.getType()),
                        isHiddenEquals(request.isHidden())
                )
                .fetch();
    }

    private BooleanExpression keyEquals(final String key) {
        return checkIfEmpty(key) ? config.key.eq(key) : null;
    }

    private BooleanExpression valueEquals(final String value) {
        return checkIfEmpty(value) ? config.value.eq(value) : null;
    }

    private BooleanExpression descriptionLike(final String description) {
        return checkIfEmpty(description) ? config.description.like(description) : null;
    }

    private BooleanExpression typeEquals(final OptionType type) {
        return config.type.eq(type);
    }

    private BooleanExpression isHiddenEquals(final boolean isHidden) {
        return config.isHidden.eq(isHidden);
    }


    private boolean checkIfEmpty(final String input) {
        return input == null || input.isEmpty();
    }
}
