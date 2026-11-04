package com.dgmoonlabs.cms.domain.board.service;

import com.dgmoonlabs.cms.domain.board.constant.BoardType;
import com.dgmoonlabs.cms.domain.board.dto.*;
import com.dgmoonlabs.cms.domain.board.dto.BoardResponse.Permissions;
import com.dgmoonlabs.cms.domain.board.entity.aritcle.Board;
import com.dgmoonlabs.cms.domain.board.entity.aritcle.BoardArticle;
import com.dgmoonlabs.cms.domain.board.entity.aritcle.BoardArticleFile;
import com.dgmoonlabs.cms.domain.board.entity.aritcle.BoardArticleLike;
import com.dgmoonlabs.cms.domain.board.entity.aritcle.BoardConfig;
import com.dgmoonlabs.cms.domain.board.repository.BoardArticleFileRepository;
import com.dgmoonlabs.cms.domain.board.repository.BoardArticleLikeRepository;
import com.dgmoonlabs.cms.domain.board.repository.BoardArticleRepository;
import com.dgmoonlabs.cms.domain.board.repository.BoardCommentRepository;
import com.dgmoonlabs.cms.domain.common.file.dto.FileResponse;
import com.dgmoonlabs.cms.domain.common.file.service.FileService;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.html.HtmlSanitizer;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import com.dgmoonlabs.cms.global.security.LoginUser;
import com.dgmoonlabs.cms.domain.common.grade.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardArticleService {
    private final BoardService boardService;
    private final BoardArticleRepository articleRepository;
    private final BoardArticleFileRepository articleFileRepository;
    private final BoardArticleLikeRepository articleLikeRepository;
    private final BoardCommentRepository commentRepository;
    private final FileService fileService;
    private final PasswordEncoder passwordEncoder;
    private final HtmlSanitizer htmlSanitizer;
    private final GradeService gradeService;

    @Transactional(readOnly = true)
    public ArticleListResponse getArticles(final Long boardId, final String keyword, final int page) {
        Board board = boardService.findBoard(boardId);
        BoardConfig config = boardService.findConfig(boardId);
        Permissions permissions = boardService.permissionsOf(config);
        if (!permissions.canList()) {
            throw new BusinessException(ErrorCode.BOARD_PERMISSION_DENIED);
        }

        LocalDateTime now = LocalDateTime.now();
        boolean includeHidden = permissions.isAdmin();
        List<BoardArticle> notices = config.isSupportsNotice()
                ? articleRepository.findActiveNotices(boardId, includeHidden, now)
                : List.of();
        Page<BoardArticle> articles = articleRepository.search(boardId, keyword, includeHidden, now,
                PageRequest.of(Math.max(page, 0), config.getPageSize()));

        List<BoardArticle> all = new ArrayList<>(notices);
        all.addAll(articles.getContent());
        Map<Long, Long> commentCounts = commentCounts(all);
        Map<Long, String> thumbnails = board.getType() == BoardType.GALLERY ? thumbnails(all) : Map.of();
        Function<BoardArticle, ArticleSummaryResponse> toSummary = article -> ArticleSummaryResponse.of(
                article, commentCounts.getOrDefault(article.getId(), 0L), thumbnails.get(article.getId()));

        return new ArticleListResponse(
                notices.stream().map(toSummary).toList(),
                new PagedModel<>(articles.map(toSummary))
        );
    }

    @Transactional
    public ArticleDetailResponse getArticle(final Long boardId, final Long articleId, final String password) {
        BoardConfig config = boardService.findConfig(boardId);
        Permissions permissions = boardService.permissionsOf(config);
        if (!permissions.canRead()) {
            throw new BusinessException(ErrorCode.BOARD_PERMISSION_DENIED);
        }
        gradeService.require(config.getReadGradeId());
        BoardArticle article = findArticle(boardId, articleId);
        if (article.isHidden() && !permissions.isAdmin()) {
            throw BusinessException.notFound();
        }
        if (article.isSecret() && !isOwnerOrAdmin(article) && !guestPasswordMatches(article, password)) {
            throw new BusinessException(ErrorCode.ARTICLE_PASSWORD_MISMATCH);
        }
        article.increaseHit();
        boolean canModify = isOwnerOrAdmin(article) || guestPasswordMatches(article, password);
        return ArticleDetailResponse.of(article, filesOf(articleId), canModify || article.isWrittenByGuest());
    }

    @Transactional
    public Long createArticle(final Long boardId, final ArticleSaveRequest request, final String ipAddress) {
        BoardConfig config = boardService.findConfig(boardId);
        gradeService.require(config.getWriteGradeId());
        Permissions permissions = boardService.permissionsOf(config);
        if (!permissions.canWrite()) {
            throw new BusinessException(ErrorCode.BOARD_PERMISSION_DENIED);
        }
        validateFeatures(config, request, permissions.isAdmin());

        Optional<LoginUser> user = CurrentUser.get();
        String passwordHash = null;
        String authorName;
        if (user.isPresent()) {
            authorName = user.get().name();
        } else {
            if (isBlank(request.authorName()) || isBlank(request.password())) {
                throw new BusinessException(ErrorCode.GUEST_PASSWORD_REQUIRED);
            }
            authorName = request.authorName();
            passwordHash = passwordEncoder.encode(request.password());
        }

        String html = htmlSanitizer.sanitize(request.contentHtml());
        BoardArticle article = BoardArticle.builder()
                .boardId(boardId)
                .title(request.title())
                .contentHtml(html)
                .contentText(htmlSanitizer.toPlainText(html))
                .isSecret(request.isSecret())
                .userType(user.map(LoginUser::userType).orElse(UserType.GUEST))
                .userId(user.map(LoginUser::id).orElse(null))
                .authorName(authorName)
                .password(passwordHash)
                .ipAddress(ipAddress)
                .build();
        if (permissions.isAdmin()) {
            article.updateAdminFlags(request.isNotice(), request.noticeStartDateTime(), request.noticeEndDateTime(),
                    request.isHidden());
        }
        Long articleId = articleRepository.save(article).getId();
        saveFiles(articleId, request.fileIdsOrEmpty());
        return articleId;
    }

    @Transactional
    public void updateArticle(final Long boardId, final Long articleId, final ArticleSaveRequest request) {
        BoardConfig config = boardService.findConfig(boardId);
        boolean isAdmin = CurrentUser.isAdmin();
        validateFeatures(config, request, isAdmin);
        BoardArticle article = findArticle(boardId, articleId);
        requireModifiable(article, request.password());

        String html = htmlSanitizer.sanitize(request.contentHtml());
        article.update(request.title(), html, htmlSanitizer.toPlainText(html), request.isSecret());
        if (isAdmin) {
            article.updateAdminFlags(request.isNotice(), request.noticeStartDateTime(), request.noticeEndDateTime(),
                    request.isHidden());
        }
        articleFileRepository.deleteAllByArticleId(articleId);
        saveFiles(articleId, request.fileIdsOrEmpty());
    }

    @Transactional
    public void deleteArticle(final Long boardId, final Long articleId, final String password) {
        BoardArticle article = findArticle(boardId, articleId);
        requireModifiable(article, password);
        article.delete();
    }

    @Transactional
    public int like(final Long boardId, final Long articleId) {
        BoardConfig config = boardService.findConfig(boardId);
        if (!config.isSupportsLike()) {
            throw new BusinessException(ErrorCode.FEATURE_NOT_SUPPORTED);
        }
        LoginUser user = CurrentUser.get()
                .filter(loginUser -> loginUser.userType() == UserType.MEMBER)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
        BoardArticle article = findArticle(boardId, articleId);
        if (!articleLikeRepository.existsByArticleIdAndMemberId(articleId, user.id())) {
            articleLikeRepository.save(BoardArticleLike.builder().articleId(articleId).memberId(user.id()).build());
            article.increaseLikes();
        }
        return article.getLikes();
    }

    BoardArticle findArticle(final Long boardId, final Long articleId) {
        return articleRepository.findByIdAndBoardIdAndDeletedAtIsNull(articleId, boardId)
                .orElseThrow(BusinessException::notFound);
    }

    private void requireModifiable(final BoardArticle article, final String password) {
        if (isOwnerOrAdmin(article)) {
            return;
        }
        if (article.isWrittenByGuest()) {
            if (!guestPasswordMatches(article, password)) {
                throw new BusinessException(ErrorCode.ARTICLE_PASSWORD_MISMATCH);
            }
            return;
        }
        throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }

    private boolean isOwnerOrAdmin(final BoardArticle article) {
        return CurrentUser.isAdmin() || (article.getUserId() != null && CurrentUser.is(article.getUserType(), article.getUserId()));
    }

    private boolean guestPasswordMatches(final BoardArticle article, final String password) {
        return article.isWrittenByGuest() && password != null && article.getPassword() != null
                && passwordEncoder.matches(password, article.getPassword());
    }

    private void validateFeatures(final BoardConfig config, final ArticleSaveRequest request, final boolean isAdmin) {
        if (request.isSecret() && !config.isSupportsSecret()
                || request.isNotice() && (!config.isSupportsNotice() || !isAdmin)
                || !request.fileIdsOrEmpty().isEmpty() && !config.isSupportsAttachment()
                || request.fileIdsOrEmpty().size() > config.getMaximumFileCount()) {
            throw new BusinessException(ErrorCode.FEATURE_NOT_SUPPORTED);
        }
        if (request.noticeStartDateTime() != null && request.noticeEndDateTime() != null
                && !request.noticeEndDateTime().isAfter(request.noticeStartDateTime())) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD);
        }
    }

    private void saveFiles(final Long articleId, final List<Long> fileIds) {
        fileService.requireFilesExist(fileIds);
        for (int i = 0; i < fileIds.size(); i++) {
            articleFileRepository.save(BoardArticleFile.builder()
                    .articleId(articleId).fileId(fileIds.get(i)).sequence(i).build());
        }
    }

    private List<FileResponse> filesOf(final Long articleId) {
        return fileService.getFiles(articleFileRepository.findAllByArticleIdOrderBySequenceAsc(articleId).stream()
                .map(BoardArticleFile::getFileId)
                .toList());
    }

    private Map<Long, Long> commentCounts(final List<BoardArticle> articles) {
        if (articles.isEmpty()) {
            return Map.of();
        }
        return commentRepository.countByArticleIds(articles.stream().map(BoardArticle::getId).toList()).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
    }

    private Map<Long, String> thumbnails(final List<BoardArticle> articles) {
        if (articles.isEmpty()) {
            return Map.of();
        }
        List<BoardArticleFile> links = articleFileRepository.findAllByArticleIdInOrderBySequenceAsc(
                articles.stream().map(BoardArticle::getId).toList());
        Map<Long, FileResponse> files = fileService.getFiles(links.stream().map(BoardArticleFile::getFileId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(FileResponse::id, Function.identity()));

        Map<Long, String> thumbnails = new HashMap<>();
        for (BoardArticleFile link : links) {
            FileResponse file = files.get(link.getFileId());
            if (file != null && file.thumbnailUrl() != null) {
                thumbnails.putIfAbsent(link.getArticleId(), file.thumbnailUrl());
            }
        }
        return thumbnails;
    }

    private static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }
}
