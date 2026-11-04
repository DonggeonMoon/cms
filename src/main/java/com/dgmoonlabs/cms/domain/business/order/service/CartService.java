package com.dgmoonlabs.cms.domain.business.order.service;

import com.dgmoonlabs.cms.domain.business.order.constant.ProductSaleType;
import com.dgmoonlabs.cms.domain.business.order.dto.CartDtos.*;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.CartCheckoutRequest;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderResponse;
import com.dgmoonlabs.cms.domain.business.order.entity.CartItem;
import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import com.dgmoonlabs.cms.domain.business.order.entity.product.ProductOption;
import com.dgmoonlabs.cms.domain.business.order.repository.CartItemRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductOptionRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductRepository;
import com.dgmoonlabs.cms.domain.business.shipping.entity.ShippingPolicy;
import com.dgmoonlabs.cms.domain.business.shipping.service.ShippingService;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private static final int MAX_QUANTITY = 999;

    private final CartItemRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository optionRepository;
    private final ShippingService shippingService;
    private final OrderService orderService;

    @Transactional(readOnly = true)
    public CartResponse get(final Long memberId, final String postalCode) {
        return toResponse(cartRepository.findAllByMemberIdOrderByIdAsc(memberId), postalCode);
    }

    @Transactional
    public CartResponse add(final Long memberId, final CartAddRequest request) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(request.productId())
                .filter(Product::isOnSale)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE));
        if (product.getSaleType() != ProductSaleType.PURCHASE) {
            throw new BusinessException(ErrorCode.CART_PURCHASE_ONLY);
        }
        if (request.productOptionId() == null && product.hasOptionGroups()) {
            throw new BusinessException(ErrorCode.OPTION_REQUIRED);
        }
        if (request.productOptionId() != null) {
            optionRepository.findByIdAndProductIdAndDeletedAtIsNull(request.productOptionId(), product.getId())
                    .filter(ProductOption::isEnabled)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE));
        }
        Optional<CartItem> existing = request.productOptionId() == null
                ? cartRepository.findByMemberIdAndProductIdAndProductOptionIdIsNull(memberId, product.getId())
                : cartRepository.findByMemberIdAndProductIdAndProductOptionId(memberId, product.getId(), request.productOptionId());
        existing.ifPresentOrElse(
                item -> item.changeQuantity(Math.min(MAX_QUANTITY, item.getQuantity() + request.quantity())),
                () -> cartRepository.save(CartItem.builder()
                        .memberId(memberId)
                        .productId(product.getId())
                        .productOptionId(request.productOptionId())
                        .quantity(request.quantity())
                        .build()));
        return get(memberId, null);
    }

    @Transactional
    public CartResponse changeQuantity(final Long memberId, final Long itemId, final int quantity) {
        cartRepository.findByIdAndMemberId(itemId, memberId).orElseThrow(BusinessException::notFound).changeQuantity(quantity);
        return get(memberId, null);
    }

    @Transactional
    public CartResponse remove(final Long memberId, final Long itemId) {
        cartRepository.findByIdAndMemberId(itemId, memberId).ifPresent(cartRepository::delete);
        return get(memberId, null);
    }

    @Transactional
    public OrderResponse checkout(final Long memberId, final CartCheckoutRequest request) {
        List<CartItem> items = cartRepository.findAllByMemberIdAndIdIn(memberId, request.cartItemIds());
        if (items.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }
        OrderResponse order = orderService.place(UserType.MEMBER, memberId,
                items.stream().map(item -> new OrderService.Line(item.getProductId(), item.getProductOptionId(),
                        item.getQuantity(), null)).toList(),
                new OrderService.Receiver(request.receiverName(), request.phoneNumber(), request.postalCode(),
                        request.address(), request.detailAddress()),
                false);
        cartRepository.deleteAll(items);
        return order;
    }

    private CartResponse toResponse(final List<CartItem> items, final String postalCode) {
        Map<Long, Product> products = productRepository.findAllById(items.stream().map(CartItem::getProductId).toList())
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<Long, ProductOption> options = optionRepository.findAllByIdIn(
                        items.stream().map(CartItem::getProductOptionId).filter(Objects::nonNull).toList())
                .stream().collect(Collectors.toMap(ProductOption::getId, Function.identity()));
        List<CartItemResponse> lines = items.stream().map(item -> toLine(item, products.get(item.getProductId()),
                item.getProductOptionId() == null ? null : options.get(item.getProductOptionId()))).toList();
        List<CartItemResponse> orderable = lines.stream().filter(CartItemResponse::available).toList();
        BigDecimal itemsAmount = orderable.stream().map(CartItemResponse::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<CartItemResponse> shippable = orderable.stream().filter(CartItemResponse::requiresShipping).toList();
        ShippingPolicy.Quote quote = shippingService.quote(!shippable.isEmpty(),
                shippable.stream().map(CartItemResponse::amount).reduce(BigDecimal.ZERO, BigDecimal::add), postalCode);
        return new CartResponse(lines, itemsAmount, quote.total(), quote.regionName(),
                shippingService.getPolicy().freeThreshold(), itemsAmount.add(quote.total()));
    }

    private static CartItemResponse toLine(final CartItem item, final Product product, final ProductOption option) {
        if (product == null) {
            return new CartItemResponse(item.getId(), item.getProductId(), item.getProductOptionId(), "삭제된 상품", null, null,
                    BigDecimal.ZERO, item.getQuantity(), BigDecimal.ZERO, 0, false, false);
        }
        BigDecimal unitPrice = product.getPrice().add(option == null ? BigDecimal.ZERO : option.getAdditionalPrice());
        Integer stock = option != null && option.getStock() != null
                ? (product.getAvailableQuantity() == null ? option.getStock() : Math.min(option.getStock(), product.getAvailableQuantity()))
                : product.getAvailableQuantity();
        boolean optionOk = item.getProductOptionId() == null
                ? !product.hasOptionGroups()
                : option != null && option.getDeletedAt() == null && option.isEnabled();
        boolean available = product.isOnSale() && product.getSaleType() == ProductSaleType.PURCHASE && optionOk
                && (stock == null || stock >= item.getQuantity());
        return new CartItemResponse(item.getId(), product.getId(), item.getProductOptionId(), product.getName(),
                option == null ? null : option.getName(),
                product.getImageFileId() == null ? null : "/api/files/" + product.getImageFileId() + "/view",
                unitPrice, item.getQuantity(), unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())), stock,
                product.isRequiresShipping(), available);
    }
}
