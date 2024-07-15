package com.dgmoonlabs.cms.domain.banner.repository.impl;

import com.dgmoonlabs.cms.domain.banner.constant.BannerType;
import com.dgmoonlabs.cms.domain.banner.dto.BannerRequest;
import com.dgmoonlabs.cms.domain.banner.entity.Banner;
import com.dgmoonlabs.cms.domain.banner.repository.BannerCustomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.dgmoonlabs.cms.domain.banner.entity.QBanner.banner;

@Repository
@RequiredArgsConstructor
public class BannerCustomRepositoryImpl implements BannerCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Banner> find(BannerRequest bannerRequest, Pageable pageable) {
        List<Banner> content = queryFactory.select(banner)
                .from(banner)
                .where(
                        typeEquals(bannerRequest.getType()),
                        orderEquals(bannerRequest.getOrder()),
                        nameEquals(bannerRequest.getName()),
                        descriptionContains(bannerRequest.getDescription()),
                        linkEquals(bannerRequest.getLink()),
                        openNewWindowsEquals(bannerRequest.getOpensNewWindow()),
                        dateBetween(bannerRequest.getStartDateTime(), bannerRequest.getEndDateTime()),
                        isUsingEquals(bannerRequest.getIsUsing())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(banner.count())
                .from(banner)
                .where(
                        typeEquals(bannerRequest.getType()),
                        orderEquals(bannerRequest.getOrder()),
                        nameEquals(bannerRequest.getName()),
                        descriptionContains(bannerRequest.getDescription()),
                        linkEquals(bannerRequest.getLink()),
                        openNewWindowsEquals(bannerRequest.getOpensNewWindow()),
                        dateBetween(bannerRequest.getStartDateTime(), bannerRequest.getEndDateTime()),
                        isUsingEquals(bannerRequest.getIsUsing())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<Banner> find(BannerRequest bannerRequest) {
        return queryFactory.select(banner)
                .from(banner)
                .where(
                        typeEquals(bannerRequest.getType()),
                        orderEquals(bannerRequest.getOrder()),
                        nameEquals(bannerRequest.getName()),
                        descriptionContains(bannerRequest.getDescription()),
                        linkEquals(bannerRequest.getLink()),
                        openNewWindowsEquals(bannerRequest.getOpensNewWindow()),
                        dateBetween(bannerRequest.getStartDateTime(), bannerRequest.getEndDateTime()),
                        isUsingEquals(bannerRequest.getIsUsing())
                )
                .fetch();
    }

    private BooleanExpression orderEquals(final int order) {
        return banner.order.eq(order);
    }

    private BooleanExpression typeEquals(final BannerType type) {
        return checkIfEmpty(type) ? banner.type.eq(type) : null;
    }

    private BooleanExpression descriptionContains(final String description) {
        return checkIfEmpty(description) ? banner.description.contains(description) : null;
    }

    private BooleanExpression nameEquals(final String name) {
        return checkIfEmpty(name) ? banner.name.eq(name) : null;
    }

    private BooleanExpression isUsingEquals(final Boolean isUsing) {
        return checkIfEmpty(isUsing) ? banner.isUsing.eq(isUsing) : null;
    }

    private BooleanExpression linkEquals(final String link) {
        return checkIfEmpty(link) ? banner.link.eq(link) : null;
    }

    private BooleanExpression openNewWindowsEquals(final Boolean opensNewWindow) {
        return checkIfEmpty(opensNewWindow) ? banner.opensNewWindow.isTrue() : null;
    }

    private BooleanExpression dateBetween(final LocalDateTime startDateTime, final LocalDateTime endDateTime) {
        return banner.startDateTime.after(startDateTime).and(banner.endDateTime.before(endDateTime));
    }

    private boolean checkIfEmpty(final String input) {
        return input == null || input.isEmpty();
    }

    private boolean checkIfEmpty(final BannerType type) {
        return type == null;
    }

    private boolean checkIfEmpty(final Boolean input) {
        return input == null;
    }

}
