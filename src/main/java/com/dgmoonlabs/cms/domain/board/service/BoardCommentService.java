package com.dgmoonlabs.cms.domain.board.service;

import com.dgmoonlabs.cms.domain.board.constant.CommentStatus;
import com.dgmoonlabs.cms.domain.board.dto.BoardResponse.Permissions;
import com.dgmoonlabs.cms.domain.board.dto.CommentResponse;
import com.dgmoonlabs.cms.domain.board.dto.CommentSaveRequest;
import com.dgmoonlabs.cms.domain.board.entity.aritcle.BoardArticle;
import com.dgmoonlabs.cms.domain.board.entity.aritcle.BoardConfig;
import com.dgmoonlabs.cms.domain.board.entity.comment.BoardComment;
import com.dgmoonlabs.cms.domain.board.repository.BoardCommentRepository;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import com.dgmoonlabs.cms.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BoardCommentService {
    private static final int MAX_DEPTH = 1;

    private final BoardService boardService;
    private final BoardArticleService articleService;
    private final BoardCommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    // threads ordered root-first: each root comment followed by its replies
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(final Long boardId, final Long articleId) {
        BoardConfig config = requireCommentable(boardId);
        if (!boardService.permissionsOf(config).canRead()) {
            throw new BusinessException(ErrorCode.BOARD_PERMISSION_DENIED);
        }
        articleService.findArticle(boardId, articleId);

        List<BoardComment> comments = commentRepository.findAllByArticleIdOrderByIdAsc(articleId);
        Map<Long, List<BoardComment>> replies = new HashMap<>();
        List<BoardComment> roots = new ArrayList<>();
        for (BoardComment comment : comments) {
            if (comment.getParentId() == null) {
                roots.add(comment);
            } else {
                replies.computeIfAbsent(comment.getParentId(), key -> new ArrayList<>()).add(comment);
            }
        }

        List<CommentResponse> result = new ArrayList<>();
        for (BoardComment root : roots) {
            List<BoardComment> children = replies.getOrDefault(root.getId(), List.of());
            boolean hideRoot = root.getDeletedAt() != null && children.stream().allMatch(c -> c.getDeletedAt() != null);
            if (hideRoot) {
                continue;
            }
            result.add(CommentResponse.of(root, canDelete(root)));
            children.stream()
                    .filter(child -> child.getDeletedAt() == null)
                    .forEach(child -> result.add(CommentResponse.of(child, canDelete(child))));
        }
        return result;
    }

    @Transactional
    public Long createComment(final Long boardId, final Long articleId, final CommentSaveRequest request,
                              final String ipAddress) {
        BoardConfig config = requireCommentable(boardId);
        Permissions permissions = boardService.permissionsOf(config);
        if (!permissions.canWrite()) {
            throw new BusinessException(ErrorCode.BOARD_PERMISSION_DENIED);
        }
        BoardArticle article = articleService.findArticle(boardId, articleId);

        int depth = 0;
        if (request.parentId() != null) {
            BoardComment parent = commentRepository.findByIdAndArticleIdAndDeletedAtIsNull(request.parentId(), article.getId())
                    .orElseThrow(BusinessException::notFound);
            if (parent.getDepth() >= MAX_DEPTH) {
                throw new BusinessException(ErrorCode.FEATURE_NOT_SUPPORTED);
            }
            depth = parent.getDepth() + 1;
        }

        Optional<LoginUser> user = CurrentUser.get();
        if (user.isEmpty() && (isBlank(request.authorName()) || isBlank(request.password()))) {
            throw new BusinessException(ErrorCode.GUEST_PASSWORD_REQUIRED);
        }
        BoardComment comment = BoardComment.builder()
                .articleId(article.getId())
                .parentId(request.parentId())
                .depth(depth)
                .content(request.content())
                .userType(user.map(LoginUser::userType).orElse(UserType.GUEST))
                .userId(user.map(LoginUser::id).orElse(null))
                .authorName(user.map(LoginUser::name).orElse(request.authorName()))
                .password(user.isPresent() ? null : passwordEncoder.encode(request.password()))
                .status(CommentStatus.APPROVED)
                .ipAddress(ipAddress)
                .build();
        return commentRepository.save(comment).getId();
    }

    @Transactional
    public void deleteComment(final Long boardId, final Long articleId, final Long commentId, final String password) {
        articleService.findArticle(boardId, articleId);
        BoardComment comment = commentRepository.findByIdAndArticleIdAndDeletedAtIsNull(commentId, articleId)
                .orElseThrow(BusinessException::notFound);
        boolean guestMatches = comment.isWrittenByGuest() && password != null && comment.getPassword() != null
                && passwordEncoder.matches(password, comment.getPassword());
        if (!isOwnerOrAdmin(comment) && !guestMatches) {
            throw new BusinessException(comment.isWrittenByGuest() ? ErrorCode.ARTICLE_PASSWORD_MISMATCH : ErrorCode.ACCESS_DENIED);
        }
        comment.delete();
    }

    @Transactional
    public void moderateComment(final Long boardId, final Long articleId, final Long commentId) {
        articleService.findArticle(boardId, articleId);
        commentRepository.findByIdAndArticleIdAndDeletedAtIsNull(commentId, articleId)
                .orElseThrow(BusinessException::notFound)
                .moderate();
    }

    // UI hint only: guest comments show a delete button that then asks for the password
    private boolean canDelete(final BoardComment comment) {
        return isOwnerOrAdmin(comment) || comment.isWrittenByGuest();
    }

    private boolean isOwnerOrAdmin(final BoardComment comment) {
        return CurrentUser.isAdmin()
                || comment.getUserId() != null && CurrentUser.is(comment.getUserType(), comment.getUserId());
    }

    private BoardConfig requireCommentable(final Long boardId) {
        boardService.findBoard(boardId);
        BoardConfig config = boardService.findConfig(boardId);
        if (!config.isSupportsComment()) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_SUPPORTED);
        }
        return config;
    }

    private static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }
}
