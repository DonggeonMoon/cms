package com.dgmoonlabs.cms.domain.course.service;

public record CourseLessonCommentRepliedEvent(Long questionId, Long replyId, Long courseId, Long lessonId, Long questionAuthorId) {
}
