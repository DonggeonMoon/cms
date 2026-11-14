package com.dgmoonlabs.cms.domain.course.controller;

import com.dgmoonlabs.cms.domain.course.dto.QuizDtos.*;
import com.dgmoonlabs.cms.domain.course.service.QuizService;
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
public class QuizController {
    private final QuizService quizService;

    @GetMapping("/api/admin/quizzes")
    public ResponseEntity<PagedModel<QuizResponse>> list(@RequestParam(required = false) final String keyword,
                                                           @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(quizService.getQuizzes(keyword, pageable)));
    }

    @GetMapping("/api/admin/quizzes/{id}")
    public ResponseEntity<QuizResponse> get(@PathVariable final Long id) {
        return ResponseEntity.ok(quizService.getQuiz(id));
    }

    @GetMapping("/api/admin/quizzes/{id}/statistics")
    public ResponseEntity<QuizStatisticsResponse> statistics(@PathVariable final Long id) {
        return ResponseEntity.ok(quizService.getStatistics(id));
    }

    @PostMapping("/api/admin/quizzes")
    public ResponseEntity<Void> create(@RequestBody @Valid final QuizSaveRequest request) {
        return ResponseEntity.created(URI.create("/api/admin/quizzes/" + quizService.createQuiz(request))).build();
    }

    @PutMapping("/api/admin/quizzes/{id}")
    public ResponseEntity<Void> update(@PathVariable final Long id, @RequestBody @Valid final QuizSaveRequest request) {
        quizService.updateQuiz(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/quizzes/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }
}
