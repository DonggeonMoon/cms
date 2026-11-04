package com.dgmoonlabs.cms.domain.course.controller;

import com.dgmoonlabs.cms.domain.course.dto.CourseDtos.*;
import com.dgmoonlabs.cms.domain.course.service.CourseService;
import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @GetMapping("/api/courses")
    public ResponseEntity<PagedModel<CourseResponse>> courses(@RequestParam(required = false) final String keyword,
                                                              @PageableDefault(size = 12) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(courseService.getCourses(keyword, pageable)));
    }

    @GetMapping("/api/courses/{id:\\d+}")
    public ResponseEntity<CourseResponse> course(@PathVariable final Long id) {
        return ResponseEntity.ok(courseService.getCourse(id));
    }

    @PostMapping("/api/courses/{id:\\d+}/enroll")
    public ResponseEntity<CourseResponse> enroll(@AuthenticationPrincipal final LoginUser member, @PathVariable final Long id) {
        return ResponseEntity.ok(courseService.enroll(id, member));
    }

    @PostMapping("/api/courses/{id:\\d+}/lessons/{lessonId}/quiz")
    public ResponseEntity<QuizResult> submitQuiz(@AuthenticationPrincipal final LoginUser member, @PathVariable final Long id,
                                                 @PathVariable final Long lessonId, @RequestBody @Valid final QuizSubmitRequest request) {
        return ResponseEntity.ok(courseService.submitQuiz(id, lessonId, member, request));
    }

    @GetMapping("/api/courses/me")
    public ResponseEntity<PagedModel<CourseResponse>> mine(@AuthenticationPrincipal final LoginUser member,
                                                           @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(courseService.getMine(member.id(), pageable)));
    }

    @GetMapping("/api/admin/courses")
    public ResponseEntity<PagedModel<CourseResponse>> adminCourses(@RequestParam(required = false) final String keyword,
                                                                   @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(courseService.getCourses(keyword, pageable)));
    }

    @GetMapping("/api/admin/courses/{id}")
    public ResponseEntity<CourseResponse> adminCourse(@PathVariable final Long id) {
        return ResponseEntity.ok(courseService.getCourse(id));
    }

    @PostMapping("/api/admin/courses")
    public ResponseEntity<Void> create(@RequestBody @Valid final CourseSaveRequest request) {
        return ResponseEntity.created(URI.create("/api/courses/" + courseService.createCourse(request))).build();
    }

    @PutMapping("/api/admin/courses/{id}")
    public ResponseEntity<Void> update(@PathVariable final Long id, @RequestBody @Valid final CourseSaveRequest request) {
        courseService.updateCourse(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/courses/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/courses/{id}/learners")
    public ResponseEntity<PagedModel<LearnerResponse>> learners(@PathVariable final Long id,
                                                                @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(courseService.getLearners(id, pageable)));
    }
}
