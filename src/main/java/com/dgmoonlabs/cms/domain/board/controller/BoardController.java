package com.dgmoonlabs.cms.domain.board.controller;

import com.dgmoonlabs.cms.domain.board.dto.*;
import com.dgmoonlabs.cms.domain.board.service.BoardArticleService;
import com.dgmoonlabs.cms.domain.board.service.BoardCommentService;
import com.dgmoonlabs.cms.domain.board.service.BoardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final BoardArticleService articleService;
    private final BoardCommentService commentService;

    @GetMapping
    public ResponseEntity<List<BoardResponse>> boards() {
        return ResponseEntity.ok(boardService.getBoards());
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponse> board(@PathVariable final Long boardId) {
        return ResponseEntity.ok(boardService.getBoard(boardId));
    }

    @GetMapping("/{boardId}/articles")
    public ResponseEntity<ArticleListResponse> articles(@PathVariable final Long boardId,
                                                        @RequestParam(required = false) final String keyword,
                                                        @RequestParam(defaultValue = "0") final int page) {
        return ResponseEntity.ok(articleService.getArticles(boardId, keyword, page));
    }

    @GetMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<ArticleDetailResponse> article(@PathVariable final Long boardId, @PathVariable final Long articleId) {
        return ResponseEntity.ok(articleService.getArticle(boardId, articleId, null));
    }

    @PostMapping("/{boardId}/articles/{articleId}/access")
    public ResponseEntity<ArticleDetailResponse> accessArticle(@PathVariable final Long boardId,
                                                               @PathVariable final Long articleId,
                                                               @RequestBody final PasswordRequest request) {
        return ResponseEntity.ok(articleService.getArticle(boardId, articleId, request.password()));
    }

    @PostMapping("/{boardId}/articles")
    public ResponseEntity<Void> createArticle(@PathVariable final Long boardId,
                                              @RequestBody @Valid final ArticleSaveRequest request,
                                              final HttpServletRequest httpRequest) {
        Long id = articleService.createArticle(boardId, request, httpRequest.getRemoteAddr());
        return ResponseEntity.created(URI.create("/api/boards/" + boardId + "/articles/" + id)).build();
    }

    @PutMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<Void> updateArticle(@PathVariable final Long boardId, @PathVariable final Long articleId,
                                              @RequestBody @Valid final ArticleSaveRequest request) {
        articleService.updateArticle(boardId, articleId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<Void> deleteArticle(@PathVariable final Long boardId, @PathVariable final Long articleId,
                                              @RequestBody(required = false) final PasswordRequest request) {
        articleService.deleteArticle(boardId, articleId, request == null ? null : request.password());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{boardId}/articles/{articleId}/likes")
    public ResponseEntity<Map<String, Integer>> like(@PathVariable final Long boardId, @PathVariable final Long articleId) {
        return ResponseEntity.ok(Map.of("likes", articleService.like(boardId, articleId)));
    }

    @GetMapping("/{boardId}/articles/{articleId}/comments")
    public ResponseEntity<List<CommentResponse>> comments(@PathVariable final Long boardId, @PathVariable final Long articleId) {
        return ResponseEntity.ok(commentService.getComments(boardId, articleId));
    }

    @PostMapping("/{boardId}/articles/{articleId}/comments")
    public ResponseEntity<Void> createComment(@PathVariable final Long boardId, @PathVariable final Long articleId,
                                              @RequestBody @Valid final CommentSaveRequest request,
                                              final HttpServletRequest httpRequest) {
        Long id = commentService.createComment(boardId, articleId, request, httpRequest.getRemoteAddr());
        return ResponseEntity.created(URI.create("/api/boards/" + boardId + "/articles/" + articleId + "/comments/" + id))
                .build();
    }

    @DeleteMapping("/{boardId}/articles/{articleId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable final Long boardId, @PathVariable final Long articleId,
                                              @PathVariable final Long commentId,
                                              @RequestBody(required = false) final PasswordRequest request) {
        commentService.deleteComment(boardId, articleId, commentId, request == null ? null : request.password());
        return ResponseEntity.noContent().build();
    }
}
