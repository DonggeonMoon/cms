package com.dgmoonlabs.cms.domain.common.user.repository.impl;

import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberCustomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dgmoonlabs.cms.domain.common.user.entity.member.QMember.member;

@Repository
@RequiredArgsConstructor
public class MemberCustomRepositoryImpl implements MemberCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Member> search(final String keyword, final Pageable pageable) {
        List<Member> content = queryFactory.selectFrom(member)
                .where(member.deletedAt.isNull(), keywordContains(keyword))
                .orderBy(member.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, () -> queryFactory.select(member.count())
                .from(member)
                .where(member.deletedAt.isNull(), keywordContains(keyword))
                .fetchOne());
    }

    private BooleanExpression keywordContains(final String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return member.username.containsIgnoreCase(keyword);
    }
}
