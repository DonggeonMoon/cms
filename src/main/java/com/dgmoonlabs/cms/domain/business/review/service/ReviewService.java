package com.dgmoonlabs.cms.domain.business.review.service;

import com.dgmoonlabs.cms.domain.business.order.constant.OrderStatus;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.order.service.ProductService;
import com.dgmoonlabs.cms.domain.business.reservation.constant.ReservationStatus;
import com.dgmoonlabs.cms.domain.business.reservation.repository.ReservationItemRepository;
import com.dgmoonlabs.cms.domain.business.reservation.repository.ReservationRepository;
import com.dgmoonlabs.cms.domain.business.review.constant.ReviewTargetType;
import com.dgmoonlabs.cms.domain.business.review.dto.ReviewDtos.ReviewResponse;
import com.dgmoonlabs.cms.domain.business.review.dto.ReviewDtos.ReviewSaveRequest;
import com.dgmoonlabs.cms.domain.business.review.entity.Review;
import com.dgmoonlabs.cms.domain.business.review.repository.ReviewRepository;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.html.HtmlSanitizer;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import com.dgmoonlabs.cms.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private static final Set<OrderStatus> PURCHASED = EnumSet.of(OrderStatus.PAID, OrderStatus.SHIPPING, OrderStatus.DELIVERED,
            OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.COMPLETED);

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationItemRepository reservationItemRepository;
    private final ProductService productService;
    private final HtmlSanitizer htmlSanitizer;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviews(final Long productId, final Pageable pageable) {
        return reviewRepository.findAllByProductIdAndDeletedAtIsNullOrderByIdDesc(productId, pageable)
                .map(review -> ReviewResponse.of(review, canDelete(review)));
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReservationItemReviews(final Long reservationItemId, final Pageable pageable) {
        return reviewRepository.findAllByReservationItemIdAndDeletedAtIsNullOrderByIdDesc(reservationItemId, pageable)
                .map(review -> ReviewResponse.of(review, canDelete(review)));
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllReviews(final ReviewTargetType targetType, final Pageable pageable) {
        return reviewRepository.findAllByTargetTypeAndDeletedAtIsNullOrderByIdDesc(targetType, pageable)
                .map(review -> ReviewResponse.of(review, true));
    }

    @Transactional
    public Long createReview(final LoginUser member, final Long productId, final ReviewSaveRequest request,
                             final String ipAddress) {
        productService.findProduct(productId);
        if (!orderRepository.existsByUserTypeAndUserIdAndProductIdAndStatusIn(UserType.MEMBER, member.id(), productId, PURCHASED)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        if (reviewRepository.existsByProductIdAndUserIdAndDeletedAtIsNull(productId, member.id())) {
            throw new BusinessException(ErrorCode.ALREADY_SUBMITTED);
        }
        return save(member, request, ipAddress, Review.builder().targetType(ReviewTargetType.PRODUCT).productId(productId));
    }

    @Transactional
    public Long createReservationItemReview(final LoginUser member, final Long reservationItemId, final ReviewSaveRequest request,
                                            final String ipAddress) {
        reservationItemRepository.findByIdAndDeletedAtIsNull(reservationItemId).orElseThrow(BusinessException::notFound);
        if (!reservationRepository.existsByUserTypeAndUserIdAndReservationItemIdAndStatus(UserType.MEMBER, member.id(), reservationItemId, ReservationStatus.CONFIRMED)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        if (reviewRepository.existsByReservationItemIdAndUserIdAndDeletedAtIsNull(reservationItemId, member.id())) {
            throw new BusinessException(ErrorCode.ALREADY_SUBMITTED);
        }
        return save(member, request, ipAddress, Review.builder().targetType(ReviewTargetType.RESERVATION_ITEM).reservationItemId(reservationItemId));
    }

    @Transactional(readOnly = true)
    public Double averageReservationItemRating(final Long reservationItemId) {
        return reviewRepository.averageReservationItemRating(reservationItemId);
    }

    @Transactional
    public void deleteReview(final Long reviewId) {
        Review review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId).orElseThrow(BusinessException::notFound);
        if (!canDelete(review)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        review.delete();
    }

    private Long save(final LoginUser member, final ReviewSaveRequest request, final String ipAddress,
                      final Review.ReviewBuilder target) {
        String html = htmlSanitizer.sanitize(request.contentHtml());
        return reviewRepository.save(target
                .rating(request.rating())
                .title(request.title())
                .contentHtml(html)
                .contentText(htmlSanitizer.toPlainText(html))
                .userType(UserType.MEMBER)
                .userId(member.id())
                .authorName(member.name())
                .ipAddress(ipAddress)
                .build()).getId();
    }

    private boolean canDelete(final Review review) {
        return CurrentUser.isAdmin() || review.getUserId() != null && CurrentUser.is(UserType.MEMBER, review.getUserId());
    }
}
