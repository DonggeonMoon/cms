package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderCreateRequest;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderResponse;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderReturnDtos.OrderReturnCreateRequest;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderReturnDtos.OrderReturnResponse;
import com.dgmoonlabs.cms.domain.business.order.service.OrderService;
import com.dgmoonlabs.cms.domain.business.license.dto.LicenseKeyDtos.MyLicenseKeyResponse;
import com.dgmoonlabs.cms.domain.business.license.service.LicenseKeyService;
import com.dgmoonlabs.cms.domain.business.vote.dto.VoteDtos.BallotRequest;
import com.dgmoonlabs.cms.domain.business.vote.dto.VoteDtos.MyVoteResponse;
import com.dgmoonlabs.cms.domain.business.vote.service.VoteService;
import com.dgmoonlabs.cms.domain.business.payment.dto.PaymentDtos.PaymentRequest;
import com.dgmoonlabs.cms.domain.business.payment.dto.PaymentDtos.PaymentResponse;
import com.dgmoonlabs.cms.domain.business.payment.service.PaymentService;
import com.dgmoonlabs.cms.domain.business.reservation.dto.ReservationDtos.ExtendRequest;
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

import com.dgmoonlabs.cms.domain.business.order.dto.ProductDtos.ProductSummary;
import com.dgmoonlabs.cms.domain.business.subscription.dto.SubscriptionRequests.PauseRequest;
import com.dgmoonlabs.cms.domain.business.subscription.dto.SubscriptionRequests.PlanChangeRequest;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberBusinessController {
    private final OrderService orderService;
    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;
    private final SubscriptionService subscriptionService;
    private final LicenseKeyService licenseKeyService;
    private final VoteService voteService;

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

    @PostMapping("/api/subscriptions/{id}/pause")
    public ResponseEntity<SubscriptionResponse> pauseSubscription(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id,
                                                                  @RequestBody(required = false) final PauseRequest request) {
        return ResponseEntity.ok(subscriptionService.pauseMine(user.id(), id, request == null ? null : request.resumeOn()));
    }

    @PostMapping("/api/subscriptions/{id}/resume")
    public ResponseEntity<SubscriptionResponse> resumeSubscription(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id) {
        return ResponseEntity.ok(subscriptionService.resumeMine(user.id(), id));
    }

    @GetMapping("/api/subscriptions/{id}/plans")
    public ResponseEntity<List<ProductSummary>> subscriptionPlans(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id) {
        return ResponseEntity.ok(subscriptionService.getPlanOptions(user.id(), id));
    }

    @PutMapping("/api/subscriptions/{id}/plan")
    public ResponseEntity<SubscriptionResponse> changeSubscriptionPlan(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id,
                                                                       @RequestBody @Valid final PlanChangeRequest request) {
        return ResponseEntity.ok(subscriptionService.changePlanMine(user.id(), id, request.productId()));
    }

    @DeleteMapping("/api/subscriptions/{id}/plan")
    public ResponseEntity<SubscriptionResponse> cancelSubscriptionPlanChange(@AuthenticationPrincipal final LoginUser user,
                                                                             @PathVariable final Long id) {
        return ResponseEntity.ok(subscriptionService.cancelPlanChangeMine(user.id(), id));
    }

    @PostMapping("/api/orders/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id) {
        orderService.cancelMyOrder(user.id(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/orders/{id}/return-requests")
    public ResponseEntity<OrderReturnResponse> requestReturn(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id,
                                                              @RequestBody @Valid final OrderReturnCreateRequest request) {
        return ResponseEntity.ok(orderService.requestReturn(user.id(), id, request));
    }

    @GetMapping("/api/orders/return-requests/me")
    public ResponseEntity<PagedModel<OrderReturnResponse>> myReturnRequests(@AuthenticationPrincipal final LoginUser user,
                                                                            @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(orderService.getMyReturns(user.id(), pageable)));
    }

    @GetMapping("/api/license-keys/me")
    public ResponseEntity<PagedModel<MyLicenseKeyResponse>> myLicenseKeys(@AuthenticationPrincipal final LoginUser user,
                                                                          @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(licenseKeyService.getMine(user.id(), pageable)));
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

    @PostMapping("/api/reservations/{id}/extend")
    public ResponseEntity<ReservationResponse> extendReservation(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id,
                                                                 @RequestBody @Valid final ExtendRequest request) {
        return ResponseEntity.ok(reservationService.extendMine(user.id(), id, request));
    }

    @PostMapping("/api/reservations/{id}/extension/cancel")
    public ResponseEntity<ReservationResponse> cancelExtension(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id) {
        return ResponseEntity.ok(reservationService.cancelMyExtension(user.id(), id));
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

    @PostMapping("/api/reservation-items/{programId}/reviews")
    public ResponseEntity<Void> programReview(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long programId,
                                              @RequestBody @Valid final ReviewSaveRequest request,
                                              final HttpServletRequest httpRequest) {
        Long id = reviewService.createProgramReview(user, programId, request, httpRequest.getRemoteAddr());
        return ResponseEntity.created(URI.create("/api/reservation-items/" + programId + "/reviews/" + id)).build();
    }

    @DeleteMapping("/api/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable final Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/votes")
    public ResponseEntity<PagedModel<MyVoteResponse>> votes(@AuthenticationPrincipal final LoginUser user,
                                                             @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(voteService.searchForMember(user.id(), pageable)));
    }

    @PostMapping("/api/votes/{id}/ballots")
    public ResponseEntity<MyVoteResponse> castBallot(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id,
                                                      @RequestBody @Valid final BallotRequest request) {
        return ResponseEntity.ok(voteService.castBallot(user.id(), id, request));
    }
}
