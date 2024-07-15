package com.dgmoonlabs.cms.domain.board.repository.impl;

import com.dgmoonlabs.cms.domain.board.constant.BoardType;
import com.dgmoonlabs.cms.domain.board.dto.BoardRequest;
import com.dgmoonlabs.cms.domain.board.entity.aritcle.Board;
import com.dgmoonlabs.cms.domain.board.repository.BoardCustomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dgmoonlabs.cms.domain.board.entity.aritcle.QBoard.board;

@Repository
@RequiredArgsConstructor
public class BoardCustomRepositoryImpl implements BoardCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Board> find(BoardRequest boardRequest, Pageable pageable) {
        List<Board> content = queryFactory.select(board)
                .from(board)
                .where(
                        nameEquals(boardRequest.getName()),
                        typeEquals(boardRequest.getType()),
                        fieldsEquals(boardRequest.getFields())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(board.count())
                .from(board)
                .where(
                        nameEquals(boardRequest.getName()),
                        typeEquals(boardRequest.getType()),
                        fieldsEquals(boardRequest.getFields())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<Board> find(BoardRequest boardRequest) {
        return queryFactory.select(board)
                .from(board)
                .where(
                        nameEquals(boardRequest.getName()),
                        typeEquals(boardRequest.getType()),
                        fieldsEquals(boardRequest.getFields())
                )
                .fetch();
    }

    private BooleanExpression typeEquals(final BoardType boardType) {
        return checkIfEmpty(boardType) ? null : board.type.eq(boardType);
    }

    private BooleanExpression nameEquals(final String name) {
        return checkIfEmpty(name) ? null : board.name.eq(name);
    }

    private BooleanExpression fieldsEquals(final String fields) {
        return checkIfEmpty(fields) ? null : board.fields.eq(fields);
    }

    private boolean checkIfEmpty(final String input) {
        return input == null || input.isEmpty();
    }

    private boolean checkIfEmpty(final BoardType input) {
        return input == null;
    }
}
