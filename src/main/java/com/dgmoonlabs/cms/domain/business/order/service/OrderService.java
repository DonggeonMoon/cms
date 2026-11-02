package com.dgmoonlabs.cms.domain.business.order.service;

import com.dgmoonlabs.cms.domain.business.BusinessNumbers;
import com.dgmoonlabs.cms.domain.business.discount.service.DiscountService;
import com.dgmoonlabs.cms.domain.business.order.constant.OrderStatus;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderCreateRequest;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderResponse;
import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import com.dgmoonlabs.cms.domain.business.order.entity.product.ProductOption;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductOptionRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductRepository;
import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentTargetType;
import com.dgmoonlabs.cms.domain.business.payment.service.PaymentService;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository optionRepository;
    private final DiscountService discountService;
    private final PaymentService paymentService;

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

        BigDecimal unitPrice = product.getPrice();
        String productName = product.getName();
        if (request.productOptionId() != null) {
            ProductOption option = optionRepository.findByIdAndProductIdAndDeletedAtIsNull(request.productOptionId(), product.getId())
                    .orElseThrow(BusinessException::notFound);
            unitPrice = unitPrice.add(option.getAdditionalPrice());
            productName = productName + " (" + option.getName() + ")";
        }
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(request.quantity()));
        BigDecimal total = discountService.applyTo(request.discountId(), product.getId(), subtotal);

        product.decreaseStock(request.quantity());
        Order order = orderRepository.save(Order.builder()
                .orderNumber(BusinessNumbers.next("O", orderRepository::existsByOrderNumber))
                .userType(userType)
                .userId(userId)
                .productId(product.getId())
                .productOptionId(request.productOptionId())
                .productName(productName)
                .quantity(request.quantity())
                .discountId(request.discountId())
                .totalPrice(total)
                .status(paid ? OrderStatus.PAID : OrderStatus.PENDING_PAYMENT)
                .canDeliver(request.address() != null && !request.address().isBlank())
                .receiverName(request.receiverName())
                .phoneNumber(request.phoneNumber())
                .postalCode(request.postalCode())
                .address(request.address())
                .detailAddress(request.detailAddress())
                .build());
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(final Long memberId, final Pageable pageable) {
        return orderRepository.findAllByUserTypeAndUserIdOrderByIdDesc(UserType.MEMBER, memberId, pageable).map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(final Long memberId, final Long orderId) {
        return OrderResponse.from(orderRepository.findByIdAndUserTypeAndUserId(orderId, UserType.MEMBER, memberId).orElseThrow(BusinessException::notFound));
    }

    @Transactional
    public void cancelMyOrder(final Long memberId, final Long orderId) {
        cancel(orderRepository.findByIdAndUserTypeAndUserId(orderId, UserType.MEMBER, memberId).orElseThrow(BusinessException::notFound));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> searchOrders(final OrderStatus status, final String keyword, final Pageable pageable) {
        return orderRepository.search(status, keyword == null ? "" : keyword.trim(), pageable).map(OrderResponse::from);
    }

    @Transactional
    public void markPaid(final Long orderId) {
        orderRepository.findById(orderId).orElseThrow(BusinessException::notFound).markPaid();
    }

    @Transactional
    public void changeStatus(final Long orderId, final OrderStatus status) {
        Order order = orderRepository.findById(orderId).orElseThrow(BusinessException::notFound);
        if (status == OrderStatus.CANCELED) {
            cancel(order);
        } else {
            order.moveTo(status);
        }
    }

    private void cancel(final Order order) {
        order.cancel();
        productRepository.findForUpdate(order.getProductId()).ifPresent(product -> product.increaseStock(order.getQuantity()));
        paymentService.refundIfPaid(PaymentTargetType.ORDER, order.getId());
    }
}
