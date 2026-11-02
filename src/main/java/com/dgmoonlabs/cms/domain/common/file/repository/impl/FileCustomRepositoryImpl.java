package com.dgmoonlabs.cms.domain.common.file.repository.impl;

import com.dgmoonlabs.cms.domain.common.file.constant.MediaType;
import com.dgmoonlabs.cms.domain.common.file.entity.File;
import com.dgmoonlabs.cms.domain.common.file.repository.FileCustomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dgmoonlabs.cms.domain.common.file.entity.QFile.file;

@Repository
@RequiredArgsConstructor
public class FileCustomRepositoryImpl implements FileCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<File> search(final String keyword, final MediaType mediaType, final Pageable pageable) {
        List<File> content = queryFactory.selectFrom(file)
                .where(file.deletedAt.isNull(), nameContains(keyword), mediaTypeEquals(mediaType))
                .orderBy(file.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, () -> queryFactory.select(file.count())
                .from(file)
                .where(file.deletedAt.isNull(), nameContains(keyword), mediaTypeEquals(mediaType))
                .fetchOne());
    }

    private BooleanExpression nameContains(final String keyword) {
        return keyword == null || keyword.isBlank() ? null : file.originalName.containsIgnoreCase(keyword);
    }

    private BooleanExpression mediaTypeEquals(final MediaType mediaType) {
        return mediaType == null ? null : file.mediaType.eq(mediaType);
    }
}
