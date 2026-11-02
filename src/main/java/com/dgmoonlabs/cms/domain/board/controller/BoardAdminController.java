package com.dgmoonlabs.cms.domain.board.controller;

import com.dgmoonlabs.cms.domain.board.dto.BoardSaveRequest;
import com.dgmoonlabs.cms.domain.board.service.BoardCommentService;
import com.dgmoonlabs.cms.domain.board.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/admin/boards")
@RequiredArgsConstructor
public class BoardAdminController {
    private final BoardService boardService;
    private final BoardCommentService commentService;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid final BoardSaveRequest request) {
        return ResponseEntity.created(URI.create("/api/boards/" + boardService.createBoard(request))).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable final Long id, @RequestBody @Valid final BoardSaveRequest request) {
        boardService.updateBoard(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        boardService.deleteBoard(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{boardId}/articles/{articleId}/comments/{commentId}/moderate")
    public ResponseEntity<Void> moderate(@PathVariable final Long boardId, @PathVariable final Long articleId,
                                         @PathVariable final Long commentId) {
        commentService.moderateComment(boardId, articleId, commentId);
        return ResponseEntity.noContent().build();
    }
}
