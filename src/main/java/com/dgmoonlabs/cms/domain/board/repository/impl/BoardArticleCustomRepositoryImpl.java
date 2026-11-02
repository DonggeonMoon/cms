package com.dgmoonlabs.cms.domain.board.repository.impl;

import com.dgmoonlabs.cms.domain.board.entity.aritcle.BoardArticle;
import com.dgmoonlabs.cms.domain.board.repository.BoardArticleCustomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.dgmoonlabs.cms.domain.board.entity.aritcle.QBoardArticle.boardArticle;

@Repository
@RequiredArgsConstructor
public class BoardArticleCustomRepositoryImpl implements BoardArticleCustomRepository {
    private final JPAQueryFactory queryFactory;

    // active notices are listed separately on top, so the regular page leaves them out
    @Override
    public Page<BoardArticle> search(final Long boardId, final String keyword, final boolean includeHidden,
                                     final LocalDateTime now, final Pageable pageable) {
        BooleanExpression[] conditions = {
                boardArticle.boardId.eq(boardId),
                boardArticle.deletedAt.isNull(),
                hiddenFilter(includeHidden),
                activeNotice(now).not(),
                keywordContains(keyword)
        };
        List<BoardArticle> content = queryFactory.selectFrom(boardArticle)
                .where(conditions)
                .orderBy(boardArticle.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, () -> queryFactory.select(boardArticle.count())
                .from(boardArticle)
                .where(conditions)
                .fetchOne());
    }

    @Override
    public List<BoardArticle> findActiveNotices(final Long boardId, final boolean includeHidden, final LocalDateTime now) {
        return queryFactory.selectFrom(boardArticle)
                .where(boardArticle.boardId.eq(boardId), boardArticle.deletedAt.isNull(), hiddenFilter(includeHidden),
                        activeNotice(now))
                .orderBy(boardArticle.id.desc())
                .fetch();
    }

    private BooleanExpression activeNotice(final LocalDateTime now) {
        return boardArticle.isNotice.isTrue()
                .and(boardArticle.noticeStartDateTime.isNull().or(boardArticle.noticeStartDateTime.loe(now)))
                .and(boardArticle.noticeEndDateTime.isNull().or(boardArticle.noticeEndDateTime.goe(now)));
    }

    private BooleanExpression hiddenFilter(final boolean includeHidden) {
        return includeHidden ? null : boardArticle.isHidden.isFalse();
    }

    private BooleanExpression keywordContains(final String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return boardArticle.title.containsIgnoreCase(keyword).or(boardArticle.contentText.containsIgnoreCase(keyword));
    }
}
