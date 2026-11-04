package com.dgmoonlabs.cms.domain.business.order.service;

import com.dgmoonlabs.cms.domain.business.BusinessNumbers;
import com.dgmoonlabs.cms.domain.business.discount.service.DiscountService;
import com.dgmoonlabs.cms.domain.business.order.constant.OrderStatus;
import com.dgmoonlabs.cms.domain.business.order.constant.ProductSaleType;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderCreateRequest;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderResponse;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.QuoteRequest;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.ShipmentRequest;
import com.dgmoonlabs.cms.domain.business.order.dto.ProductDtos.DonationProgress;
import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.entity.OrderItem;
import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import com.dgmoonlabs.cms.domain.business.order.entity.product.ProductOption;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderItemRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductOptionRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductRepository;
import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentTargetType;
import com.dgmoonlabs.cms.domain.business.payment.service.PaymentService;
import com.dgmoonlabs.cms.domain.business.shipping.service.ShippingService;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderPreviewRequest;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderPreviewResponse;
import com.dgmoonlabs.cms.domain.common.coupon.service.CouponService;
import com.dgmoonlabs.cms.domain.common.grade.service.GradeService;
import com.dgmoonlabs.cms.domain.common.point.service.PointService;
import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.service.RegexService;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository optionRepository;
    private final DiscountService discountService;
    private final PaymentService paymentService;
    private final ShippingService shippingService;
    private final RegexService regexService;
    private final ApplicationEventPublisher events;
    private final GradeService gradeService;
    private final CouponService couponService;
    private final PointService pointService;

    public record Line(Long productId, Long productOptionId, int quantity, Long discountId) {
    }

    public record Discounts(Long couponIssueId, long usePoints) {
        public static final Discounts NONE = new Discounts(null, 0);
    }

    private record Priced(List<OrderItem> items, BigDecimal itemsAmount, BigDecimal shippingFee, BigDecimal couponDiscount,
                          long pointsUsed) {
        BigDecimal total() {
            return itemsAmount.subtract(couponDiscount).subtract(BigDecimal.valueOf(pointsUsed)).add(shippingFee);
        }
    }

    public record Receiver(String receiverName, String phoneNumber, String postalCode, String address, String detailAddress) {
        static Receiver of(final OrderCreateRequest request) {
            return new Receiver(request.receiverName(), request.phoneNumber(), request.postalCode(), request.address(),
                    request.detailAddress());
        }

        boolean hasAddress() {
            return address != null && !address.isBlank();
        }
    }

    @Transactional
    public OrderResponse createOrder(final Long memberId, final OrderCreateRequest request) {
        return create(UserType.MEMBER, memberId, request, false);
    }

    @Transactional
    public OrderResponse createAdminOrder(final Long adminId, final OrderCreateRequest request, final boolean paid) {
        return create(UserType.ADMIN, adminId, request, paid);
    }

    private OrderResponse create(final UserType userType, final Long userId, final OrderCreateRequest request,
                                 final boolean paid) {
        Product product = productRepository.findForUpdate(request.productId())
                .filter(Product::isOnSale)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE));
        gradeService.require(product.getAccessGradeId());
        return switch (product.getSaleType()) {
            case DONATION -> donate(userType, userId, product, request, paid);
            case PURCHASE -> place(userType, userId,
                    List.of(new Line(product.getId(), request.productOptionId(), request.quantity(), request.discountId())),
                    Receiver.of(request), new Discounts(request.couponIssueId(), request.usePoints() == null ? 0 : request.usePoints()),
                    paid);
            default -> createSingle(userType, userId, product, request, paid);
        };
    }

    @Transactional
    public OrderResponse place(final UserType userType, final Long userId, final List<Line> lines, final Receiver receiver,
                               final Discounts discounts, final boolean paid) {
        Long memberId = userType == UserType.MEMBER ? userId : null;
        Priced priced = price(memberId, lines, receiver, memberId == null ? Discounts.NONE : discounts, true);
        List<OrderItem> items = priced.items();
        if (items.stream().anyMatch(OrderItem::isRequiresShipping) && !receiver.hasAddress() && memberId != null) {
            throw new BusinessException(ErrorCode.ADDRESS_REQUIRED);
        }
        BigDecimal total = priced.total();
        OrderItem first = items.getFirst();
        Order order = orderRepository.save(Order.builder()
                .orderNumber(nextOrderNumber())
                .userType(userType)
                .userId(userId)
                .productId(first.getProductId())
                .saleType(ProductSaleType.PURCHASE)
                .productOptionId(items.size() == 1 ? first.getProductOptionId() : null)
                .productName(headline(items))
                .quantity(items.stream().mapToInt(OrderItem::getQuantity).sum())
                .discountId(items.size() == 1 ? first.getDiscountId() : null)
                .itemsAmount(priced.itemsAmount())
                .shippingFee(priced.shippingFee())
                .couponIssueId(priced.couponDiscount().signum() > 0 ? discounts.couponIssueId() : null)
                .couponDiscount(priced.couponDiscount())
                .pointsUsed(priced.pointsUsed())
                .totalPrice(total)
                .status(paid || total.signum() == 0 ? OrderStatus.PAID : OrderStatus.PENDING_PAYMENT)
                .canDeliver(receiver.hasAddress())
                .receiverName(receiver.receiverName())
                .phoneNumber(receiver.phoneNumber())
                .postalCode(receiver.postalCode())
                .address(receiver.address())
                .detailAddress(receiver.detailAddress())
                .build());
        items.forEach(item -> item.assignOrder(order.getId()));
        List<OrderItem> saved = itemRepository.saveAll(items);
        if (order.getCouponIssueId() != null) {
            couponService.use(memberId, order.getCouponIssueId(), order.getId());
        }
        pointService.use(memberId, priced.pointsUsed(), order.getId());
        if (order.getStatus() == OrderStatus.PAID) {
            events.publishEvent(new OrderPaidEvent(order.getId()));
        }
        return OrderResponse.of(order, saved);
    }

    @Transactional(readOnly = true)
    public OrderPreviewResponse preview(final Long memberId, final OrderPreviewRequest request) {
        List<Line> lines = request.lines().stream()
                .map(line -> new Line(line.productId(), line.productOptionId(), line.quantity(), line.discountId())).toList();
        Receiver receiver = new Receiver(null, null, request.postalCode(), "-", null);
        Priced priced = price(memberId, lines, receiver,
                new Discounts(request.couponIssueId(), request.usePoints() == null ? 0 : request.usePoints()), false);
        List<OrderItem> items = priced.items();
        long toEarn = items.stream().mapToLong(item -> pointService.earnFor(item.paidAmount())).sum();
        return new OrderPreviewResponse(priced.itemsAmount(), priced.shippingFee(), priced.couponDiscount(), priced.pointsUsed(),
                priced.total(), pointService.balance(memberId),
                pointService.maxUsable(memberId, priced.itemsAmount().subtract(priced.couponDiscount())),
                pointService.getPolicy().minUsePoints(), toEarn, couponService.optionsFor(memberId, couponLines(items)));
    }

    /**
     * 상품 줄을 값으로 바꾸고 배송비·쿠폰·포인트를 매긴다. commit 이면 행을 잠그고 재고를 뺀다.
     * 쿠폰은 대상 줄에, 포인트는 쿠폰을 뺀 금액 비율로 줄마다 나눠 두어 부분 취소 때 그 줄 몫만 정산한다.
     */
    private Priced price(final Long memberId, final List<Line> lines, final Receiver receiver, final Discounts discounts,
                         final boolean commit) {
        if (lines.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }
        List<OrderItem> items = new ArrayList<>();
        for (Line line : lines) {
            Product product = (commit ? productRepository.findForUpdate(line.productId())
                    : productRepository.findByIdAndDeletedAtIsNull(line.productId()))
                    .filter(Product::isOnSale)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE));
            if (product.getSaleType() != ProductSaleType.PURCHASE) {
                throw new BusinessException(ErrorCode.CART_PURCHASE_ONLY);
            }
            gradeService.require(product.getAccessGradeId());
            ProductOption option = commit ? lockOption(product, line.productOptionId()) : findOption(product, line.productOptionId());
            BigDecimal unitPrice = product.getPrice().add(option == null ? BigDecimal.ZERO : option.getAdditionalPrice());
            BigDecimal amount = discountService.applyTo(line.discountId(), product.getId(),
                    unitPrice.multiply(BigDecimal.valueOf(line.quantity())));
            if (commit) {
                takeStock(product, option, line.quantity());
            }
            items.add(OrderItem.builder()
                    .orderId(0L)
                    .productId(product.getId())
                    .productOptionId(option == null ? null : option.getId())
                    .productName(product.getName())
                    .optionName(option == null ? null : option.getName())
                    .unitPrice(unitPrice)
                    .quantity(line.quantity())
                    .discountId(line.discountId())
                    .amount(amount)
                    .requiresShipping(product.isRequiresShipping())
                    .build());
        }
        BigDecimal itemsAmount = sum(items);
        // 관리자가 주소 없이 받은 주문(방문 수령 등)은 배송비를 받지 않는다.
        BigDecimal shippingFee = receiver.hasAddress() ? shippingFeeOf(items, receiver.postalCode()) : BigDecimal.ZERO;

        CouponService.Evaluation coupon = memberId == null || discounts.couponIssueId() == null
                ? new CouponService.Evaluation(BigDecimal.ZERO, Collections.nCopies(items.size(), BigDecimal.ZERO))
                : couponService.evaluate(memberId, discounts.couponIssueId(), couponLines(items));
        BigDecimal afterCoupon = itemsAmount.subtract(coupon.discount());
        long points = memberId == null ? 0 : discounts.usePoints();
        pointService.validateUse(memberId, points, afterCoupon);

        long left = points;
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            BigDecimal net = item.getAmount().subtract(coupon.allocation().get(i));
            long share = i == items.size() - 1 || afterCoupon.signum() == 0 ? left
                    : net.multiply(BigDecimal.valueOf(points)).divide(afterCoupon, 0, RoundingMode.DOWN).longValue();
            share = Math.min(share, left);
            item.applyDiscounts(coupon.allocation().get(i), share);
            left -= share;
        }
        return new Priced(items, itemsAmount, shippingFee, coupon.discount(), points);
    }

    private static List<CouponService.Line> couponLines(final List<OrderItem> items) {
        return items.stream().map(item -> new CouponService.Line(item.getProductId(), item.getAmount())).toList();
    }

    private OrderResponse createSingle(final UserType userType, final Long userId, final Product product,
                                       final OrderCreateRequest request, final boolean paid) {
        ProductOption option = lockOption(product, request.productOptionId());
        BigDecimal unitPrice = product.getPrice().add(option == null ? BigDecimal.ZERO : option.getAdditionalPrice());
        String productName = option == null ? product.getName() : product.getName() + " (" + option.getName() + ")";
        boolean quote = product.getSaleType() == ProductSaleType.QUOTE;
        BigDecimal total = null;
        if (!quote) {
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(request.quantity()));
            total = discountService.applyTo(request.discountId(), product.getId(), subtotal);
            takeStock(product, option, request.quantity());
        }
        Order order = orderRepository.save(Order.builder()
                .orderNumber(nextOrderNumber())
                .userType(userType)
                .userId(userId)
                .productId(product.getId())
                .saleType(product.getSaleType())
                .productOptionId(option == null ? null : option.getId())
                .productName(productName)
                .quantity(request.quantity())
                .discountId(quote ? null : request.discountId())
                .totalPrice(total)
                .requestMessage(quote ? blankToNull(request.requestMessage()) : null)
                .status(quote ? OrderStatus.QUOTE_REQUESTED : paid ? OrderStatus.PAID : OrderStatus.PENDING_PAYMENT)
                .canDeliver(request.address() != null && !request.address().isBlank())
                .receiverName(request.receiverName())
                .phoneNumber(request.phoneNumber())
                .postalCode(request.postalCode())
                .address(request.address())
                .detailAddress(request.detailAddress())
                .build());
        if (order.getStatus() == OrderStatus.PAID) {
            events.publishEvent(new OrderPaidEvent(order.getId()));
        }
        return OrderResponse.of(order, List.of());
    }

    private OrderResponse donate(final UserType userType, final Long userId, final Product product,
                                 final OrderCreateRequest request, final boolean paid) {
        if (request.amount() == null || request.amount().compareTo(product.getPrice()) < 0) {
            throw new BusinessException(ErrorCode.DONATION_AMOUNT_TOO_LOW);
        }
        Order order = orderRepository.save(Order.builder()
                .orderNumber(nextOrderNumber())
                .userType(userType)
                .userId(userId)
                .productId(product.getId())
                .saleType(ProductSaleType.DONATION)
                .productName(product.getName())
                .quantity(1)
                .totalPrice(request.amount())
                .status(paid ? OrderStatus.PAID : OrderStatus.PENDING_PAYMENT)
                .receiverName(request.receiverName())
                .phoneNumber(request.phoneNumber())
                .build());
        return OrderResponse.of(order, List.of());
    }

    @Transactional
    public Order createRenewal(final Long memberId, final Long subscriptionId, final Long firstOrderId, final BigDecimal price) {
        Order first = orderRepository.findById(firstOrderId).orElseThrow(BusinessException::notFound);
        takeStock(first.getProductId(), first.getProductOptionId(), first.getQuantity());
        return orderRepository.save(Order.builder()
                .orderNumber(nextOrderNumber())
                .userType(UserType.MEMBER)
                .userId(memberId)
                .productId(first.getProductId())
                .saleType(ProductSaleType.SUBSCRIPTION)
                .subscriptionId(subscriptionId)
                .productOptionId(first.getProductOptionId())
                .productName(first.getProductName())
                .quantity(first.getQuantity())
                .totalPrice(price)
                .status(OrderStatus.PAID)
                .canDeliver(first.isCanDeliver())
                .receiverName(first.getReceiverName())
                .phoneNumber(first.getPhoneNumber())
                .postalCode(first.getPostalCode())
                .address(first.getAddress())
                .detailAddress(first.getDetailAddress())
                .build());
    }

    @Transactional
    public OrderResponse quote(final Long orderId, final QuoteRequest request) {
        Order order = findOrder(orderId);
        order.quote(request.amount(), blankToNull(request.message()));
        return toResponse(order);
    }

    @Transactional
    public OrderResponse acceptMyQuote(final Long memberId, final Long orderId) {
        Order order = findMine(memberId, orderId);
        order.acceptQuote();
        takeStock(order.getProductId(), order.getProductOptionId(), order.getQuantity());
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public DonationProgress donationProgress(final Long productId) {
        return new DonationProgress(orderRepository.sumPaidDonations(productId), orderRepository.countPaidDonations(productId));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(final Long memberId, final Pageable pageable) {
        return toResponses(orderRepository.findAllByUserTypeAndUserIdOrderByIdDesc(UserType.MEMBER, memberId, pageable));
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(final Long memberId, final Long orderId) {
        return toResponse(findMine(memberId, orderId));
    }

    @Transactional
    public void cancelMyOrder(final Long memberId, final Long orderId) {
        cancel(findMine(memberId, orderId));
    }

    @Transactional
    public OrderResponse cancelMyItem(final Long memberId, final Long orderId, final Long itemId) {
        return cancelItem(findMine(memberId, orderId), itemId);
    }

    @Transactional
    public OrderResponse cancelItem(final Long orderId, final Long itemId) {
        return cancelItem(findOrder(orderId), itemId);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> searchOrders(final OrderStatus status, final String keyword, final Pageable pageable) {
        String text = keyword == null ? "" : keyword.trim();
        if (regexService.matches(RegexTarget.ORDER_NUMBER, text) && !text.isEmpty()) {
            return toResponses(orderRepository.findByOrderNumberAndStatus(text, status, pageable));
        }
        return toResponses(orderRepository.search(status, text, pageable));
    }

    @Transactional
    public void markPaid(final Long orderId) {
        findOrder(orderId).markPaid();
        events.publishEvent(new OrderPaidEvent(orderId));
    }

    @Transactional
    public void changeStatus(final Long orderId, final OrderStatus status) {
        Order order = findOrder(orderId);
        if (status == OrderStatus.CANCELED) {
            cancel(order);
        } else {
            order.moveTo(status);
        }
    }

    @Transactional
    public OrderResponse ship(final Long orderId, final ShipmentRequest request) {
        Order order = findOrder(orderId);
        order.ship(request.courier().trim(), request.trackingNumber().trim());
        return toResponse(order);
    }

    /** 남은 상품 금액이 무료 배송 기준 아래로 내려가 배송비가 생기면 그만큼 환불액에서 뺀다. */
    private OrderResponse cancelItem(final Order order, final Long itemId) {
        if (!order.isPartiallyCancelable()) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        List<OrderItem> items = itemRepository.findAllByOrderIdOrderByIdAsc(order.getId());
        OrderItem item = items.stream().filter(candidate -> candidate.getId().equals(itemId)).findFirst()
                .orElseThrow(BusinessException::notFound);
        if (!item.isActive()) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        List<OrderItem> remaining = items.stream().filter(OrderItem::isActive).filter(other -> other != item).toList();
        if (remaining.isEmpty()) {
            cancel(order);
            return toResponse(order);
        }
        returnStock(item);
        Long memberId = memberOf(order);
        pointService.restore(memberId, item.getPointsUsed(), order.getId());
        BigDecimal newShipping = order.isCanDeliver() ? shippingFeeOf(remaining, order.getPostalCode()) : BigDecimal.ZERO;
        if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
            item.cancel(BigDecimal.ZERO);
            order.repriceBeforePayment(sum(remaining), newShipping,
                    remaining.stream().map(OrderItem::getCouponDiscount).reduce(BigDecimal.ZERO, BigDecimal::add),
                    remaining.stream().mapToLong(OrderItem::getPointsUsed).sum());
        } else {
            BigDecimal oldShipping = order.getShippingFee() == null ? BigDecimal.ZERO : order.getShippingFee();
            BigDecimal refund = item.paidAmount().add(oldShipping).subtract(newShipping)
                    .max(BigDecimal.ZERO).min(order.paidRemaining());
            item.cancel(refund);
            order.recordRefund(refund, newShipping);
            paymentService.refundPartially(PaymentTargetType.ORDER, order.getId(), refund);
            pointService.revokeEarned(memberId, item.getPointsEarned(), order.getId());
            publishRefund(order, List.of(item.getProductId()));
        }
        return OrderResponse.of(order, items);
    }

    private void cancel(final Order order) {
        boolean heldStock = order.holdsStock();
        boolean paid = order.getStatus() == OrderStatus.PAID;
        BigDecimal remaining = order.paidRemaining();
        order.cancel();
        List<OrderItem> items = itemRepository.findAllByOrderIdOrderByIdAsc(order.getId());
        if (items.isEmpty()) {
            if (heldStock) {
                returnStock(order.getProductId(), order.getProductOptionId(), order.getQuantity());
            }
        } else {
            List<OrderItem> active = items.stream().filter(OrderItem::isActive).toList();
            BigDecimal shipping = order.getShippingFee() == null ? BigDecimal.ZERO : order.getShippingFee();
            Long memberId = memberOf(order);
            for (int i = 0; i < active.size(); i++) {
                OrderItem item = active.get(i);
                returnStock(item);
                boolean last = i == active.size() - 1;
                item.cancel(!paid ? BigDecimal.ZERO : last ? item.paidAmount().add(shipping) : item.paidAmount());
            }
            pointService.restore(memberId, active.stream().mapToLong(OrderItem::getPointsUsed).sum(), order.getId());
            if (paid) {
                order.recordRefund(remaining, order.getShippingFee());
                pointService.revokeEarned(memberId, active.stream().mapToLong(OrderItem::getPointsEarned).sum(), order.getId());
            }
            couponService.restore(order.getCouponIssueId());
        }
        paymentService.refundIfPaid(PaymentTargetType.ORDER, order.getId());
        if (paid) {
            publishRefund(order, items.isEmpty() ? List.of(order.getProductId())
                    : items.stream().map(OrderItem::getProductId).distinct().toList());
        }
    }

    private static Long memberOf(final Order order) {
        return order.getUserType() == UserType.MEMBER ? order.getUserId() : null;
    }

    private void publishRefund(final Order order, final List<Long> productIds) {
        if (order.getUserType() == UserType.MEMBER) {
            events.publishEvent(new OrderRefundedEvent(order.getId(), order.getUserId(), productIds));
        }
    }

    private ProductOption lockOption(final Product product, final Long optionId) {
        if (optionId == null) {
            if (product.hasOptionGroups()) {
                throw new BusinessException(ErrorCode.OPTION_REQUIRED);
            }
            return null;
        }
        ProductOption option = optionRepository.findForUpdate(optionId)
                .filter(found -> found.getProductId().equals(product.getId()) && found.getDeletedAt() == null)
                .orElseThrow(BusinessException::notFound);
        if (!option.isEnabled()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE);
        }
        return option;
    }

    private ProductOption findOption(final Product product, final Long optionId) {
        if (optionId == null) {
            if (product.hasOptionGroups()) {
                throw new BusinessException(ErrorCode.OPTION_REQUIRED);
            }
            return null;
        }
        ProductOption option = optionRepository.findByIdAndProductIdAndDeletedAtIsNull(optionId, product.getId())
                .orElseThrow(BusinessException::notFound);
        if (!option.isEnabled()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE);
        }
        return option;
    }

    private static void takeStock(final Product product, final ProductOption option, final int quantity) {
        product.decreaseStock(quantity);
        if (option != null) {
            option.decreaseStock(quantity);
        }
    }

    private void takeStock(final Long productId, final Long optionId, final int quantity) {
        productRepository.findForUpdate(productId).ifPresent(product -> product.decreaseStock(quantity));
        if (optionId != null) {
            optionRepository.findForUpdate(optionId).ifPresent(option -> option.decreaseStock(quantity));
        }
    }

    private void returnStock(final OrderItem item) {
        returnStock(item.getProductId(), item.getProductOptionId(), item.getQuantity());
    }

    private void returnStock(final Long productId, final Long optionId, final int quantity) {
        productRepository.findForUpdate(productId).ifPresent(product -> product.increaseStock(quantity));
        if (optionId != null) {
            optionRepository.findForUpdate(optionId).ifPresent(option -> option.increaseStock(quantity));
        }
    }

    private BigDecimal shippingFeeOf(final List<OrderItem> items, final String postalCode) {
        List<OrderItem> shippable = items.stream().filter(OrderItem::isRequiresShipping).toList();
        return shippingService.quote(!shippable.isEmpty(), sum(shippable), postalCode).total();
    }

    private static BigDecimal sum(final List<OrderItem> items) {
        return items.stream().map(OrderItem::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String headline(final List<OrderItem> items) {
        OrderItem first = items.getFirst();
        String name = first.getOptionName() == null ? first.getProductName() : first.getProductName() + " (" + first.getOptionName() + ")";
        return items.size() == 1 ? name : name + " 외 " + (items.size() - 1) + "건";
    }

    private OrderResponse toResponse(final Order order) {
        return OrderResponse.of(order, itemRepository.findAllByOrderIdOrderByIdAsc(order.getId()));
    }

    private Page<OrderResponse> toResponses(final Page<Order> orders) {
        List<Long> ids = orders.map(Order::getId).toList();
        Map<Long, List<OrderItem>> items = ids.isEmpty() ? Map.of()
                : itemRepository.findAllByOrderIdInOrderByIdAsc(ids).stream().collect(Collectors.groupingBy(OrderItem::getOrderId));
        return orders.map(order -> OrderResponse.of(order, items.getOrDefault(order.getId(), List.of())));
    }

    private Order findOrder(final Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(BusinessException::notFound);
    }

    private Order findMine(final Long memberId, final Long orderId) {
        return orderRepository.findByIdAndUserTypeAndUserId(orderId, UserType.MEMBER, memberId).orElseThrow(BusinessException::notFound);
    }

    private String nextOrderNumber() {
        return regexService.requireNumberFormat(RegexTarget.ORDER_NUMBER, BusinessNumbers.next("O", orderRepository::existsByOrderNumber));
    }

    private static String blankToNull(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
