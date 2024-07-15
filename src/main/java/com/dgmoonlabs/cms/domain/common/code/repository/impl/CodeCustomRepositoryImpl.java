package com.dgmoonlabs.cms.domain.common.code.repository.impl;

import com.dgmoonlabs.cms.domain.common.code.dto.CodeRequest;
import com.dgmoonlabs.cms.domain.common.code.entity.Code;
import com.dgmoonlabs.cms.domain.common.code.repository.CodeCustomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
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
    public Page<Code> find(CodeRequest codeRequest, Pageable pageable) {
        List<Code> content = queryFactory.select(code)
                .from(code)
                .where(
                        parentIdEquals(codeRequest.getParentId()),
                        depthEquals(codeRequest.getDepth()),
                        orderEquals(codeRequest.getOrder()),
                        nameEquals(codeRequest.getName()),
                        englishNameEquals(codeRequest.getEnglishName()),
                        depthEquals(codeRequest.getDepth()),
                        isUsingEquals(codeRequest.isUsing())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(code.count())
                .from(code)
                .where(
                        parentIdEquals(codeRequest.getParentId()),
                        depthEquals(codeRequest.getDepth()),
                        orderEquals(codeRequest.getOrder()),
                        nameEquals(codeRequest.getName()),
                        englishNameEquals(codeRequest.getEnglishName()),
                        depthEquals(codeRequest.getDepth()),
                        isUsingEquals(codeRequest.isUsing())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<Code> find(CodeRequest codeRequest) {
        return queryFactory.select(code)
                .from(code)
                .where(
                        parentIdEquals(codeRequest.getParentId()),
                        depthEquals(codeRequest.getDepth()),
                        orderEquals(codeRequest.getOrder()),
                        nameEquals(codeRequest.getName()),
                        englishNameEquals(codeRequest.getEnglishName()),
                        depthEquals(codeRequest.getDepth()),
                        isUsingEquals(codeRequest.isUsing())
                )
                .fetch();
    }

    private BooleanExpression depthEquals(final int depth) {
        return code.depth.eq(depth);
    }

    private BooleanExpression parentIdEquals(final long parentId) {
        return code.parentId.eq(parentId);
    }

    private BooleanExpression orderEquals(final int order) {
        return code.order.eq(order);
    }

    private BooleanExpression nameEquals(final String name) {
        return checkIfEmpty(name) ? code.name.eq(name) : null;
    }

    private BooleanExpression englishNameEquals(final String englishName) {
        return checkIfEmpty(englishName) ? code.englishName.eq(englishName) : null;
    }

    private BooleanExpression descriptionContains(final String description) {
        return checkIfEmpty(description) ? code.description.contains(description) : null;
    }

    private BooleanExpression isUsingEquals(final Boolean isUsing) {
        return checkIfEmpty(isUsing) ? code.isUsing.eq(isUsing) : null;
    }

    private boolean checkIfEmpty(final String input) {
        return input == null || input.isEmpty();
    }

    private boolean checkIfEmpty(final Boolean input) {
        return input == null;
    }
}
