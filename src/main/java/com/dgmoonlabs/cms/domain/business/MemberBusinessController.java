package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderCreateRequest;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderResponse;
import com.dgmoonlabs.cms.domain.business.order.service.OrderService;
import com.dgmoonlabs.cms.domain.business.payment.dto.PaymentDtos.PaymentRequest;
import com.dgmoonlabs.cms.domain.business.payment.dto.PaymentDtos.PaymentResponse;
import com.dgmoonlabs.cms.domain.business.payment.service.PaymentService;
import com.dgmoonlabs.cms.domain.business.reservation.dto.ReservationDtos.ReservationCreateRequest;
import com.dgmoonlabs.cms.domain.business.reservation.dto.ReservationDtos.ReservationResponse;
import com.dgmoonlabs.cms.domain.business.reservation.service.ReservationService;
import com.dgmoonlabs.cms.domain.business.review.dto.ReviewDtos.ReviewSaveRequest;
import com.dgmoonlabs.cms.domain.business.review.service.ReviewService;
import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.dgmoonlabs.cms.domain.business.subscription.dto.SubscriptionResponse;
import com.dgmoonlabs.cms.domain.business.subscription.service.SubscriptionService;
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
public class MemberBusinessController {
    private final OrderService orderService;
    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;
    private final SubscriptionService subscriptionService;

    @PostMapping("/api/orders")
    public ResponseEntity<OrderResponse> order(@AuthenticationPrincipal final LoginUser user,
                                               @RequestBody @Valid final OrderCreateRequest request) {
        OrderResponse order = orderService.createOrder(user.id(), request);
        return ResponseEntity.created(URI.create("/api/orders/" + order.id())).body(order);
    }

    @GetMapping("/api/orders/me")
    public ResponseEntity<PagedModel<OrderResponse>> myOrders(@AuthenticationPrincipal final LoginUser user,
                                                              @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(orderService.getMyOrders(user.id(), pageable)));
    }

    @GetMapping("/api/orders/{id}")
    public ResponseEntity<OrderResponse> myOrder(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id) {
        return ResponseEntity.ok(orderService.getMyOrder(user.id(), id));
    }

    @PostMapping("/api/orders/{id}/accept-quote")
    public ResponseEntity<OrderResponse> acceptQuote(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id) {
        return ResponseEntity.ok(orderService.acceptMyQuote(user.id(), id));
    }

    @GetMapping("/api/subscriptions/me")
    public ResponseEntity<PagedModel<SubscriptionResponse>> mySubscriptions(@AuthenticationPrincipal final LoginUser user,
                                                                            @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(subscriptionService.getMine(user.id(), pageable)));
    }

    @PostMapping("/api/subscriptions/{id}/cancel")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(@AuthenticationPrincipal final LoginUser user,
                                                                   @PathVariable final Long id) {
        return ResponseEntity.ok(subscriptionService.cancelMine(user.id(), id));
    }

    @PostMapping("/api/orders/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id) {
        orderService.cancelMyOrder(user.id(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/reservations")
    public ResponseEntity<ReservationResponse> reserve(@AuthenticationPrincipal final LoginUser user,
                                                       @RequestBody @Valid final ReservationCreateRequest request) {
        ReservationResponse reservation = reservationService.reserve(user.id(), request);
        return ResponseEntity.created(URI.create("/api/reservations/" + reservation.id())).body(reservation);
    }

    @GetMapping("/api/reservations/me")
    public ResponseEntity<PagedModel<ReservationResponse>> myReservations(@AuthenticationPrincipal final LoginUser user,
                                                                          @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(reservationService.getMyReservations(user.id(), pageable)));
    }

    @PostMapping("/api/reservations/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id) {
        reservationService.cancelMyReservation(user.id(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/payments")
    public ResponseEntity<PaymentResponse> pay(@AuthenticationPrincipal final LoginUser user,
                                               @RequestBody @Valid final PaymentRequest request) {
        return ResponseEntity.ok(paymentService.pay(user.id(), request));
    }

    @PostMapping("/api/products/{productId}/reviews")
    public ResponseEntity<Void> review(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long productId,
                                       @RequestBody @Valid final ReviewSaveRequest request,
                                       final HttpServletRequest httpRequest) {
        Long id = reviewService.createReview(user, productId, request, httpRequest.getRemoteAddr());
        return ResponseEntity.created(URI.create("/api/products/" + productId + "/reviews/" + id)).build();
    }

    @PostMapping("/api/programs/{programId}/reviews")
    public ResponseEntity<Void> programReview(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long programId,
                                              @RequestBody @Valid final ReviewSaveRequest request,
                                              final HttpServletRequest httpRequest) {
        Long id = reviewService.createProgramReview(user, programId, request, httpRequest.getRemoteAddr());
        return ResponseEntity.created(URI.create("/api/programs/" + programId + "/reviews/" + id)).build();
    }

    @DeleteMapping("/api/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable final Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
