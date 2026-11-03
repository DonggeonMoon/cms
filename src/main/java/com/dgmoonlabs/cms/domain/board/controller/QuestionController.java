package com.dgmoonlabs.cms.domain.board.controller;

import com.dgmoonlabs.cms.domain.board.dto.AnswerSaveRequest;
import com.dgmoonlabs.cms.domain.board.dto.PasswordRequest;
import com.dgmoonlabs.cms.domain.board.dto.QuestionResponse;
import com.dgmoonlabs.cms.domain.board.dto.QuestionSaveRequest;
import com.dgmoonlabs.cms.domain.board.service.QuestionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    @GetMapping("/api/questions")
    public ResponseEntity<PagedModel<QuestionResponse.Summary>> list(@RequestParam(required = false) final String keyword,
                                                                     @RequestParam(defaultValue = "false") final boolean unanswered,
                                                                     @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(questionService.getQuestions(keyword, unanswered, pageable)));
    }

    @GetMapping("/api/questions/{id}")
    public ResponseEntity<QuestionResponse.Detail> get(@PathVariable final Long id) {
        return ResponseEntity.ok(questionService.getQuestion(id, null));
    }

    @PostMapping("/api/questions/{id}/access")
    public ResponseEntity<QuestionResponse.Detail> access(@PathVariable final Long id, @RequestBody final PasswordRequest request) {
        return ResponseEntity.ok(questionService.getQuestion(id, request.password()));
    }

    @PostMapping("/api/questions")
    public ResponseEntity<Void> create(@RequestBody @Valid final QuestionSaveRequest request,
                                       final HttpServletRequest httpRequest) {
        Long id = questionService.createQuestion(request, httpRequest.getRemoteAddr());
        return ResponseEntity.created(URI.create("/api/questions/" + id)).build();
    }

    @PutMapping("/api/questions/{id}")
    public ResponseEntity<Void> update(@PathVariable final Long id, @RequestBody @Valid final QuestionSaveRequest request) {
        questionService.updateQuestion(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/questions/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id,
                                       @RequestBody(required = false) final PasswordRequest request) {
        questionService.deleteQuestion(id, request == null ? null : request.password());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/admin/questions/{id}/answer")
    public ResponseEntity<Void> answer(@PathVariable final Long id, @RequestBody @Valid final AnswerSaveRequest request) {
        questionService.saveAnswer(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/questions/{id}/answer")
    public ResponseEntity<Void> deleteAnswer(@PathVariable final Long id) {
        questionService.deleteAnswer(id);
        return ResponseEntity.noContent().build();
    }
}
