package com.dgmoonlabs.cms.domain.common.notification.service;

import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.order.service.OrderPaidEvent;
import com.dgmoonlabs.cms.domain.business.order.service.OrderRefundedEvent;
import com.dgmoonlabs.cms.domain.common.notification.constant.NotificationType;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.domain.common.user.service.MemberJoinedEvent;
import com.dgmoonlabs.cms.domain.course.entity.Course;
import com.dgmoonlabs.cms.domain.course.repository.CourseRepository;
import com.dgmoonlabs.cms.domain.course.service.CourseApplicationAppliedEvent;
import com.dgmoonlabs.cms.domain.course.service.CourseApplicationApprovedEvent;
import com.dgmoonlabs.cms.domain.course.service.CourseCompletedEvent;
import com.dgmoonlabs.cms.domain.course.service.CourseLessonCommentRepliedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final OrderRepository orderRepository;
    private final CourseRepository courseRepository;
    private final NotificationService notificationService;
    private final NotificationSettingService settingService;

    @EventListener
    public void onOrderPaid(final OrderPaidEvent event) {
        if (!settingService.isEnabled(NotificationType.ORDER_PAID)) {
            return;
        }
        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null || order.getUserType() != UserType.MEMBER) {
            return;
        }
        notificationService.notifyAllChannels(order.getUserId(), "결제가 완료되었습니다",
                "주문 번호 " + order.getOrderNumber() + "의 결제가 완료되었습니다.", "/orders/" + order.getId());
    }

    @EventListener
    public void onOrderRefunded(final OrderRefundedEvent event) {
        if (!settingService.isEnabled(NotificationType.ORDER_REFUNDED)) {
            return;
        }
        notificationService.notifyAllChannels(event.memberId(), "환불이 처리되었습니다",
                "주문 번호 " + event.orderId() + "의 환불이 처리되었습니다.", "/orders/" + event.orderId());
    }

    @EventListener
    public void onMemberJoined(final MemberJoinedEvent event) {
        if (!settingService.isEnabled(NotificationType.MEMBER_JOINED)) {
            return;
        }
        notificationService.notifyAllChannels(event.memberId(), "가입을 환영합니다", "회원 가입이 완료되었습니다.", null);
    }

    @EventListener
    public void onCourseApplied(final CourseApplicationAppliedEvent event) {
        if (!settingService.isEnabled(NotificationType.COURSE_APPLIED)) {
            return;
        }
        Course course = findCourse(event.courseId());
        notificationService.notifyAllChannels(event.memberId(), "수강 신청이 접수되었습니다",
                title(course) + " 수강 신청이 접수되었습니다. 승인을 기다려 주세요.", coursePath(course, event.courseId()));
    }

    @EventListener
    public void onCourseApplicationApproved(final CourseApplicationApprovedEvent event) {
        if (!settingService.isEnabled(NotificationType.COURSE_APPROVED)) {
            return;
        }
        Course course = findCourse(event.courseId());
        notificationService.notifyAllChannels(event.memberId(), "수강 신청이 승인되었습니다",
                title(course) + " 수강 신청이 승인되었습니다. 지금 수강을 시작할 수 있습니다.", coursePath(course, event.courseId()));
    }

    @EventListener
    public void onCourseCompleted(final CourseCompletedEvent event) {
        if (!settingService.isEnabled(NotificationType.COURSE_COMPLETED)) {
            return;
        }
        Course course = findCourse(event.courseId());
        String noun = course != null && course.isCourse() ? "교육과정을 수료했습니다" : "시리즈를 다 봤습니다";
        notificationService.notifyAllChannels(event.memberId(), noun, title(course) + " " + noun + ". 축하합니다!",
                coursePath(course, event.courseId()));
    }

    @EventListener
    public void onCourseLessonCommentReplied(final CourseLessonCommentRepliedEvent event) {
        if (!settingService.isEnabled(NotificationType.COURSE_QUESTION_ANSWERED)) {
            return;
        }
        Course course = findCourse(event.courseId());
        notificationService.notifyAllChannels(event.questionAuthorId(), "질문에 답변이 등록되었습니다",
                title(course) + "에 남긴 질문에 답변이 등록되었습니다.", coursePath(course, event.courseId()));
    }

    private Course findCourse(final Long courseId) {
        return courseRepository.findById(courseId).orElse(null);
    }

    private static String title(final Course course) {
        return course == null ? "교육과정" : course.getTitle();
    }

    private static String coursePath(final Course course, final Long courseId) {
        return (course == null || course.isCourse() ? "/courses/" : "/series/") + courseId;
    }
}
