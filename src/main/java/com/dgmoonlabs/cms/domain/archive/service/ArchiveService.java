package com.dgmoonlabs.cms.domain.archive.service;

import com.dgmoonlabs.cms.domain.archive.constant.ArchiveStatus;
import com.dgmoonlabs.cms.domain.archive.dto.ArchiveDtos.*;
import com.dgmoonlabs.cms.domain.archive.entity.article.Archive;
import com.dgmoonlabs.cms.domain.archive.entity.article.ArchiveArticle;
import com.dgmoonlabs.cms.domain.archive.entity.article.ArchiveArticleFile;
import com.dgmoonlabs.cms.domain.archive.entity.comment.ArchiveComment;
import com.dgmoonlabs.cms.domain.archive.repository.ArchiveArticleFileRepository;
import com.dgmoonlabs.cms.domain.archive.repository.ArchiveArticleRepository;
import com.dgmoonlabs.cms.domain.archive.repository.ArchiveCommentRepository;
import com.dgmoonlabs.cms.domain.archive.repository.ArchiveRepository;
import com.dgmoonlabs.cms.domain.board.constant.BoardType;
import com.dgmoonlabs.cms.domain.board.constant.CommentStatus;
import com.dgmoonlabs.cms.domain.common.file.dto.FileResponse;
import com.dgmoonlabs.cms.domain.common.file.service.FileService;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.html.HtmlSanitizer;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import com.dgmoonlabs.cms.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArchiveService {
    private final ArchiveRepository archiveRepository;
    private final ArchiveArticleRepository articleRepository;
    private final ArchiveArticleFileRepository articleFileRepository;
    private final ArchiveCommentRepository commentRepository;
    private final FileService fileService;
    private final HtmlSanitizer htmlSanitizer;

    @Transactional(readOnly = true)
    public List<ArchiveResponse> getArchives() {
        return archiveRepository.findAllByDeletedAtIsNullOrderByIdAsc().stream().map(ArchiveResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ArchiveResponse getArchive(final Long id) {
        return ArchiveResponse.from(findArchive(id));
    }

    @Transactional
    public Long createArchive(final ArchiveSaveRequest request) {
        if (archiveRepository.existsByName(request.name())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return archiveRepository.save(Archive.builder()
                .name(request.name())
                .description(request.description())
                .type(request.type())
                .supportsComment(request.supportsComment())
                .pageSize(request.pageSize())
                .build()).getId();
    }

    @Transactional
    public void updateArchive(final Long id, final ArchiveSaveRequest request) {
        if (archiveRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        findArchive(id).update(request.name(), request.description(), request.type(), request.supportsComment(),
                request.pageSize());
    }

    @Transactional
    public void deleteArchive(final Long id) {
        findArchive(id).delete();
    }

    // visitors only see published entries; admins also see drafts
    @Transactional(readOnly = true)
    public Page<ArchiveArticleSummary> getArticles(final Long archiveId, final String keyword, final int page) {
        Archive archive = findArchive(archiveId);
        ArchiveStatus status = CurrentUser.isAdmin() ? null : ArchiveStatus.PUBLISHED;
        Page<ArchiveArticle> articles = articleRepository.search(archiveId, status, keyword == null ? "" : keyword.trim(),
                PageRequest.of(Math.max(page, 0), archive.getPageSize()));
        Map<Long, String> thumbnails = archive.getType() == BoardType.GALLERY ? thumbnails(articles.getContent()) : Map.of();
        return articles.map(article -> ArchiveArticleSummary.of(article, thumbnails.get(article.getId())));
    }

    @Transactional
    public ArchiveArticleDetail getArticle(final Long archiveId, final Long articleId) {
        ArchiveArticle article = findArticle(archiveId, articleId);
        if (article.getStatus() != ArchiveStatus.PUBLISHED && !CurrentUser.isAdmin()) {
            throw BusinessException.notFound();
        }
        article.increaseHit();
        return ArchiveArticleDetail.of(article, filesOf(articleId));
    }

    @Transactional
    public Long createArticle(final Long archiveId, final ArchiveArticleSaveRequest request, final String ipAddress) {
        findArchive(archiveId);
        LoginUser admin = CurrentUser.get().orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
        String html = htmlSanitizer.sanitize(request.contentHtml());
        ArchiveArticle article = articleRepository.save(ArchiveArticle.builder()
                .archiveId(archiveId)
                .title(request.title())
                .contentHtml(html)
                .contentText(htmlSanitizer.toPlainText(html))
                .status(request.status())
                .userType(admin.userType())
                .userId(admin.id())
                .authorName(admin.name())
                .ipAddress(ipAddress)
                .build());
        saveFiles(article.getId(), request.fileIdsOrEmpty());
        return article.getId();
    }

    @Transactional
    public void updateArticle(final Long archiveId, final Long articleId, final ArchiveArticleSaveRequest request) {
        ArchiveArticle article = findArticle(archiveId, articleId);
        String html = htmlSanitizer.sanitize(request.contentHtml());
        article.update(request.title(), html, htmlSanitizer.toPlainText(html), request.status());
        articleFileRepository.deleteAllByArticleId(articleId);
        saveFiles(articleId, request.fileIdsOrEmpty());
    }

    @Transactional
    public void deleteArticle(final Long archiveId, final Long articleId) {
        findArticle(archiveId, articleId).delete();
    }

    @Transactional(readOnly = true)
    public List<ArchiveCommentResponse> getComments(final Long archiveId, final Long articleId) {
        requireCommentable(archiveId);
        requirePublished(findArticle(archiveId, articleId));
        return commentRepository.findAllByArticleIdAndDeletedAtIsNullOrderByIdAsc(articleId).stream()
                .map(comment -> ArchiveCommentResponse.of(comment, canDelete(comment)))
                .toList();
    }

    @Transactional
    public Long createComment(final Long archiveId, final Long articleId, final ArchiveCommentSaveRequest request,
                              final String ipAddress) {
        requireCommentable(archiveId);
        requirePublished(findArticle(archiveId, articleId));
        LoginUser user = CurrentUser.get().orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
        return commentRepository.save(ArchiveComment.builder()
                .articleId(articleId)
                .content(request.content())
                .userType(user.userType())
                .userId(user.id())
                .authorName(user.name())
                .status(CommentStatus.APPROVED)
                .ipAddress(ipAddress)
                .build()).getId();
    }

    @Transactional
    public void deleteComment(final Long archiveId, final Long articleId, final Long commentId) {
        findArticle(archiveId, articleId);
        ArchiveComment comment = commentRepository.findByIdAndArticleIdAndDeletedAtIsNull(commentId, articleId)
                .orElseThrow(BusinessException::notFound);
        if (!canDelete(comment)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        comment.delete();
    }

    private boolean canDelete(final ArchiveComment comment) {
        return CurrentUser.isAdmin() || comment.getUserId() != null
                && CurrentUser.is(comment.getUserType() == null ? UserType.MEMBER : comment.getUserType(), comment.getUserId());
    }

    private void requireCommentable(final Long archiveId) {
        if (!findArchive(archiveId).isSupportsComment()) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_SUPPORTED);
        }
    }

    private void requirePublished(final ArchiveArticle article) {
        if (article.getStatus() != ArchiveStatus.PUBLISHED && !CurrentUser.isAdmin()) {
            throw BusinessException.notFound();
        }
    }

    private void saveFiles(final Long articleId, final List<Long> fileIds) {
        fileService.requireFilesExist(fileIds);
        for (int i = 0; i < fileIds.size(); i++) {
            articleFileRepository.save(ArchiveArticleFile.builder()
                    .articleId(articleId).fileId(fileIds.get(i)).sequence(i).build());
        }
    }

    private List<FileResponse> filesOf(final Long articleId) {
        return fileService.getFiles(articleFileRepository.findAllByArticleIdOrderBySequenceAsc(articleId).stream()
                .map(ArchiveArticleFile::getFileId)
                .toList());
    }

    private Map<Long, String> thumbnails(final List<ArchiveArticle> articles) {
        if (articles.isEmpty()) {
            return Map.of();
        }
        List<ArchiveArticleFile> links = articleFileRepository.findAllByArticleIdInOrderBySequenceAsc(
                articles.stream().map(ArchiveArticle::getId).toList());
        Map<Long, FileResponse> files = fileService.getFiles(links.stream().map(ArchiveArticleFile::getFileId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(FileResponse::id, Function.identity()));
        Map<Long, String> thumbnails = new HashMap<>();
        for (ArchiveArticleFile link : links) {
            FileResponse file = files.get(link.getFileId());
            if (file != null && file.thumbnailUrl() != null) {
                thumbnails.putIfAbsent(link.getArticleId(), file.thumbnailUrl());
            }
        }
        return thumbnails;
    }

    private Archive findArchive(final Long id) {
        return archiveRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }

    private ArchiveArticle findArticle(final Long archiveId, final Long articleId) {
        return articleRepository.findByIdAndArchiveIdAndDeletedAtIsNull(articleId, archiveId)
                .orElseThrow(BusinessException::notFound);
    }
}
