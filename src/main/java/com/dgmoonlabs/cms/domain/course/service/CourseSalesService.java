package com.dgmoonlabs.cms.domain.course.service;

import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.entity.OrderItem;
import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderItemRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductRepository;
import com.dgmoonlabs.cms.domain.business.order.service.OrderPaidEvent;
import com.dgmoonlabs.cms.domain.business.order.service.OrderRefundedEvent;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.domain.course.constant.CourseApplicationStatus;
import com.dgmoonlabs.cms.domain.course.entity.Course;
import com.dgmoonlabs.cms.domain.course.entity.CourseApplication;
import com.dgmoonlabs.cms.domain.course.entity.CourseEnrollment;
import com.dgmoonlabs.cms.domain.course.repository.CourseApplicationRepository;
import com.dgmoonlabs.cms.domain.course.repository.CourseEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 유료 시리즈/교육과정은 숨긴 판매 상품을 하나씩 두고 일반 주문으로 판다. 그래서 쿠폰·포인트·할인·결제를 그대로 쓰고,
 * 결제가 끝나면 수강 신청 방식과 무관하게 바로 수강 기록을 만들고(자동 승인), 주문이 취소·환불되면 회수한다.
 */
@Service
@RequiredArgsConstructor
public class CourseSalesService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseApplicationRepository applicationRepository;

    public void syncProduct(final Course course) {
        Product product = course.getProductId() == null ? null : productRepository.findById(course.getProductId()).orElse(null);
        if (!course.isPaid()) {
            if (product != null) {
                product.update(product.getName(), product.getDescription(), product.getImageFileId(), product.getPrice(), null,
                        product.getContentHtml(), product.getContentText(), false);
            }
            return;
        }
        boolean onSale = course.isUsing() && course.getDeletedAt() == null;
        if (product == null) {
            product = productRepository.save(Product.builder()
                    .name(course.getTitle())
                    .description("시리즈/교육과정 수강권")
                    .imageFileId(course.getThumbnailFileId())
                    .price(course.getPrice())
                    .contentHtml("")
                    .contentText("")
                    .requiresShipping(false)
                    .courseId(course.getId())
                    .isEnabled(onSale)
                    .build());
            course.linkProduct(product.getId());
        } else {
            product.update(course.getTitle(), "시리즈/교육과정 수강권", course.getThumbnailFileId(), course.getPrice(), null, "", "", onSale);
        }
        product.changeGrades(course.getAccessGradeId(), null, null);
    }

    @EventListener
    public void grantOnPayment(final OrderPaidEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null || order.getUserType() != UserType.MEMBER) {
            return;
        }
        List<Long> productIds = itemRepository.findAllByOrderIdOrderByIdAsc(order.getId()).stream()
                .filter(OrderItem::isActive).map(OrderItem::getProductId).distinct().toList();
        List<Long> targets = productIds.isEmpty() && order.getProductId() != null ? List.of(order.getProductId()) : productIds;
        LocalDateTime now = LocalDateTime.now();
        for (Product product : productRepository.findAllById(targets)) {
            if (product.getCourseId() == null) {
                continue;
            }
            Long courseId = product.getCourseId();
            Long memberId = order.getUserId();
            CourseEnrollment enrollment = enrollmentRepository.findByCourseIdAndMemberId(courseId, memberId)
                    .orElseGet(() -> enrollmentRepository.save(CourseEnrollment.builder().courseId(courseId).memberId(memberId).build()));
            if (enrollment.getOrderId() == null) {
                enrollment.linkOrder(order.getId());
            }
            boolean hasActiveApplication = applicationRepository.findFirstByCourseIdAndMemberIdOrderByIdDesc(courseId, memberId)
                    .filter(application -> application.getStatus() != CourseApplicationStatus.REJECTED
                            && application.getStatus() != CourseApplicationStatus.CANCELED)
                    .isPresent();
            if (!hasActiveApplication) {
                applicationRepository.save(CourseApplication.apply(courseId, memberId)).approve(now);
            }
        }
    }

    @EventListener
    public void revokeOnRefund(final OrderRefundedEvent event) {
        LocalDateTime now = LocalDateTime.now();
        enrollmentRepository.findAllByOrderId(event.orderId()).forEach(enrollment -> enrollment.revoke(now));
    }
}
