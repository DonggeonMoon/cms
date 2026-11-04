package com.dgmoonlabs.cms.domain.content.service;

import com.dgmoonlabs.cms.domain.content.constant.ContentStatus;
import com.dgmoonlabs.cms.domain.content.dto.ContentResponse;
import com.dgmoonlabs.cms.domain.content.dto.ContentSaveRequest;
import com.dgmoonlabs.cms.domain.content.dto.ContentSummaryResponse;
import com.dgmoonlabs.cms.domain.content.entity.Content;
import com.dgmoonlabs.cms.domain.content.repository.ContentRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.html.HtmlSanitizer;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import com.dgmoonlabs.cms.global.security.LoginUser;
import com.dgmoonlabs.cms.domain.common.grade.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentService {
    private final GradeService gradeService;
    private final ContentRepository contentRepository;
    private final HtmlSanitizer htmlSanitizer;

    @Transactional(readOnly = true)
    public Page<ContentResponse> searchContents(final String keyword, final Pageable pageable) {
        return contentRepository.findAllByDeletedAtIsNullAndTitleContainingIgnoreCaseOrderByIdAsc(
                keyword == null ? "" : keyword, pageable).map(ContentResponse::from);
    }

    @Transactional(readOnly = true)
    public ContentResponse getContent(final Long id) {
        return ContentResponse.from(findContent(id));
    }

    @Transactional(readOnly = true)
    public List<ContentSummaryResponse> listPublishedContents() {
        return contentRepository.findAllByStatusAndDeletedAtIsNullOrderByIdAsc(ContentStatus.PUBLISHED).stream()
                .map(ContentSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContentResponse getPublishedContent(final Long id) {
        Content content = contentRepository.findByIdAndStatusAndDeletedAtIsNull(id, ContentStatus.PUBLISHED)
                .orElseThrow(BusinessException::notFound);
        gradeService.require(content.getAccessGradeId());
        return ContentResponse.from(content);
    }

    @Transactional
    public Long createContent(final ContentSaveRequest request) {
        String html = htmlSanitizer.sanitize(request.contentHtml());
        Content content = Content.builder()
                .title(request.title())
                .contentHtml(html)
                .contentText(htmlSanitizer.toPlainText(html))
                .status(request.status())
                .memo(request.memoOrEmpty())
                .version(1)
                .lastWorker(currentWorker())
                .accessGradeId(gradeService.validGradeId(request.accessGradeId()))
                .build();
        return contentRepository.save(content).getId();
    }

    @Transactional
    public void updateContent(final Long id, final ContentSaveRequest request) {
        String html = htmlSanitizer.sanitize(request.contentHtml());
        findContent(id).update(request.title(), html, htmlSanitizer.toPlainText(html), request.status(),
                request.memoOrEmpty(), currentWorker());
        findContent(id).changeAccessGrade(gradeService.validGradeId(request.accessGradeId()));
    }

    @Transactional
    public void deleteContent(final Long id) {
        findContent(id).delete();
    }

    private String currentWorker() {
        return CurrentUser.get().map(LoginUser::username).orElse(null);
    }

    private Content findContent(final Long id) {
        return contentRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }
}
