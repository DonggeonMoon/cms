package com.dgmoonlabs.cms.domain.admin.menu.repository.impl;

import com.dgmoonlabs.cms.domain.admin.menu.constant.MenuType;
import com.dgmoonlabs.cms.domain.admin.menu.dto.MenuRequest;
import com.dgmoonlabs.cms.domain.admin.menu.entity.Menu;
import com.dgmoonlabs.cms.domain.admin.menu.repository.MenuCustomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dgmoonlabs.cms.domain.admin.menu.entity.QMenu.menu;


@Repository
@RequiredArgsConstructor
public class MenuCustomRepositoryImpl implements MenuCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Menu> find(MenuRequest request, Pageable pageable) {
        List<Menu> content = queryFactory.select(menu)
                .from(menu)
                .where(
                        nameEquals(request.getName()),
                        typeEquals(request.getType()),
                        parentIdEquals(request.getParentId()),
                        depthEquals(request.getDepth()),
                        linkEquals(request.getLink())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(menu.count())
                .from(menu)
                .where(
                        nameEquals(request.getName()),
                        typeEquals(request.getType()),
                        parentIdEquals(request.getParentId()),
                        depthEquals(request.getDepth()),
                        linkEquals(request.getLink())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<Menu> find(MenuRequest request) {
        return queryFactory.select(menu)
                .from(menu)
                .where(
                        nameEquals(request.getName()),
                        typeEquals(request.getType()),
                        parentIdEquals(request.getParentId()),
                        depthEquals(request.getDepth()),
                        linkEquals(request.getLink())
                )
                .fetch();
    }

    private BooleanExpression nameEquals(final String name) {
        return checkIfEmpty(name) ? menu.name.eq(name) : null;
    }

    private BooleanExpression typeEquals(final MenuType type) {
        return menu.type.eq(type);
    }

    private BooleanExpression parentIdEquals(final Long parentId) {
        return menu.parentId.eq(parentId);
    }

    private BooleanExpression depthEquals(final int depth) {
        return menu.depth.eq(depth);
    }

    private BooleanExpression linkEquals(final String link) {
        return checkIfEmpty(link) ? menu.link.eq(link) : null;
    }


    private boolean checkIfEmpty(final String input) {
        return input == null || input.isEmpty();
    }
}
