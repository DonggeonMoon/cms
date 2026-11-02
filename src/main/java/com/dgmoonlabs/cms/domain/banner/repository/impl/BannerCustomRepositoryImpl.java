package com.dgmoonlabs.cms.domain.banner.repository.impl;

import com.dgmoonlabs.cms.domain.banner.constant.BannerType;
import com.dgmoonlabs.cms.domain.banner.entity.Banner;
import com.dgmoonlabs.cms.domain.banner.repository.BannerCustomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
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
    public Page<Banner> search(final String keyword, final BannerType type, final Pageable pageable) {
        List<Banner> content = queryFactory.selectFrom(banner)
                .where(banner.deletedAt.isNull(), nameContains(keyword), typeEquals(type))
                .orderBy(banner.order.asc(), banner.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, () -> queryFactory.select(banner.count())
                .from(banner)
                .where(banner.deletedAt.isNull(), nameContains(keyword), typeEquals(type))
                .fetchOne());
    }

    @Override
    public List<Banner> findActive(final BannerType type, final LocalDateTime now) {
        return queryFactory.selectFrom(banner)
                .where(
                        banner.deletedAt.isNull(),
                        banner.isUsing.isTrue(),
                        typeEquals(type),
                        banner.startDateTime.isNull().or(banner.startDateTime.loe(now)),
                        banner.endDateTime.isNull().or(banner.endDateTime.goe(now))
                )
                .orderBy(banner.order.asc(), banner.id.desc())
                .fetch();
    }

    private BooleanExpression nameContains(final String keyword) {
        return keyword == null || keyword.isBlank() ? null : banner.name.containsIgnoreCase(keyword);
    }

    private BooleanExpression typeEquals(final BannerType type) {
        return type == null ? null : banner.type.eq(type);
    }
}
