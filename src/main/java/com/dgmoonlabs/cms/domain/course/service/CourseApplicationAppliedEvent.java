package com.dgmoonlabs.cms.domain.course.service;

public record CourseApplicationAppliedEvent(Long applicationId, Long courseId, Long memberId) {
}
