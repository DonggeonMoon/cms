package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.domain.business.discount.dto.DiscountDtos.DiscountResponse;
import com.dgmoonlabs.cms.domain.business.discount.dto.DiscountDtos.DiscountSaveRequest;
import com.dgmoonlabs.cms.domain.business.discount.service.DiscountService;
import com.dgmoonlabs.cms.domain.business.order.constant.OrderStatus;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderCreateRequest;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderResponse;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderStatusRequest;
import com.dgmoonlabs.cms.domain.business.order.constant.OrderReturnStatus;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderReturnDtos.OrderReturnDecisionRequest;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderReturnDtos.OrderReturnResponse;
import com.dgmoonlabs.cms.domain.business.license.dto.LicenseKeyDtos.BulkAddRequest;
import com.dgmoonlabs.cms.domain.business.license.dto.LicenseKeyDtos.BulkAddResult;
import com.dgmoonlabs.cms.domain.business.license.dto.LicenseKeyDtos.LicenseKeyResponse;
import com.dgmoonlabs.cms.domain.business.license.service.LicenseKeyService;
import com.dgmoonlabs.cms.domain.business.stock.dto.StockMovementDtos.AdjustRequest;
import com.dgmoonlabs.cms.domain.business.stock.dto.StockMovementDtos.StockMovementResponse;
import com.dgmoonlabs.cms.domain.business.stock.service.StockMovementService;
import com.dgmoonlabs.cms.domain.business.accounting.dto.AccountingDtos.CollectRequest;
import com.dgmoonlabs.cms.domain.business.accounting.dto.AccountingDtos.ReceivableCreateRequest;
import com.dgmoonlabs.cms.domain.business.accounting.dto.AccountingDtos.ReceivableResponse;
import com.dgmoonlabs.cms.domain.business.accounting.dto.AccountingDtos.SalesReportRow;
import com.dgmoonlabs.cms.domain.business.accounting.dto.AccountingDtos.SettlementRow;
import com.dgmoonlabs.cms.domain.business.accounting.service.AccountingService;
import com.dgmoonlabs.cms.domain.business.currency.dto.ExchangeRateDtos.ExchangeRateRequest;
import com.dgmoonlabs.cms.domain.business.currency.dto.ExchangeRateDtos.ExchangeRateResponse;
import com.dgmoonlabs.cms.domain.business.currency.service.ExchangeRateService;
import com.dgmoonlabs.cms.domain.business.marketing.dto.MarketingDtos.CampaignResponse;
import com.dgmoonlabs.cms.domain.business.marketing.dto.MarketingDtos.CampaignSendRequest;
import com.dgmoonlabs.cms.domain.business.marketing.service.MarketingCampaignService;
import com.dgmoonlabs.cms.domain.business.vote.dto.VoteDtos.VoteResponse;
import com.dgmoonlabs.cms.domain.business.vote.dto.VoteDtos.VoteSaveRequest;
import com.dgmoonlabs.cms.domain.business.vote.service.VoteService;
import com.dgmoonlabs.cms.domain.business.order.dto.ProductDtos.ProductSaveRequest;
import com.dgmoonlabs.cms.domain.business.order.dto.ProductDtos.GradePriceRequest;
import com.dgmoonlabs.cms.domain.business.order.dto.ProductDtos.GradePriceResponse;
import com.dgmoonlabs.cms.domain.business.order.service.FundingService;
import com.dgmoonlabs.cms.domain.business.order.service.OrderService;
import com.dgmoonlabs.cms.domain.business.order.service.ProductService;
import com.dgmoonlabs.cms.domain.business.payment.dto.PaymentDtos.PaymentResponse;
import com.dgmoonlabs.cms.domain.business.payment.service.PaymentService;
import com.dgmoonlabs.cms.domain.business.reservation.constant.ReservationStatus;
import com.dgmoonlabs.cms.domain.business.reservation.dto.ReservationDtos.ExtendRequest;
import com.dgmoonlabs.cms.domain.business.reservation.dto.ReservationDtos.ExtensionResponse;
import com.dgmoonlabs.cms.domain.business.reservation.dto.ReservationDtos.CheckInRequest;
import com.dgmoonlabs.cms.domain.business.reservation.dto.ReservationDtos.ProgramSaveRequest;
import com.dgmoonlabs.cms.domain.business.reservation.dto.ReservationDtos.QueueStatusRequest;
import com.dgmoonlabs.cms.domain.business.reservation.dto.ReservationDtos.SessionResponse;
import com.dgmoonlabs.cms.domain.business.reservation.dto.ReservationDtos.SessionSaveRequest;
import java.util.List;
import com.dgmoonlabs.cms.domain.business.reservation.dto.ReservationDtos.ReservationCreateRequest;
import com.dgmoonlabs.cms.domain.business.reservation.dto.ReservationDtos.ReservationResponse;
import com.dgmoonlabs.cms.domain.business.reservation.dto.ReservationDtos.ReturnRequest;
import com.dgmoonlabs.cms.domain.business.reservation.dto.ReservationDtos.WorkCompleteRequest;
import com.dgmoonlabs.cms.domain.business.reservation.dto.ReservationDtos.WorkerRequest;
import com.dgmoonlabs.cms.domain.business.reservation.service.ReservationService;
import com.dgmoonlabs.cms.domain.business.review.constant.ReviewTargetType;
import com.dgmoonlabs.cms.domain.business.review.dto.ReviewDtos.ReviewResponse;
import com.dgmoonlabs.cms.domain.business.review.service.ReviewService;
import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.validation.Valid;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.QuoteRequest;
import com.dgmoonlabs.cms.domain.business.subscription.dto.SubscriptionRequests.PauseRequest;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class BusinessAdminController {
    private final ProductService productService;
    private final DiscountService discountService;
    private final OrderService orderService;
    private final LicenseKeyService licenseKeyService;
    private final StockMovementService stockMovementService;
    private final AccountingService accountingService;
    private final ExchangeRateService exchangeRateService;
    private final MarketingCampaignService marketingCampaignService;
    private final VoteService voteService;
    private final SubscriptionService subscriptionService;
    private final FundingService fundingService;
    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;

    @PostMapping("/products")
    public ResponseEntity<Void> createProduct(@RequestBody @Valid final ProductSaveRequest request) {
        return ResponseEntity.created(URI.create("/api/products/" + productService.createProduct(request))).build();
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Void> updateProduct(@PathVariable final Long id, @RequestBody @Valid final ProductSaveRequest request) {
        productService.updateProduct(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable final Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/products/{id}/grade-prices")
    public ResponseEntity<List<GradePriceResponse>> gradePrices(@PathVariable final Long id) {
        return ResponseEntity.ok(productService.getGradePrices(id));
    }

    @PutMapping("/products/{id}/grade-prices")
    public ResponseEntity<List<GradePriceResponse>> updateGradePrices(@PathVariable final Long id,
                                                                       @RequestBody @Valid final List<@Valid GradePriceRequest> requests) {
        return ResponseEntity.ok(productService.updateGradePrices(id, requests));
    }

    @GetMapping("/discounts")
    public ResponseEntity<PagedModel<DiscountResponse>> discounts(@PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(discountService.getDiscounts(pageable)));
    }

    @PostMapping("/discounts")
    public ResponseEntity<Void> createDiscount(@RequestBody @Valid final DiscountSaveRequest request) {
        return ResponseEntity.created(URI.create("/api/admin/discounts/" + discountService.createDiscount(request))).build();
    }

    @PutMapping("/discounts/{id}")
    public ResponseEntity<Void> updateDiscount(@PathVariable final Long id, @RequestBody @Valid final DiscountSaveRequest request) {
        discountService.updateDiscount(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/discounts/{id}")
    public ResponseEntity<Void> deleteDiscount(@PathVariable final Long id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders")
    public ResponseEntity<PagedModel<OrderResponse>> orders(@RequestParam(required = false) final OrderStatus status,
                                                            @RequestParam(required = false) final String keyword,
                                                            @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(orderService.searchOrders(status, keyword, pageable)));
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@AuthenticationPrincipal final LoginUser admin,
                                                     @RequestBody @Valid final OrderCreateRequest request,
                                                     @RequestParam(defaultValue = "false") final boolean paid) {
        OrderResponse order = orderService.createAdminOrder(admin.id(), request, paid);
        return ResponseEntity.created(URI.create("/api/admin/orders/" + order.id())).body(order);
    }

    @PostMapping("/orders/{id}/pay")
    public ResponseEntity<Void> markOrderPaid(@PathVariable final Long id) {
        orderService.markPaid(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/orders/{id}/quote")
    public ResponseEntity<OrderResponse> quote(@PathVariable final Long id, @RequestBody @Valid final QuoteRequest request) {
        return ResponseEntity.ok(orderService.quote(id, request));
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<PagedModel<SubscriptionResponse>> subscriptions(@PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(subscriptionService.search(pageable)));
    }

    @PostMapping("/subscriptions/{id}/cancel")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(@PathVariable final Long id) {
        return ResponseEntity.ok(subscriptionService.cancel(id));
    }

    @PostMapping("/subscriptions/{id}/pause")
    public ResponseEntity<SubscriptionResponse> pauseSubscription(@PathVariable final Long id,
                                                                  @RequestBody(required = false) final PauseRequest request) {
        return ResponseEntity.ok(subscriptionService.pause(id, request == null ? null : request.resumeOn()));
    }

    @PostMapping("/subscriptions/{id}/resume")
    public ResponseEntity<SubscriptionResponse> resumeSubscription(@PathVariable final Long id) {
        return ResponseEntity.ok(subscriptionService.resume(id));
    }

    @PostMapping("/subscriptions/renew")
    public ResponseEntity<Map<String, Integer>> renewSubscriptions() {
        return ResponseEntity.ok(Map.of("renewed", subscriptionService.renewDue(LocalDate.now())));
    }

    @PostMapping("/products/funding/resolve")
    public ResponseEntity<Map<String, Integer>> resolveFunding() {
        return ResponseEntity.ok(Map.of("failed", fundingService.resolveDue(LocalDateTime.now())));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Void> changeOrderStatus(@PathVariable final Long id, @RequestBody @Valid final OrderStatusRequest request) {
        orderService.changeStatus(id, request.status());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders/return-requests")
    public ResponseEntity<PagedModel<OrderReturnResponse>> returnRequests(@RequestParam(required = false) final OrderReturnStatus status,
                                                                          @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(orderService.searchReturns(status, pageable)));
    }

    @PostMapping("/orders/return-requests/{id}/approve")
    public ResponseEntity<OrderReturnResponse> approveReturnRequest(@PathVariable final Long id) {
        return ResponseEntity.ok(orderService.approveReturn(id));
    }

    @PostMapping("/orders/return-requests/{id}/reject")
    public ResponseEntity<OrderReturnResponse> rejectReturnRequest(@PathVariable final Long id,
                                                                    @RequestBody(required = false) final OrderReturnDecisionRequest request) {
        return ResponseEntity.ok(orderService.rejectReturn(id, request == null ? null : request.adminMemo()));
    }

    @PostMapping("/license-keys/bulk")
    public ResponseEntity<BulkAddResult> addLicenseKeys(@RequestBody @Valid final BulkAddRequest request) {
        return ResponseEntity.ok(licenseKeyService.addBulk(request));
    }

    @GetMapping("/license-keys")
    public ResponseEntity<PagedModel<LicenseKeyResponse>> licenseKeys(@RequestParam final Long productId,
                                                                      @PageableDefault(size = 50) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(licenseKeyService.search(productId, pageable)));
    }

    @GetMapping("/license-keys/available-count")
    public ResponseEntity<Long> availableLicenseKeyCount(@RequestParam final Long productId) {
        return ResponseEntity.ok(licenseKeyService.availableCount(productId));
    }

    @DeleteMapping("/license-keys/{id}")
    public ResponseEntity<Void> removeLicenseKey(@PathVariable final Long id) {
        licenseKeyService.remove(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stock-movements")
    public ResponseEntity<PagedModel<StockMovementResponse>> stockMovements(@RequestParam final Long productId,
                                                                             @PageableDefault(size = 50) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(stockMovementService.search(productId, pageable)));
    }

    @PostMapping("/stock-movements/adjust")
    public ResponseEntity<StockMovementResponse> adjustStock(@RequestBody @Valid final AdjustRequest request) {
        return ResponseEntity.ok(stockMovementService.adjust(request));
    }

    @PostMapping("/receivables")
    public ResponseEntity<ReceivableResponse> createReceivable(@RequestBody @Valid final ReceivableCreateRequest request) {
        return ResponseEntity.ok(accountingService.createReceivable(request));
    }

    @GetMapping("/receivables")
    public ResponseEntity<PagedModel<ReceivableResponse>> receivables(@RequestParam(required = false) final Long memberId,
                                                                       @PageableDefault(size = 50) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(accountingService.search(memberId, pageable)));
    }

    @PostMapping("/receivables/{id}/collect")
    public ResponseEntity<ReceivableResponse> collectReceivable(@PathVariable final Long id,
                                                                 @RequestBody @Valid final CollectRequest request) {
        return ResponseEntity.ok(accountingService.collect(id, request));
    }

    @GetMapping("/sales-report")
    public ResponseEntity<List<SalesReportRow>> salesReport(@RequestParam final LocalDate from, @RequestParam final LocalDate to) {
        return ResponseEntity.ok(accountingService.salesReport(from, to));
    }

    @GetMapping("/settlements")
    public ResponseEntity<List<SettlementRow>> settlements(@RequestParam final LocalDate from, @RequestParam final LocalDate to) {
        return ResponseEntity.ok(accountingService.settlementSummary(from, to));
    }

    @GetMapping("/exchange-rates")
    public ResponseEntity<List<ExchangeRateResponse>> exchangeRates() {
        return ResponseEntity.ok(exchangeRateService.search());
    }

    @PutMapping("/exchange-rates")
    public ResponseEntity<ExchangeRateResponse> saveExchangeRate(@RequestBody @Valid final ExchangeRateRequest request) {
        return ResponseEntity.ok(exchangeRateService.upsert(request));
    }

    @DeleteMapping("/exchange-rates/{id}")
    public ResponseEntity<Void> removeExchangeRate(@PathVariable final Long id) {
        exchangeRateService.remove(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/marketing/campaigns")
    public ResponseEntity<CampaignResponse> sendCampaign(@RequestBody @Valid final CampaignSendRequest request) {
        return ResponseEntity.ok(marketingCampaignService.send(request));
    }

    @GetMapping("/marketing/campaigns")
    public ResponseEntity<PagedModel<CampaignResponse>> campaignHistory(@PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(marketingCampaignService.history(pageable)));
    }

    @PostMapping("/votes")
    public ResponseEntity<VoteResponse> createVote(@RequestBody @Valid final VoteSaveRequest request) {
        VoteResponse vote = voteService.create(request);
        return ResponseEntity.created(URI.create("/api/admin/votes/" + vote.id())).body(vote);
    }

    @GetMapping("/votes")
    public ResponseEntity<PagedModel<VoteResponse>> votes(@PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(voteService.search(pageable)));
    }

    @PostMapping("/votes/{id}/close")
    public ResponseEntity<VoteResponse> closeVote(@PathVariable final Long id) {
        return ResponseEntity.ok(voteService.close(id));
    }

    @DeleteMapping("/votes/{id}")
    public ResponseEntity<Void> removeVote(@PathVariable final Long id) {
        voteService.remove(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reservation-items")
    public ResponseEntity<Void> createProgram(@RequestBody @Valid final ProgramSaveRequest request) {
        return ResponseEntity.created(URI.create("/api/reservation-items/" + reservationService.createProgram(request))).build();
    }

    @PutMapping("/reservation-items/{id}")
    public ResponseEntity<Void> updateProgram(@PathVariable final Long id, @RequestBody @Valid final ProgramSaveRequest request) {
        reservationService.updateProgram(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reservation-items/{id}")
    public ResponseEntity<Void> deleteProgram(@PathVariable final Long id) {
        reservationService.deleteProgram(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reservation-items/{id}/sessions")
    public ResponseEntity<SessionResponse> createSession(@PathVariable final Long id, @RequestBody @Valid final SessionSaveRequest request) {
        return ResponseEntity.ok(reservationService.createSession(id, request));
    }

    @PutMapping("/reservation-items/{id}/sessions/{sessionId}")
    public ResponseEntity<SessionResponse> updateSession(@PathVariable final Long id, @PathVariable final Long sessionId,
                                                         @RequestBody @Valid final SessionSaveRequest request) {
        return ResponseEntity.ok(reservationService.updateSession(id, sessionId, request));
    }

    @DeleteMapping("/reservation-items/{id}/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable final Long id, @PathVariable final Long sessionId) {
        reservationService.deleteSession(id, sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reservations/{id}/extend")
    public ResponseEntity<ReservationResponse> extendReservation(@PathVariable final Long id, @RequestBody @Valid final ExtendRequest request) {
        return ResponseEntity.ok(reservationService.extend(id, request));
    }

    @PostMapping("/reservations/{id}/extension/cancel")
    public ResponseEntity<ReservationResponse> cancelExtension(@PathVariable final Long id) {
        return ResponseEntity.ok(reservationService.cancelExtension(id));
    }

    @GetMapping("/reservations/{id}/extensions")
    public ResponseEntity<List<ExtensionResponse>> extensions(@PathVariable final Long id) {
        return ResponseEntity.ok(reservationService.getExtensions(id));
    }

    @GetMapping("/reservations")
    public ResponseEntity<PagedModel<ReservationResponse>> reservations(@RequestParam(required = false) final Long programId,
                                                                        @RequestParam(required = false) final ReservationStatus status,
                                                                        @RequestParam(required = false) final String keyword,
                                                                        @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(reservationService.searchReservations(programId, status, keyword, pageable)));
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@AuthenticationPrincipal final LoginUser admin,
                                                                 @RequestBody @Valid final ReservationCreateRequest request,
                                                                 @RequestParam(defaultValue = "false") final boolean paid) {
        ReservationResponse reservation = reservationService.reserveAsAdmin(admin.id(), request, paid);
        return ResponseEntity.created(URI.create("/api/admin/reservations/" + reservation.id())).body(reservation);
    }

    @PostMapping("/reservations/{id}/confirm")
    public ResponseEntity<Void> confirmReservation(@PathVariable final Long id) {
        reservationService.confirmReservation(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reservations/{id}/worker")
    public ResponseEntity<ReservationResponse> assignWorker(@PathVariable final Long id, @RequestBody @Valid final WorkerRequest request) {
        return ResponseEntity.ok(reservationService.assignWorker(id, request));
    }

    @PostMapping("/reservations/{id}/complete")
    public ResponseEntity<ReservationResponse> completeWork(@PathVariable final Long id,
                                                            @RequestBody @Valid final WorkCompleteRequest request) {
        return ResponseEntity.ok(reservationService.completeWork(id, request));
    }

    @PostMapping("/reservations/{id}/return")
    public ResponseEntity<ReservationResponse> returnItems(@PathVariable final Long id, @RequestBody @Valid final ReturnRequest request) {
        return ResponseEntity.ok(reservationService.returnItems(id, request));
    }

    @PostMapping("/reservations/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable final Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reservations/check-in")
    public ResponseEntity<ReservationResponse> checkIn(@RequestBody @Valid final CheckInRequest request) {
        return ResponseEntity.ok(reservationService.checkIn(request.code()));
    }

    @GetMapping("/reservations/queue")
    public ResponseEntity<List<ReservationResponse>> queue(@RequestParam final Long programId,
                                                            @RequestParam(required = false) final LocalDate date) {
        return ResponseEntity.ok(reservationService.getQueue(programId, date));
    }

    @PostMapping("/reservations/{id}/queue-status")
    public ResponseEntity<ReservationResponse> changeQueueStatus(@PathVariable final Long id,
                                                                  @RequestBody @Valid final QueueStatusRequest request) {
        return ResponseEntity.ok(reservationService.changeQueueStatus(id, request.status()));
    }

    @GetMapping("/payments")
    public ResponseEntity<PagedModel<PaymentResponse>> payments(@RequestParam(required = false) final String keyword,
                                                                @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(paymentService.getPayments(keyword, pageable)));
    }

    @GetMapping("/reviews")
    public ResponseEntity<PagedModel<ReviewResponse>> reviews(
            @RequestParam(defaultValue = "PRODUCT") final ReviewTargetType targetType,
            @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(reviewService.getAllReviews(targetType, pageable)));
    }
}
