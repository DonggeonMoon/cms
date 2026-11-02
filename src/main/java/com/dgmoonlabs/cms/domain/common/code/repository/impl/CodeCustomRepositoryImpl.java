package com.dgmoonlabs.cms.domain.common.code.repository.impl;

import com.dgmoonlabs.cms.domain.common.code.dto.CodeRequest;
import com.dgmoonlabs.cms.domain.common.code.entity.Code;
import com.dgmoonlabs.cms.domain.common.code.repository.CodeCustomRepository;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dgmoonlabs.cms.domain.common.code.entity.QCode.code;

@Repository
@RequiredArgsConstructor
public class CodeCustomRepositoryImpl implements CodeCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Code> find(final CodeRequest codeRequest, final Pageable pageable) {
        List<Code> content = queryFactory.selectFrom(code)
                .where(conditions(codeRequest))
                .orderBy(code.depth.asc(), code.order.asc(), code.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, () -> queryFactory.select(code.count())
                .from(code)
                .where(conditions(codeRequest))
                .fetchOne());
    }

    @Override
    public List<Code> find(final CodeRequest codeRequest) {
        return queryFactory.selectFrom(code)
                .where(conditions(codeRequest))
                .orderBy(code.order.asc(), code.id.asc())
                .fetch();
    }

    private Predicate[] conditions(final CodeRequest request) {
        return new Predicate[]{
                code.deletedAt.isNull(),
                parentCondition(request.parentId(), request.rootOnly()),
                keywordContains(request.keyword()),
                isUsingEquals(request.isUsing())
        };
    }

    private BooleanExpression parentCondition(final Long parentId, final Boolean rootOnly) {
        if (parentId != null) {
            return code.parentId.eq(parentId);
        }
        return Boolean.TRUE.equals(rootOnly) ? code.parentId.isNull() : null;
    }

    private BooleanExpression keywordContains(final String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return code.name.containsIgnoreCase(keyword)
                .or(code.englishName.containsIgnoreCase(keyword))
                .or(code.description.containsIgnoreCase(keyword));
    }

    private BooleanExpression isUsingEquals(final Boolean isUsing) {
        return isUsing == null ? null : code.isUsing.eq(isUsing);
    }
}
