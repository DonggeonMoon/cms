package com.dgmoonlabs.cms.domain.course.controller;

import com.dgmoonlabs.cms.domain.common.file.service.FileService.DownloadableFile;
import com.dgmoonlabs.cms.domain.course.dto.CourseDtos.*;
import com.dgmoonlabs.cms.domain.course.service.CourseService;
import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @GetMapping("/api/courses")
    public ResponseEntity<PagedModel<CourseResponse>> courses(@RequestParam(required = false) final String keyword,
                                                              @PageableDefault(size = 12) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(courseService.getCourses(keyword, null, pageable)));
    }

    @GetMapping("/api/courses/{id:\\d+}")
    public ResponseEntity<CourseResponse> course(@PathVariable final Long id) {
        return ResponseEntity.ok(courseService.getCourse(id));
    }

    @PostMapping("/api/courses/{id:\\d+}/enroll")
    public ResponseEntity<CourseResponse> enroll(@AuthenticationPrincipal final LoginUser member, @PathVariable final Long id) {
        return ResponseEntity.ok(courseService.enroll(id, member));
    }

    @PostMapping("/api/courses/{id:\\d+}/applications/cancel")
    public ResponseEntity<Void> cancelApplication(@AuthenticationPrincipal final LoginUser member, @PathVariable final Long id) {
        courseService.cancelApplication(id, member);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/courses/{id:\\d+}/materials/{materialId}/download")
    public ResponseEntity<Resource> downloadMaterial(@PathVariable final Long id, @PathVariable final Long materialId) {
        DownloadableFile file = courseService.downloadMaterial(id, materialId);
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(file.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.filename(), StandardCharsets.UTF_8).build().toString())
                .body(file.resource());
    }

    @PostMapping("/api/courses/{id:\\d+}/lessons/{lessonId}/quiz/start")
    public ResponseEntity<QuizStartResponse> startQuiz(@AuthenticationPrincipal final LoginUser member, @PathVariable final Long id,
                                                       @PathVariable final Long lessonId) {
        return ResponseEntity.ok(courseService.startQuiz(id, lessonId, member));
    }

    @PostMapping("/api/courses/{id:\\d+}/lessons/{lessonId}/quiz")
    public ResponseEntity<QuizResult> submitQuiz(@AuthenticationPrincipal final LoginUser member, @PathVariable final Long id,
                                                 @PathVariable final Long lessonId, @RequestBody @Valid final QuizSubmitRequest request) {
        return ResponseEntity.ok(courseService.submitQuiz(id, lessonId, member, request));
    }

    @GetMapping("/api/courses/{id:\\d+}/lessons/{lessonId}/comments")
    public ResponseEntity<List<LessonCommentResponse>> lessonComments(@PathVariable final Long id, @PathVariable final Long lessonId) {
        return ResponseEntity.ok(courseService.getLessonComments(id, lessonId));
    }

    @PostMapping("/api/courses/{id:\\d+}/lessons/{lessonId}/comments")
    public ResponseEntity<Void> createLessonComment(@PathVariable final Long id, @PathVariable final Long lessonId,
                                                     @RequestBody @Valid final LessonCommentSaveRequest request) {
        Long commentId = courseService.createLessonComment(id, lessonId, request);
        return ResponseEntity.created(URI.create("/api/courses/" + id + "/lessons/" + lessonId + "/comments/" + commentId)).build();
    }

    @PutMapping("/api/courses/{id:\\d+}/lessons/{lessonId}/comments/{commentId}")
    public ResponseEntity<Void> editLessonComment(@PathVariable final Long id, @PathVariable final Long lessonId,
                                                  @PathVariable final Long commentId,
                                                  @RequestBody @Valid final LessonCommentEditRequest request) {
        courseService.editLessonComment(id, lessonId, commentId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/courses/{id:\\d+}/lessons/{lessonId}/comments/{commentId}")
    public ResponseEntity<Void> deleteLessonComment(@PathVariable final Long id, @PathVariable final Long lessonId,
                                                    @PathVariable final Long commentId) {
        courseService.deleteLessonComment(id, lessonId, commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/courses/me")
    public ResponseEntity<PagedModel<CourseResponse>> mine(@AuthenticationPrincipal final LoginUser member,
                                                           @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(courseService.getMine(member.id(), pageable)));
    }

    @GetMapping("/api/admin/courses/statistics")
    public ResponseEntity<CourseStatisticsResponse> statistics() {
        return ResponseEntity.ok(courseService.getStatistics());
    }

    @GetMapping("/api/admin/courses/qna/unanswered")
    public ResponseEntity<PagedModel<UnansweredQuestionResponse>> unansweredQuestions(@PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(courseService.getUnansweredQuestions(pageable)));
    }

    @GetMapping("/api/admin/courses")
    public ResponseEntity<PagedModel<CourseResponse>> adminCourses(@RequestParam(required = false) final String keyword,
                                                                   @RequestParam(required = false) final Boolean isCourse,
                                                                   @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(courseService.getCourses(keyword, isCourse, pageable)));
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

    @GetMapping("/api/admin/courses/{id}/learners/{memberId}")
    public ResponseEntity<CourseResponse> learnerProgress(@PathVariable final Long id, @PathVariable final Long memberId) {
        return ResponseEntity.ok(courseService.getLearnerProgress(id, memberId));
    }

    @GetMapping("/api/admin/courses/{id}/applications")
    public ResponseEntity<PagedModel<ApplicationResponse>> applications(@PathVariable final Long id,
                                                                         @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(courseService.getApplications(id, pageable)));
    }

    @PostMapping("/api/admin/courses/{id}/applications/{applicationId}/approve")
    public ResponseEntity<Void> approveApplication(@PathVariable final Long id, @PathVariable final Long applicationId) {
        courseService.approveApplication(id, applicationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/admin/courses/{id}/applications/{applicationId}/reject")
    public ResponseEntity<Void> rejectApplication(@PathVariable final Long id, @PathVariable final Long applicationId) {
        courseService.rejectApplication(id, applicationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/admin/courses/{id}/learners/{memberId}/extend")
    public ResponseEntity<Void> extendAccess(@PathVariable final Long id, @PathVariable final Long memberId,
                                             @RequestBody @Valid final ExtendAccessRequest request) {
        courseService.extendAccess(id, memberId, request.accessUntil());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/admin/courses/{id}/learners/{memberId}/refund")
    public ResponseEntity<Void> refundEnrollment(@PathVariable final Long id, @PathVariable final Long memberId) {
        courseService.refundEnrollment(id, memberId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/admin/courses/{id}/lessons/{lessonId}/learners/{memberId}/grant-attempts")
    public ResponseEntity<Void> grantQuizAttempts(@PathVariable final Long id, @PathVariable final Long lessonId,
                                                  @PathVariable final Long memberId, @RequestBody @Valid final GrantAttemptsRequest request) {
        courseService.grantQuizAttempts(id, lessonId, memberId, request.count());
        return ResponseEntity.noContent().build();
    }
}
