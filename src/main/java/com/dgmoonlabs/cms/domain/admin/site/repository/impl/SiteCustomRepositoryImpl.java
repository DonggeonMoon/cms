package com.dgmoonlabs.cms.domain.admin.site.repository.impl;

import com.dgmoonlabs.cms.domain.admin.site.entity.Site;
import com.dgmoonlabs.cms.domain.admin.site.repository.SiteCustomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dgmoonlabs.cms.domain.admin.site.entity.QSite.site;

@Repository
@RequiredArgsConstructor
public class SiteCustomRepositoryImpl implements SiteCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Site> search(final String keyword, final Pageable pageable) {
        List<Site> content = queryFactory.selectFrom(site)
                .where(site.deletedAt.isNull(), keywordContains(keyword))
                .orderBy(site.isDefault.desc(), site.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, () -> queryFactory.select(site.count())
                .from(site)
                .where(site.deletedAt.isNull(), keywordContains(keyword))
                .fetchOne());
    }

    private BooleanExpression keywordContains(final String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return site.name.containsIgnoreCase(keyword)
                .or(site.domain.containsIgnoreCase(keyword))
                .or(site.description.containsIgnoreCase(keyword));
    }
}
