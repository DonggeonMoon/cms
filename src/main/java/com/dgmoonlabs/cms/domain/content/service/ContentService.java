package com.dgmoonlabs.cms.domain.content.service;

import com.dgmoonlabs.cms.domain.admin.account.service.AdminAccountService;
import com.dgmoonlabs.cms.domain.content.dto.ContentWorkflowDtos.RejectRequest;
import com.dgmoonlabs.cms.domain.content.dto.ContentWorkflowDtos.ReviewRequest;
import com.dgmoonlabs.cms.domain.content.constant.ContentStatus;
import com.dgmoonlabs.cms.domain.content.dto.ContentResponse;
import com.dgmoonlabs.cms.domain.content.dto.ContentSaveRequest;
import com.dgmoonlabs.cms.domain.content.dto.ContentSummaryResponse;
import com.dgmoonlabs.cms.domain.content.entity.Content;
import com.dgmoonlabs.cms.domain.content.repository.ContentRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.html.HtmlSanitizer;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import com.dgmoonlabs.cms.global.security.LoginUser;
import com.dgmoonlabs.cms.global.site.SiteContextHolder;
import com.dgmoonlabs.cms.domain.common.grade.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentService {
    private final GradeService gradeService;
    private final ContentRepository contentRepository;
    private final HtmlSanitizer htmlSanitizer;
    private final AdminAccountService adminAccountService;

    @Transactional(readOnly = true)
    public Page<ContentResponse> searchContents(final String keyword, final Pageable pageable) {
        return contentRepository.findAllBySiteIdAndDeletedAtIsNullAndTitleContainingIgnoreCaseOrderByIdAsc(
                SiteContextHolder.currentSiteId(), keyword == null ? "" : keyword, pageable).map(ContentResponse::from);
    }

    @Transactional(readOnly = true)
    public ContentResponse getContent(final Long id) {
        return ContentResponse.from(findContent(id));
    }

    @Transactional(readOnly = true)
    public List<ContentSummaryResponse> listPublishedContents() {
        return contentRepository.findAllByStatusAndSiteIdAndDeletedAtIsNullOrderByIdAsc(ContentStatus.PUBLISHED, SiteContextHolder.currentSiteId()).stream()
                .map(ContentSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContentResponse getPublishedContent(final Long id) {
        Content content = contentRepository.findByIdAndStatusAndSiteIdAndDeletedAtIsNull(id, ContentStatus.PUBLISHED, SiteContextHolder.currentSiteId())
                .orElseThrow(BusinessException::notFound);
        gradeService.require(content.getAccessGradeId());
        return ContentResponse.from(content);
    }

    @Transactional
    public Long createContent(final ContentSaveRequest request) {
        requireDirectPublishAllowed(request.status(), null);
        String html = htmlSanitizer.sanitize(request.contentHtml());
        Content content = Content.builder()
                .siteId(SiteContextHolder.currentSiteId())
                .title(request.title())
                .contentHtml(html)
                .contentText(htmlSanitizer.toPlainText(html))
                .status(request.status())
                .memo(request.memoOrEmpty())
                .version(1)
                .lastWorker(currentWorker())
                .accessGradeId(gradeService.validGradeId(request.accessGradeId()))
                .publishedAt(request.status() == ContentStatus.PUBLISHED ? LocalDateTime.now() : null)
                .reviewedBy(request.status() == ContentStatus.PUBLISHED ? currentWorker() : null)
                .build();
        return contentRepository.save(content).getId();
    }

    @Transactional
    public void updateContent(final Long id, final ContentSaveRequest request) {
        Content content = findContent(id);
        requireDirectPublishAllowed(request.status(), content.getStatus());
        String html = htmlSanitizer.sanitize(request.contentHtml());
        content.update(request.title(), html, htmlSanitizer.toPlainText(html), request.status(), request.memoOrEmpty(), currentWorker());
        content.changeAccessGrade(gradeService.validGradeId(request.accessGradeId()));
    }

    @Transactional(readOnly = true)
    public Page<ContentResponse> reviewQueue(final Pageable pageable) {
        return contentRepository.findAllByStatusAndSiteIdAndDeletedAtIsNullOrderByReviewRequestedAtAsc(
                ContentStatus.IN_REVIEW, SiteContextHolder.currentSiteId(), pageable).map(ContentResponse::from);
    }

    @Transactional
    public ContentResponse requestReview(final Long id, final ReviewRequest request) {
        LocalDateTime now = LocalDateTime.now();
        if (request.publishAt() != null && !request.publishAt().isAfter(now)) {
            throw new BusinessException(ErrorCode.INVALID_PUBLISH_AT);
        }
        Content content = findContent(id);
        content.requestReview(currentWorker(), request.publishAt(), now);
        return ContentResponse.from(content);
    }

    @Transactional
    public ContentResponse approve(final Long id) {
        Content content = findContent(id);
        requireOtherReviewer(content);
        content.approve(currentWorker(), LocalDateTime.now());
        return ContentResponse.from(content);
    }

    @Transactional
    public ContentResponse reject(final Long id, final RejectRequest request) {
        Content content = findContent(id);
        requireOtherReviewer(content);
        content.reject(currentWorker(), request.comment().trim());
        return ContentResponse.from(content);
    }

    @Transactional
    public ContentResponse unpublish(final Long id) {
        Content content = findContent(id);
        content.unpublish();
        return ContentResponse.from(content);
    }

    @Scheduled(cron = "${cms.content.publish-cron:0 * * * * *}")
    @Transactional
    public void publishScheduled() {
        LocalDateTime now = LocalDateTime.now();
        contentRepository.findAllByStatusAndPublishAtLessThanEqualAndDeletedAtIsNull(ContentStatus.SCHEDULED, now)
                .forEach(content -> content.publishIfDue(now));
    }

    /** 검토 단계를 건너뛰고 바로 발행하는 것은 최고 관리자만. 이미 발행된 글을 고치는 것은 그대로 둔다. */
    private void requireDirectPublishAllowed(final ContentStatus requested, final ContentStatus current) {
        if (requested != ContentStatus.DRAFT && requested != ContentStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (requested == ContentStatus.PUBLISHED && current != ContentStatus.PUBLISHED && !isSuperAdmin()) {
            throw new BusinessException(ErrorCode.PUBLISH_REVIEW_REQUIRED);
        }
    }

    private void requireOtherReviewer(final Content content) {
        String worker = currentWorker();
        if (worker != null && worker.equals(content.getReviewRequestedBy()) && !isSuperAdmin()) {
            throw new BusinessException(ErrorCode.SELF_REVIEW_NOT_ALLOWED);
        }
    }

    private boolean isSuperAdmin() {
        return CurrentUser.get().map(user -> adminAccountService.permissions(user.id()).superAdmin()).orElse(false);
    }

    @Transactional
    public void deleteContent(final Long id) {
        findContent(id).delete();
    }

    private String currentWorker() {
        return CurrentUser.get().map(LoginUser::username).orElse(null);
    }

    private Content findContent(final Long id) {
        return contentRepository.findByIdAndSiteIdAndDeletedAtIsNull(id, SiteContextHolder.currentSiteId()).orElseThrow(BusinessException::notFound);
    }
}
