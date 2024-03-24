package com.dgmoonlabs.cms.domain.admin.config.repository.impl;

import com.dgmoonlabs.cms.domain.admin.config.constant.OptionType;
import com.dgmoonlabs.cms.domain.admin.config.dto.ConfigRequest;
import com.dgmoonlabs.cms.domain.admin.config.entity.Config;
import com.dgmoonlabs.cms.domain.admin.config.repository.ConfigCustomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dgmoonlabs.cms.domain.admin.config.entity.QConfig.config;

@Service
@RequiredArgsConstructor
public class ConfigCustomRepositoryImpl implements ConfigCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Config> find(ConfigRequest request, Pageable pageable) {
        return queryFactory.select(config)
                .from(config)
                .where(
                        keyEquals(request.getKey()),
                        valueEquals(request.getValue()),
                        descriptionEquals(request.getDescription()),
                        typeEquals(request.getType()),
                        isHiddenEquals(request.isHidden())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<Config> find(ConfigRequest request) {
        return queryFactory.select(config)
                .from(config)
                .where(
                        keyEquals(request.getKey()),
                        valueEquals(request.getValue()),
                        descriptionEquals(request.getDescription()),
                        typeEquals(request.getType()),
                        isHiddenEquals(request.isHidden())
                )
                .fetch();
    }

    private BooleanExpression keyEquals(final String key) {
        return checkIfEmpty(key) ? config.key.eq(key) : null;
    }

    private BooleanExpression valueEquals(final String value) {
        return checkIfEmpty(value) ? config.value.contains(value) : null;
    }

    private BooleanExpression typeEquals(final OptionType type) {
        return config.type.eq(type);
    }

    private BooleanExpression descriptionEquals(final String domain) {
        return checkIfEmpty(domain) ? config.description.eq(domain) : null;
    }

    private BooleanExpression isHiddenEquals(final boolean isHidden) {
        return config.isHidden.eq(isHidden);
    }

    private boolean checkIfEmpty(final String input) {
        return input == null || input.isEmpty();
    }
}