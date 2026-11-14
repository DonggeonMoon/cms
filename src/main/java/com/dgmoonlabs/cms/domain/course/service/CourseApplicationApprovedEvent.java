package com.dgmoonlabs.cms.domain.course.service;

public record CourseApplicationApprovedEvent(Long applicationId, Long courseId, Long memberId) {
}
