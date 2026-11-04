package com.dgmoonlabs.cms.domain.common.grade.controller;

import com.dgmoonlabs.cms.domain.common.grade.dto.GradeDtos.*;
import com.dgmoonlabs.cms.domain.common.grade.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GradeController {
    private final GradeService gradeService;

    @GetMapping("/api/grades")
    public ResponseEntity<List<GradeResponse>> grades() {
        return ResponseEntity.ok(gradeService.getGrades());
    }

    @GetMapping("/api/members/me/grade")
    public ResponseEntity<MyGradeResponse> myGrade() {
        return ResponseEntity.ok(gradeService.getMine());
    }

    @PostMapping("/api/admin/grades")
    public ResponseEntity<GradeResponse> create(@RequestBody @Valid final GradeSaveRequest request) {
        return ResponseEntity.ok(gradeService.create(request));
    }

    @PutMapping("/api/admin/grades/{id}")
    public ResponseEntity<GradeResponse> update(@PathVariable final Long id, @RequestBody @Valid final GradeSaveRequest request) {
        return ResponseEntity.ok(gradeService.update(id, request));
    }

    @DeleteMapping("/api/admin/grades/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        gradeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/members/{memberId}/grade")
    public ResponseEntity<MyGradeResponse> memberGrade(@PathVariable final Long memberId) {
        return ResponseEntity.ok(gradeService.getOf(memberId));
    }

    @PutMapping("/api/admin/members/{memberId}/grade")
    public ResponseEntity<Void> assign(@PathVariable final Long memberId, @RequestBody final MemberGradeRequest request) {
        gradeService.assign(memberId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/members/{memberId}/grade/history")
    public ResponseEntity<List<GradeHistoryResponse>> history(@PathVariable final Long memberId) {
        return ResponseEntity.ok(gradeService.historyOf(memberId));
    }
}
