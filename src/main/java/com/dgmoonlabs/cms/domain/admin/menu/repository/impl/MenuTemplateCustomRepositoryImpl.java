package com.dgmoonlabs.cms.domain.admin.menu.repository.impl;

import com.dgmoonlabs.cms.domain.admin.menu.dto.MenuTemplateRequest;
import com.dgmoonlabs.cms.domain.admin.menu.entity.MenuTemplate;
import com.dgmoonlabs.cms.domain.admin.menu.repository.MenuTemplateCustomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dgmoonlabs.cms.domain.admin.menu.entity.QMenuTemplate.menuTemplate1;


@Repository
@RequiredArgsConstructor
public class MenuTemplateCustomRepositoryImpl implements MenuTemplateCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MenuTemplate> find(MenuTemplateRequest request, Pageable pageable) {
        List<MenuTemplate> content = queryFactory.select(menuTemplate1)
                .from(menuTemplate1)
                .where(
                        menuHeaderEquals(request.getMenuHeader()),
                        menuTemplateEquals(request.getMenuTemplate()),
                        menuFooterEquals(request.getMenuFooter())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(menuTemplate1.count())
                .from(menuTemplate1)
                .where(
                        menuHeaderEquals(request.getMenuHeader()),
                        menuTemplateEquals(request.getMenuTemplate()),
                        menuFooterEquals(request.getMenuFooter())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<MenuTemplate> find(MenuTemplateRequest request) {
        return queryFactory.select(menuTemplate1)
                .from(menuTemplate1)
                .where(
                        menuHeaderEquals(request.getMenuHeader()),
                        menuTemplateEquals(request.getMenuTemplate()),
                        menuFooterEquals(request.getMenuFooter())
                )
                .fetch();
    }

    private BooleanExpression menuHeaderEquals(final String menuHeader) {
        return checkIfEmpty(menuHeader) ? menuTemplate1.name.eq(menuHeader) : null;
    }

    private BooleanExpression menuTemplateEquals(final String menuTemplate) {
        return checkIfEmpty(menuTemplate) ? menuTemplate1.menuTemplate.eq(menuTemplate) : null;
    }

    private BooleanExpression menuFooterEquals(final String menuFooter) {
        return checkIfEmpty(menuFooter) ? menuTemplate1.menuFooter.eq(menuFooter) : null;
    }

    private boolean checkIfEmpty(final String input) {
        return input == null || input.isEmpty();
    }
}
