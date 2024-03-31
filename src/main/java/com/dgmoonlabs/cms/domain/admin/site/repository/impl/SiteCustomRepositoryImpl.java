package com.dgmoonlabs.cms.domain.admin.site.repository.impl;

import com.dgmoonlabs.cms.domain.admin.menu.constant.MenuType;
import com.dgmoonlabs.cms.domain.admin.site.dto.SiteRequest;
import com.dgmoonlabs.cms.domain.admin.site.entity.Site;
import com.dgmoonlabs.cms.domain.admin.site.repository.SiteCustomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dgmoonlabs.cms.domain.admin.site.entity.QSite.site;

@Service
@RequiredArgsConstructor
public class SiteCustomRepositoryImpl implements SiteCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Site> find(SiteRequest request, Pageable pageable) {
        return queryFactory.select(site)
                .from(site)
                .where(
                        nameEquals(request.getName()),
                        descriptionLike(request.getDescription()),
                        domainEquals(request.getDomain()),
                        themeEquals(request.getTheme()),
                        menuTypeEquals(request.getType()),
                        localeEquals(request.getLocale()),
                        isDefault(request.getIsDefault())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<Site> find(SiteRequest request) {
        return queryFactory.select(site)
                .from(site)
                .where(
                        nameEquals(request.getName()),
                        descriptionLike(request.getDescription()),
                        domainEquals(request.getDomain()),
                        themeEquals(request.getTheme()),
                        menuTypeEquals(request.getType()),
                        localeEquals(request.getLocale()),
                        isDefault(request.getIsDefault())
                )
                .fetch();
    }

    private BooleanExpression nameEquals(final String name) {
        return isNotEmpty(name) ? site.name.eq(name) : null;
    }

    private BooleanExpression descriptionLike(final String description) {
        return isNotEmpty(description) ? site.description.contains(description) : null;
    }

    private BooleanExpression domainEquals(final String domain) {
        return isNotEmpty(domain) ? site.domain.eq(domain) : null;
    }

    private BooleanExpression themeEquals(final String theme) {
        return isNotEmpty(theme) ? site.description.contains(theme) : null;
    }

    private BooleanExpression menuTypeEquals(final MenuType type) {
        return site.type.eq(type);
    }

    private BooleanExpression localeEquals(final String locale) {
        return isNotEmpty(locale) ? site.domain.eq(locale) : null;
    }

    private BooleanExpression isDefault(final Boolean isDefault) {
        return isNotEmpty(isDefault) ? site.isDefault.eq(isDefault) : null;
    }

    private boolean isNotEmpty(final String input) {
        return input != null && !input.isEmpty();
    }

    private boolean isNotEmpty(final Boolean input) {
        return input != null;
    }

}